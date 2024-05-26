package $package$.app.demos

import com.raquo.laminar.api.L.*

object DemosPage {
  
    def apply(): HtmlElement = {
      div(
        h1("Demos Page"),
        p("This is a demo page."),
        p("You can add more pages here."),
      )
    }

}
