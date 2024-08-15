package $package$.domain.errors

sealed abstract class ApplicationException(message: String) extends RuntimeException(message)

case class UnauthorizedException(message: String) extends ApplicationException(message)

case class TooYoungException(age: Int) extends ApplicationException(s"Person is too young: \$age")
