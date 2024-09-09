package $package$.app

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.Bar

object Footer:
  def apply(): HtmlElement =
    Bar("By laminar, zio, tapir, and ui5")
