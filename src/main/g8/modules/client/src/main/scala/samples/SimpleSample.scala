package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

val simple = Sample(
  "Simple", {

    val eitherVar = Var(Cat("Scala le chat", 6))

    div(
      child <-- eitherVar.signal.map { item =>
        div(
          s"$item"
        )
      },
      Form.renderVar(eitherVar)
    )
  }
)
