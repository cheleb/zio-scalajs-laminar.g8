package $package$.app

import dev.cheleb.scalamigen.ui5.UI5WidgetFactory
import dev.cheleb.scalamigen.WidgetFactory
import dev.cheleb.scalamigen.Defaultable

import $package$.domain.*

given f: WidgetFactory = UI5WidgetFactory

given Defaultable[Cat] with
  def default = Cat("")

given Defaultable[Dog] with
  def default = Dog("", 1)
