package samples

import dev.cheleb.scalamigen.Defaultable
import dev.cheleb.scalamigen.ui5.UI5WidgetFactory
import dev.cheleb.scalamigen.WidgetFactory
import dev.cheleb.scalamigen.LaminarWidgetFactory

case class Cat(name: String, weight: Int)
case class Dog(name: String, weight: Int)

given Defaultable[Cat] with
  def default = Cat("", 0)

given Defaultable[Dog] with
  def default = Dog("", 0)

given WidgetFactory = if true then UI5WidgetFactory else LaminarWidgetFactory
