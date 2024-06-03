package $package$.common

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.LinkingInfo

import org.scalajs.dom.window

object Constants:
  @js.native
  @JSImport("/static/img/fiery-lava 128x128.png", JSImport.Default)
  val logoImage: String = js.native

  @js.native
  @JSImport("/static/img/generic_company.png", JSImport.Default)
  val companyLogoPlaceHolder: String = js.native

  val emailRegex =
    """^[a-zA-Z0-9\.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\$"""

  val urlRegex = """^(https?):\/\/(([^:/?#]+)(?::(\d+))?)(\/[^?#]*)?(\?[^#]*)?(#.*)?"""

  val backendBaseURL =
    if LinkingInfo.developmentMode then "http://localhost:8080"
    else window.document.location.origin
