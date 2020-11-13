package com.newlibertie.pollster.impl

import java.math.BigInteger

import com.newlibertie.pollster.impl.CryptographicParameters.{BITS, isdbg, rng}

object CryptographicParameters {
  final val isdbg = false
  val BITS = 32 // 1000  // TODO : check for various values and then choose a production secure value
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
//  val zkp_generator_G:BigInteger = BigInteger.probablePrime(BITS/2, rng)
//  val zkp_generator_G:BigInteger = new BigInteger("11113")
//  val public_key_h = generator_g.modPow(private_key_s, large_prime_p)
  val public_key_h = if (isdbg)
    new BigInteger("1048806755")
  else
    generator_g.modPow(private_key_s, large_prime_p)
}


