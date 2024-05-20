package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

enum Tree[+T]:
  case Empty extends Tree[Nothing]
  case Node(value: T, left: Tree[T], right: Tree[T])
object Tree:
  def isSameStructure(tree1: Tree[?], tree2: Tree[?]): Boolean =
    (tree1, tree2) match
      case (Empty, Empty)         => true
      case (Node(_, _, _), Empty) => false
      case (Empty, Node(_, _, _)) => false
      case (Node(_, left1, right1), Node(_, left2, right2)) =>
        isSameStructure(left1, left2) && isSameStructure(right1, right2)
implicit def treeInstance[A](using
    default: Defaultable[A]
)(using Form[A]): Form[Tree[A]] =
  new Form[Tree[A]] { self =>
    override def isAnyRef = true
    override def render(
        variable: Var[Tree[A]],
        syncParent: () => Unit,
        values: List[Tree[A]]
    )(using WidgetFactory): HtmlElement =
      variable.now() match
        case Tree.Empty =>
          button(
            "Add me",
            onClick.mapToUnit --> { _ =>
              variable.set(Tree.Node(default.default, Tree.Empty, Tree.Empty))
              syncParent()
            }
          )
        case Tree.Node(value, left, right) =>
          div(
            button(
              "drop",
              onClick.mapToUnit --> { _ =>
                variable.set(Tree.Empty)
                syncParent()

              }
            ),
            if false then
              summon[Form[Tree.Node[A]]].render(
                variable.asInstanceOf[Var[Tree.Node[A]]]
              )
            else
              val vVar = Var(value)
              val lVar = Var(left)
              val rVar = Var(right)

              Seq(
                Form.renderVar(
                  vVar,
                  () => {
                    variable.set(Tree.Node(vVar.now(), left, right))
                    syncParent()
                  }
                ),
                div(
                  "left",
                  Form.renderVar(
                    lVar,
                    () => {
                      variable.set(Tree.Node(value, lVar.now(), right))
                      syncParent()
                    }
                  )
                ),
                div(
                  "right",
                  Form.renderVar(
                    rVar,
                    () => {
                      variable.set(Tree.Node(value, left, rVar.now()))

                      syncParent()
                    }
                  )
                )
              )
          )
  }

val tree = Sample(
  "Tree", {

    import Tree.*

    case class Person(name: String, age: Int)
    object Person {
      given Defaultable[Person] with
        def default = Person("--", 0)
    }
    val treeVar2 = Var(
      Node(Person("agnes", 50), Node(Person("Zozo", 53), Empty, Empty), Empty)
    )
    div(
      child <-- treeVar2.signal.map { item =>
        div(
          s"$item zozo"
        )
      },
      child <-- treeVar2.signal
        .distinctByFn(Tree.isSameStructure)
        .map { item =>
          Form.renderVar(treeVar2)
        }
    )
  }
)
