package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

import com.raquo.airstream.state.Var

import samples.model.CurrencyCode

given Form[CurrencyCode] = stringForm(CurrencyCode(_))

case class IronSample(
    curenncyCode: CurrencyCode,
    optional: Option[String],
    optionalInt: Option[Int],
    doubleGreaterThanEight: Double :| GreaterEqual[8.0],
    optionalDoublePositive: Option[Double :| Positive]
)

given IronTypeValidator[Double, GreaterEqual[8.0]] =
  _.toDoubleOption match
    case None         => Left("Nots a number")
    case Some(double) => double.refineEither[GreaterEqual[8.0]]

val ironSampleVar = Var(
  IronSample(CurrencyCode("Eur"), Some("name"), Some(1), 9.1, Some(1))
)

val validation = Sample(
  "Validation",
  div(
    child <-- ironSampleVar.signal.map { item =>
      div(
        s"\$item"
      )
    },
    Form.renderVar(ironSampleVar)
  )
)
