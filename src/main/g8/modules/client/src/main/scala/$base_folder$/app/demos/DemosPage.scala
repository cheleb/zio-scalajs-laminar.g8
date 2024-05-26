package $package$.app.demos

import com.raquo.laminar.api.L.*

object DemosPage:
  
  def apply(): HtmlElement =
    div(
      h1("Demos Page"),
      ul(
      $if(scalablytyped.truthy)$
        demo("Scalablytyped", "/demos/scalablytyped"),
      $endif$
      $if(scalariform.truthy)$
        demo("Scalariform", "/demos/scalariform")
      )
      $endif$
    )
  
  private def demo(title: String, link: String) =
    li(
      a(
        href := link,
        title
      )
    )
