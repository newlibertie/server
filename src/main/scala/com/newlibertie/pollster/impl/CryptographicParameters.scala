package com.newlibertie.pollster.impl

import java.math.BigInteger

object CryptographicParameters {
  val BITS = 1000 // 32
  val TWO_POW_BITS_SQUARE = new BigInteger("2").pow(2*BITS)
  import java.security.SecureRandom;
  private val rng = new SecureRandom()

  def probablePrime() = BigInteger.probablePrime(BITS, rng)

  def random(bits:Int = BITS) = new BigInteger(bits, rng)

  def randomCoprime(p:BigInteger) = {
    var candidate = random().mod(p)
    while( !candidate.gcd(p).equals(BigInteger.ONE) ) {
      candidate = random().mod(p)
    }
    candidate
  }

  def apply(p:BigInteger, g:BigInteger, s:BigInteger) = new CryptographicParameters(p, g, s)
}


case class CryptographicParameters
(
  large_prime_p:BigInteger = CryptographicParameters.probablePrime(),
  generator_g:BigInteger = CryptographicParameters.random(),
  private_key_s:BigInteger = CryptographicParameters.random(),
)
{
  val zkp_generator_G:BigInteger = CryptographicParameters.randomCoprime(large_prime_p)
  val public_key_h = generator_g.modPow(private_key_s, large_prime_p)

  // execution should truly never throw here because the probabilistic prime should be coprime to
  // a generator smaller than itself
  if(!this)
    throw new java.lang.Error("generator not defined");

  def getPublicKeyValueString: String = {
    s"""
      |"public_key_h":"${public_key_h}"
      |""".stripMargin
  }

  def unary_!(): Boolean = {
    if(!large_prime_p.gcd(generator_g).equals(BigInteger.ONE) ||
       !large_prime_p.gcd(zkp_generator_G).equals(BigInteger.ONE)
    ) {
      true
    } else {
      false
    }
  }
}


