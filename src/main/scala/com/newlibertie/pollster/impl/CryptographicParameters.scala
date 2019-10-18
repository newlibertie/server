package com.newlibertie.pollster.impl

import java.math.BigInteger

object CryptographicParameters {
  val BITS = 1000
  import java.security.SecureRandom;
  private val rng = new SecureRandom()

  def probablePrime() = BigInteger.probablePrime(BITS, rng)

  def random() = new BigInteger(BITS, rng)

  def apply(p:BigInteger, g:BigInteger, s:BigInteger) = new CryptographicParameters(p, g, s)
}


case class CryptographicParameters
(
  large_prime_p:BigInteger = CryptographicParameters.probablePrime(),
  generator_g:BigInteger = CryptographicParameters.random(),
  private_key_s:BigInteger = CryptographicParameters.random()
)
{
  val public_key_h = generator_g.modPow(private_key_s, large_prime_p)

  def getPublicKeyValueString: String = {
    s"""
      |"public_key_h":"${public_key_h}"
      |""".stripMargin
  }
}


