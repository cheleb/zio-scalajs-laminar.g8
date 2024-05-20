package samples.model

opaque type CurrencyCode = String

object CurrencyCode:
  def apply(code: String): CurrencyCode = code
