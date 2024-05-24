package $package$.app

import com.raquo.laminar.api.L.*
import org.scalajs.dom

//import dev.cheleb.scalamigen.{*, given}

//import domain.*

@main def hello: Unit =

//  val personVar = Var(Person("Alice", 42, Left(Cat("Fluffy"))))

  val myApp =
    div(
      h1("Hello World")
      //    child.text <-- personVar.signal.map(p => s"\$p"),
      //    Form.renderVar(personVar)
    )

  val containerNode = dom.document.getElementById("app")
  render(containerNode, myApp)
