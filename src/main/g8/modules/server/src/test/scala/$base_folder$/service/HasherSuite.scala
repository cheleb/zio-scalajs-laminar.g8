package $package$.service

import munit.*

class HasherSuite extends FunSuite {

  val hasher = Hasher

  test("Hasher should generate different hashes for different inputs") {
    val input1 = "test string 1"
    val input2 = "test string 2"
    val hash1  = hasher.generatedHash(input1)
    val hash2  = hasher.generatedHash(input2)
    assertNotEquals(hash1, hash2)
  }

  test("Hasher should handle empty string") {
    val input = ""
    val hash  = hasher.generatedHash(input)
    assert(hash.nonEmpty)
  }

  test("Hasher should handle special characters") {
    val input = "!@#\$%^&*()"
    val hash  = hasher.generatedHash(input)
    assert(hash.nonEmpty)
  }

  test("Hasher should generate fixed-length output") {
    val input1 = "short"
    val input2 = "very long string with lots of characters"
    val hash1  = hasher.generatedHash(input1)
    val hash2  = hasher.generatedHash(input2)
    assertEquals(hash1.length, hash2.length)
  }
}
