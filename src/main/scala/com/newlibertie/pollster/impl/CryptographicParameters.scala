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
  p:BigInteger = CryptographicParameters.probablePrime(),       // large prime
  g:BigInteger = CryptographicParameters.random(),              //  generator
  s:BigInteger = CryptographicParameters.random()               //  secret key
)
{
  val h = g.modPow(s, p)
}


