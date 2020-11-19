package com.newlibertie.pollster.impl

import java.math.BigInteger    // TODO : consider java

import scala.collection.mutable.ListBuffer
/*!! Important security note : The precise ordering and content of each 
variable computed below must be regarded as a *security sensitive change* 
where a minor slip may result in total loss of the security properties we 
set out to achieve as described in the white paper https://github.com/newlibertie/voting-protocol/blob/master/vp.pdf
Consequently, such a change can also be considered as an attack.
The deployed sha of this file should be watched vigorously !!*/

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

  private def assertInversesExist(): Boolean = {
    if( cp.large_prime_p.gcd(cp.generator_g).equals(BigInteger.ONE) && y.gcd(cp.zkp_generator_G).equals(BigInteger.ONE)) {
      true
    } else {
      false
    }
  }

  /**
    * Cast a vote - populates all parameters to become a verifiable ballot
    * @param vote boolean vote, true = yay, false = nay
    */
    // TODO: Can be made to have only-once semantics (in that case there may
    // TODO: also save the metadata - where this ballot was casted, what UTC ts,
    // TODO: etc - that can serve other purposes when aggregated at scale)

  def cast(vote:Boolean): Unit = {
    // Voter votes m^0 or m^1
    // Ballot is a tuple (x,y,a1,b1,a2,b2)
    //    x    g^alpha,
    //    y    h^alpha G      OR  h^alpha / G
    //    a1   g^r1 x^{d1}    OR  g ^ omega
    //    b1   h^r1 (yG)^{d1} OR  h ^ omega
    //    a2   g ^ omega      OR  g ^ {r2} x ^ {d2}
    //    b2   h ^ omega      OR  h ^ {r2} (y/G)^{d2}
    val alpha = CryptographicParameters.random(CryptographicParameters.BITS)
    val omega = CryptographicParameters.random(4*CryptographicParameters.BITS)

    this.x = cp.generator_g.modPow(alpha, cp.large_prime_p)
    this.y = if (vote)
      cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(cp.zkp_generator_G).mod(cp.large_prime_p)
    else
      cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(
        cp.zkp_generator_G.modInverse(cp.large_prime_p)
      )
    if(!this.assertInversesExist()) {
      cast(vote)
    } else if (vote) { // is positive vote
      this.d1 = CryptographicParameters.random(CryptographicParameters.BITS)
      this.r1 = CryptographicParameters.random(CryptographicParameters.BITS)
      this.a1 = cp.generator_g.modPow(r1, cp.large_prime_p)
        .multiply(x.modPow(d1, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.b1 = cp.public_key_h.modPow(r1, cp.large_prime_p)
        .multiply(y.multiply(cp.zkp_generator_G).modPow(d1, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.a2 = cp.generator_g.modPow(omega, cp.large_prime_p)
      this.b2 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      val c = getC()
      this.d2 = c.subtract(this.d1)
      this.r2 = omega.subtract(alpha.multiply(this.d2))
    }
    else { // vote is negative
      this.d2 = CryptographicParameters.random(CryptographicParameters.BITS)
      this.r2 = CryptographicParameters.random(CryptographicParameters.BITS)
      this.a1 = cp.generator_g.modPow(omega, cp.large_prime_p)
      this.b1 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      this.a2 = cp.generator_g.modPow(r2, cp.large_prime_p)
        .multiply(x.modPow(d2, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.b2 = cp.public_key_h.modPow(r2, cp.large_prime_p)
        .multiply(cp.zkp_generator_G.modInverse(cp.large_prime_p).multiply(y).modPow(d2, cp.large_prime_p))
        .mod(cp.large_prime_p)
      val c = getC()
      this.d1 = c.subtract(this.d2)
      this.r1 = omega.subtract(alpha.multiply(d1))
    }
  }

  private def getC() = {
    // take hash of <voter string, poll details>.  the hash of this content is used to calculate the
    // parameters d1 and d2 - which are used to produce the zero knowledge proof that the encrypted
    // ballot is a valid ballot for the given poll
    //
    // this.voter     is the name of the voter - eg: https://www.facebook.com/vpathak000
    // the remaining parameters can be described as follows:
    // h              poll public key - which is also the identifier for the polls
    // x              some random numbers,  refer to the white paper
    //
    val s =       // the "formula" for the content hash is same as in the while paper : H(v_i||T)
    s"""${this.voter}
      |h=${cp.public_key_h}
      |x=${this.x}
      |y=${this.y}
      |a1=${this.a1}
      |b1=${this.b1}
      |a2=${this.a2}
      |b2=${this.b2}
      |""".stripMargin
    val shaBin = java.security.MessageDigest.getInstance ("SHA-512").digest(s.getBytes("utf-8"))
    println(s"C ${new BigInteger(1, shaBin).mod(new BigInteger("2").pow(CryptographicParameters.BITS))} from H( ${s} ) ")
    new BigInteger(1, shaBin).mod(new BigInteger("2").pow(2*CryptographicParameters.BITS))
  }


  /**
    * ZKP Verify integrity of the ballot
    */
  def verify(_outBuffer:ListBuffer[String]=null): Boolean = {
    // TODO : fix design-ish issue.  why we would allowed 
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
    if(!cp.generator_g.modPow(r2, cp.large_prime_p).multiply(x.modPow(d2, cp.large_prime_p)).mod(cp.large_prime_p).equals(a2))
      return false

    outBuffer +=
      s"""b2=${this.b2}
         |h=${cp.public_key_h}
         |r2=${this.r2}
         |y=${this.y}
         |G=${cp.zkp_generator_G}
         |d2=${this.d2}
         |b2 = h^r2 (y/G)^d2 ?\n
         |""".stripMargin

    val yByG = y.multiply(cp.zkp_generator_G.modInverse(cp.large_prime_p)).mod(cp.large_prime_p)      // y/G
    if( cp.public_key_h.modPow(r2, cp.large_prime_p).multiply(yByG.modPow(d2, cp.large_prime_p)).mod(cp.large_prime_p) != b2 )
      return false

    println(outBuffer.toString())
    println("done")
    true
  }
}
