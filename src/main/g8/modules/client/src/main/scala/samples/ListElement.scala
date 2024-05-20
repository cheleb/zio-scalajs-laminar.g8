package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

case class Person2(name: String, age: Int)

case class ListElement(
    ints: List[Person2]
)

val listPersonVar = Var(ListElement(List(1, 2, 3).map(Person2("Vlad", _))))
val listIntVar = Var(List(1, 2, 3))

val list = Sample(
  "List",
  div(
    child <-- listPersonVar.signal.map { item =>
      div(
        s"\$item"
      )
    },
    Form.renderVar(listPersonVar),
    child <-- listIntVar.signal.map { item =>
      div(
        s"\$item"
      )
    },
    Form.renderVar(listIntVar)
  )
)
