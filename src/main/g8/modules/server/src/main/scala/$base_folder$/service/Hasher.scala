package $package$.service

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.SecureRandom

/** Hasher is a utility object to hash and validate passwords.
  *   - It uses PBKDF2WithHmacSHA512 as the hashing algorithm.
  */
object Hasher {

  private val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA512"
  private val PBKDF2_ITERATIONS = 1000
  private val SALT_BYTE_SIZE = 24
  private val HASH_BYTE_SIZE = 24

  /** The SecretKeyFactory is a factory for secret keys.
    */
  private val skf: SecretKeyFactory =
    SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

  /** Hashes a password with a random salt.
    */
  private def pbkdf2(
      password: Array[Char],
      salt: Array[Byte],
      iterations: Int,
      nBytes: Int
  ): Array[Byte] = {
    val keySpec = PBEKeySpec(password, salt, iterations, nBytes * 8)
    skf.generateSecret(keySpec).getEncoded()
  }

  private def toHex(array: Array[Byte]): String =
    array.map("%02X".format(_)).mkString

  private def fromHex(hex: String): Array[Byte] =
    hex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)

  private def compareArrays(a1: Array[Byte], a2: Array[Byte]): Boolean =
    val range = 0 until math.min(a1.length, a2.length)
    val diff =
      range.foldLeft(a1.length ^ a2.length)((acc, i) => acc | (a1(i) ^ a2(i)))
    diff == 0

  /** Generates a hash from a password.
    *
    * @param password
    * @return
    */
  def generatedHash(password: String): String = {
    val rng: SecureRandom = new SecureRandom()
    val salt: Array[Byte] = Array.ofDim[Byte](SALT_BYTE_SIZE)
    rng.nextBytes(salt) // fill the salt with SALT_BYTE_SIZE random bytes
    val hashBytes =
      pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
    s"\$PBKDF2_ITERATIONS:\${toHex(salt)}:\${toHex(hashBytes)}"
  }

  /** Validates a password against a hash.
    *
    * @param string
    * @param hash
    * @return
    */
  def validateHash(string: String, hash: String): Boolean =
    val hashSegments = hash.split(":")
    val iterations = hashSegments(0).toInt
    val salt = fromHex(hashSegments(1))
    val validHash = fromHex(hashSegments(2))
    val testHash = pbkdf2(
      string.toCharArray(),
      salt,
      iterations,
      HASH_BYTE_SIZE
    )
    // toHex(testHash) == toHex(validHash)
    compareArrays(testHash, validHash)

}
