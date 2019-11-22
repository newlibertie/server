package com.newlibertie.pollster.impl

import java.math.BigInteger

import scala.collection.mutable.ListBuffer

/**
  * Implementationv of ballot
  *
  * A ballot object is empty but permanently associated with a specific poll and
  * a specific voter upon construction
  *
  * The two ways to make it a valid ballot are as follows:
  * 1. Using the cast(vote:Boolean) method to populate its internal fields, or
  * 2. Instantiate it from pre-computed fields.  In this scenario, the implementation
  * will verify the ballot as can anyway be done using the verify() method.
  * @param parameters cryptographic parameters of the poll this ballot is about
  * @param voter String identifier identifying the voter
  */
class Ballot(cp:CryptographicParameters, voter:String) {

  var x,y :BigInteger = null

  var a1, b1, a2, b2 :BigInteger = null

  var d1, d2, r1, r2 :BigInteger = null

  /**
    * Cast a vote - populates all parameters to become a verifiable ballot
    * @param vote boolean vote, true = yay, false = nay
    */
  def cast(vote:Boolean) = {
    // Voter votes m^0 or m^1
    // Ballot is a tuple (x,y,a1,b1,a2,b2)
    //    x    g^alpha,
    //    y    h^alpha G      OR  h^alpha / G
    //    a1   g^r1 x^{d1}    OR  g ^ omega
    //    b1   h^r1 (yG)^{d1} OR  h ^ omega
    //    a2   g ^ omega      OR  g ^ {r2} x ^ {d2}
    //    b2   h ^ omega      OR  h ^ {r2} (y/G)^{d2}

    val alpha = CryptographicParameters.random()
    val omega = CryptographicParameters.random()

    this.x = cp.generator_g.modPow(alpha, cp.large_prime_p)
    this.y = if (vote)
      cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(cp.zkp_generator_G).mod(cp.large_prime_p)
    else
      cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(
        cp.zkp_generator_G.modInverse(cp.large_prime_p)
      )

    this.r1 = CryptographicParameters.random()
    this.r2 = CryptographicParameters.random()

    if (vote) { // is positive vote
      this.d1 = CryptographicParameters.random(511)    // we will use SHA-512 for zkp
      this.a1 = cp.generator_g.modPow(r1, cp.large_prime_p)
        .multiply(x.modPow(d1, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.b1 = cp.public_key_h.modPow(r1, cp.large_prime_p)
        .multiply(y.multiply(cp.zkp_generator_G).modPow(d1, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.a2 = cp.generator_g.modPow(omega, cp.large_prime_p)
      this.b2 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      this.d2 = getC().subtract(this.d1)
    }
    else { // vote is negative
      this.d2 = CryptographicParameters.random(511)
      this.a1 = cp.generator_g.modPow(omega, cp.large_prime_p)
      this.b1 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      this.a2 = cp.generator_g.modPow(r2, cp.large_prime_p)
        .multiply(x.modPow(d2, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.b2 = cp.public_key_h.modPow(r2, cp.large_prime_p)
        .multiply(cp.zkp_generator_G.modInverse(cp.large_prime_p).multiply(y).modPow(d2, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.d1 = getC().subtract(this.d2)
    }
  }

  private def getC() = {
    // take hash of voter string and poll details.  the hash of this constant is used to calculate the
    // parameters d1 and d2 that are used to produce the zero knowledge proof that the encrypted ballot is a
    // valid ballot for the poll in question
    val s =       // same as in the while paper : H(v_i||T)
    s"""${this.voter}
      |h=${cp.public_key_h}
      |x=${this.x}
      |y=${this.y}
      |a1=${this.a1}
      |b1=${this.b1}
      |a2=${this.a2}
      |b2=${this.b2}
      |""".stripMargin
    val shaBin = java.security.MessageDigest.getInstance("SHA-512").digest(s.getBytes("utf-8"))
    new BigInteger(1, shaBin)
  }


  /**
    * ZKP Verify integrity of the ballot
    */
  def verify(_outBuffer:ListBuffer[String]=null): Boolean = {
    val outBuffer = if(_outBuffer == null)
      new ListBuffer[String]()
    else
      _outBuffer
    outBuffer +=
      s"""C=${this.getC()}
         |d1=${this.d1}
         |d2=${this.d2}
         |C = d1 + d2 ?\n
         |""".stripMargin
    if(!getC().equals(
      this.d1.add(
        this.d2
      )
    ))
      return false

    outBuffer +=
      s"""a1=${this.a1}
         |g=${cp.generator_g}
         |r1=${this.r1}
         |x=${this.x}
         |d1=${this.d1}
         |a1 = g^r1 . x ^ d1 ?\n
         |""".stripMargin
    if(!cp.generator_g.modPow(r1, cp.large_prime_p).multiply(x.modPow(d1, cp.large_prime_p)).mod(cp.large_prime_p).equals(a1))
      return false

    outBuffer +=
      s"""b1=${this.b1}
         |h=${cp.public_key_h}
         |r1=${this.r1}
         |y=${this.y}
         |G=${cp.zkp_generator_G}
         |d1=${this.d1}
         |b1 = h^r1 (yG)^d1 ?\n
         |""".stripMargin
    val yG = y.multiply(cp.zkp_generator_G)
    if(!cp.public_key_h.modPow(r1, cp.large_prime_p).multiply(
        yG.modPow(d1, cp.large_prime_p)).mod(cp.large_prime_p).equals(b1))
      return false

    outBuffer +=
      s"""a2=${this.a2}
         |g=${cp.generator_g}
         |r2=${this.r2}
         |x=${this.x}
         |d2=${this.d2}
         |a2 = g^r2 x^d2 ?\n
         |""".stripMargin
    // TODO : fix
    // java.lang.ArithmeticException: BigInteger not invertible.
    // if(!cp.generator_g.modPow(r2, cp.large_prime_p).multiply(x.modPow(d2, cp.large_prime_p)).mod(cp.large_prime_p).equals(a2))
    //  return false

    outBuffer +=
      s"""b2=${this.b2}
         |h=${cp.public_key_h}
         |r2=${this.r2}
         |y=${this.y}
         |G=${cp.zkp_generator_G}
         |d2=${this.d2}
         |b2 = h^r2 (y/G)^d2 ?\n
         |""".stripMargin
    //val yByG = y.modInverse(cp.zkp_generator_G)
    //if(!cp.public_key_h.modPow(r2, cp.large_prime_p).multiply(
    //  yByG.modPow(d2, cp.large_prime_p)).mod(cp.large_prime_p).equals(b2))
    //  return false


    println("done")
    true
  }
}
