package com.newlibertie.pollster.impl

import java.math.BigInteger

object CryptographicParameters {
  val BITS = 32 // 1000
  import java.security.SecureRandom;
  private val rng = new SecureRandom()

  def probablePrime() = BigInteger.probablePrime(BITS, rng)

  def random(bits:Int = BITS) = new BigInteger(bits, rng)

  def apply(p:BigInteger, g:BigInteger, s:BigInteger) = new CryptographicParameters(p, g, s)
}


case class CryptographicParameters
(
  large_prime_p:BigInteger = CryptographicParameters.probablePrime(),
  generator_g:BigInteger = CryptographicParameters.random(),
  private_key_s:BigInteger = CryptographicParameters.random(),
)
{
  val zkp_generator_G:BigInteger = CryptographicParameters.random().mod(large_prime_p)
  val public_key_h = generator_g.modPow(private_key_s, large_prime_p)
}


