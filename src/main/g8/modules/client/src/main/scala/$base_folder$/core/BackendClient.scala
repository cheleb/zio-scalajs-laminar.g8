package $package$.core

import zio.Task
import zio.ZIO
import zio.ZLayer

import $package$.config.*

import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter
import $package$.common.Constants

case class RestrictedEndpointException(message: String) extends RuntimeException(message)

/**
 * A client to the backend, extending the endpoints as methods.
 */
trait BackendClient {

  /**
   * Call an endpoint with a payload.
   *
   * This method turns an endpoint into a Task, that:
   *   - build a request from a payload
   *   - sends it to the backend
   *   - returns the response.
   *
   * @param endpoint
   * @param payload
   * @return
   */
  def endpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])(
    payload: I
  ): Task[O]

  /**
   * Call a secured endpoint with a payload.
   *
   * This method turns a secured endpoint into a Task, that:
   *   - build a request from a payload and a security token
   *   - sends it to the backend
   *   - returns the response.
   *
   * @param endpoint
   * @param payload
   * @return
   */
  def securedEndpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[String, I, E, O, Any])(payload: I): Task[O]

}

/**
 * The live implementation of the BackendClient.
 *
 * @param backend
 * @param interpreter
 * @param config
 */
private class BackendClientLive(
  backend: SttpBackend[Task, ZioStreams],
  interpreter: SttpClientInterpreter,
  config: BackendClientConfig
) extends BackendClient {

  /**
   * Turn an endpoint into a function from Input => Request.
   *
   * @param endpoint
   * @return
   */
  private def endpointRequest[I, E, O](endpoint: Endpoint[Unit, I, E, O, Any]): I => Request[Either[E, O], Any] =
    interpreter.toRequestThrowDecodeFailures(endpoint, config.baseUrl)

  /**
   * Turn a secured endpoint into curried functions from Token => Input =>
   * Request.
   *
   * @param endpoint
   * @return
   */
  private def securedEndpointRequest[A, I, E, O](
    endpoint: Endpoint[A, I, E, O, Any]
  ): A => I => Request[Either[E, O], Any] =
    interpreter.toSecureRequestThrowDecodeFailures(endpoint, config.baseUrl)

  /** Get the token from the session, or fail with an exception. */
  private def tokenOfFail =
    ZIO
      .fromOption(Session.getUserState)
      .orElseFail(RestrictedEndpointException("No token found"))
      .map(_.token)

  def endpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])(
    payload: I
  ): ZIO[Any, Throwable, O] =
    backend.send(endpointRequest(endpoint)(payload)).map(_.body).absolve

  def securedEndpointRequestZIO[I, E <: Throwable, O](
    endpoint: Endpoint[String, I, E, O, Any]
  )(payload: I): ZIO[Any, Throwable, O] = for {
    token <- tokenOfFail
    res   <- backend.send(securedEndpointRequest(endpoint)(token)(payload)).map(_.body).absolve
  } yield res

}

object BackendClientLive {
  val layer = ZLayer.fromFunction(BackendClientLive(_, _, _))

  val configuredLayer = {
    val backend: SttpBackend[Task, ZioStreams] = FetchZioBackend()
    val interpreter                            = SttpClientInterpreter()
    val config                                 = BackendClientConfig(Some(uri"\${Constants.backendBaseURL}"))

    ZLayer.succeed(backend) ++ ZLayer.succeed(interpreter) ++ ZLayer.succeed(config) >>> layer
  }

}
