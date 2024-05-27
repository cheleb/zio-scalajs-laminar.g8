package $package$.http.prometheus

import zio.*
import sttp.tapir.ztapir.ZServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

private val metrics: PrometheusMetrics[Task]           = PrometheusMetrics.default[Task]()
val metricsInterceptor = metrics.metricsInterceptor()
val metricsEndpoint: ZServerEndpoint[Any, Any] = metrics.metricsEndpoint
