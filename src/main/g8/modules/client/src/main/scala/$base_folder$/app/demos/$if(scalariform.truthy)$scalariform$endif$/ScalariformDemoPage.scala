package $package$.app.demos.scalariform

import com.raquo.laminar.api.L.*

import dev.cheleb.scalamigen.{*, given}

import $package$.domain.*

object ScalariformDemoPage:
  def apply() =
    val personVar = Var(Person("Alice", 42, Left(Cat("Fluffy"))))
    div(
      h1("Hello World"),
      child.text <-- personVar.signal.map(p => s"\$p"),
      Form.renderVar(personVar)
    )