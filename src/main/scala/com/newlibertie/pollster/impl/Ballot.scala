package com.newlibertie.pollster.impl

import java.math.BigInteger    // TODO : consider java

import scala.collection.mutable.ListBuffer

/**
  * Implementation of ballot
  *
  * A ballot object is empty but permanently associated with a specific poll and
  * a specific voter upon construction
  *
  * The two ways to make it a valid ballot are as follows:
  * 1. Using the cast(vote:Boolean) method to populate its internal fields, or
  * 2. Instantiate it from pre-computed fields.  In this scenario, the implementation
  * will verify the ballot as can anyway be done using the verify() method.
  * @param cp cryptographic parameters of the poll this ballot is about
  * @param voter String identifier identifying the voter
  */
class Ballot(cp:CryptographicParameters, voter:String) {

  var x,y :BigInteger = _

  var a1, b1, a2, b2 :BigInteger = _

  var d1, d2, r1, r2 :BigInteger = _

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

    val alpha = CryptographicParameters.random(CryptographicParameters.BITS).mod(cp.large_prime_p)
    val omega = CryptographicParameters.random(CryptographicParameters.BITS).mod(cp.large_prime_p)
      // TODO : insecure, delete please
    println(s"alpha is $alpha")
    println(s"omega is $omega")

    this.x = cp.generator_g.modPow(alpha, cp.large_prime_p)
    this.y = if (vote) {
      cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(
        cp.zkp_generator_G).mod(cp.large_prime_p)
    } else {
      cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(
        cp.zkp_generator_G.modInverse(cp.large_prime_p)
      ).mod(cp.large_prime_p)
    }

    if (vote) { // is positive vote
      this.d1 = CryptographicParameters.random(CryptographicParameters.BITS).mod(cp.large_prime_p)   // TODO : adjust and check if "we will use SHA-512 for zkp" can work with c = d1 + d2
//      this.d1 = new BigInteger("63832").mod(cp.large_prime_p)
//      this.d1 = CryptographicParameters.random(CryptographicParameters.BITS).mod(this.c_val) // TODO : adjust and check if "we will use SHA-512 for zkp" can work with c = d1 + d2

      this.r1 = CryptographicParameters.random(CryptographicParameters.BITS).mod(cp.large_prime_p)
      this.a1 = cp.generator_g.modPow(r1, cp.large_prime_p)
        .multiply(x.modPow(d1, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.b1 = cp.public_key_h.modPow(r1, cp.large_prime_p)
        .multiply(y.multiply(cp.zkp_generator_G).mod(cp.large_prime_p).modPow(d1, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.a2 = cp.generator_g.modPow(omega, cp.large_prime_p)
      this.b2 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      val c = getC  //.mod(cp.large_prime_p)
//      val c = new BigInteger("257600").mod(cp.large_prime_p)
      this.d2 = c.subtract(this.d1).mod(cp.large_prime_p)
//      this.d2 = this.c_val.subtract(this.d1)
//      this.r2 = omega.subtract(alpha.multiply(this.d2)).mod(cp.large_prime_p)
//      this.r2 = omega.subtract(alpha.multiply(this.d2).mod(cp.large_prime_p))
      this.r2 = omega.subtract(alpha.multiply(this.d2).mod(omega))
    }
    else { // vote is negative
      this.d2 = CryptographicParameters.random(CryptographicParameters.BITS).mod(cp.large_prime_p)
//      this.d2 = new BigInteger("193768").mod(cp.large_prime_p)
//      this.d2 = CryptographicParameters.random(CryptographicParameters.BITS).mod(this.c_val)
      this.r2 = CryptographicParameters.random(CryptographicParameters.BITS).mod(cp.large_prime_p)
      this.a1 = cp.generator_g.modPow(omega, cp.large_prime_p)
      this.b1 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      this.a2 = cp.generator_g.modPow(r2, cp.large_prime_p)
        .multiply(x.modPow(d2, cp.large_prime_p))
        .mod(cp.large_prime_p)
//      this.b2 = cp.public_key_h.modPow(r2, cp.large_prime_p)
//        .multiply(cp.zkp_generator_G.modInverse(cp.large_prime_p)
//          .multiply(y).modPow(d2, cp.large_prime_p))
//        .mod(cp.large_prime_p)
      this.b2 = cp.public_key_h.modPow(r2, cp.large_prime_p)
        .multiply(cp.zkp_generator_G.modInverse(cp.large_prime_p)
          .multiply(y).mod(cp.large_prime_p).modPow(d2, cp.large_prime_p))
//          .multiply(y).modPow(d2, cp.large_prime_p))
        .mod(cp.large_prime_p)
      val c = getC  //.mod(cp.large_prime_p)
//      val c = new BigInteger("257600").mod(cp.large_prime_p)
      this.d1 = c.subtract(this.d2).mod(cp.large_prime_p)
//      this.r1 = omega.subtract(alpha.multiply(this.d1)).mod(cp.large_prime_p)
//      this.r1 = omega.subtract(alpha.multiply(this.d1).mod(cp.large_prime_p))
      this.r1 = omega.subtract(alpha.multiply(this.d1).mod(omega))
    }
    println(s"large_prime_p\t${this.cp.large_prime_p}")
    println(s"generator_g\t${this.cp.generator_g}")
    println(s"private_key_s\t${this.cp.private_key_s}")
    println(s"public_key_h\t${this.cp.public_key_h}")
    println(s"zkp_generator_G\t${this.cp.zkp_generator_G}")
    println(s"a1\t${this.a1}")
    println(s"a2\t${this.a2}")
    println(s"b1\t${this.b1}")
    println(s"b2\t${this.b2}")
    println(s"c_val\t${this.getC}")
    println(s"d1\t${this.d1}")
    println(s"d2\t${this.d2}")
    println(s"r1\t${this.r1}")
    println(s"r2\t${this.r2}")
    println(s"x\t${this.x}")
    println(s"y\t${this.y}")
  }

  private def getC = {
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
    val shaBin = java.security.MessageDigest.getInstance("SHA-512").digest(s.getBytes("utf-8"))
    println(s"string to hash $s -> ${new BigInteger(1, shaBin).toString}")
    new BigInteger(1, shaBin) //.mod(cp.large_prime_p)
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
//    val c = new BigInteger("257600").mod(cp.large_prime_p)
    outBuffer +=
      //    s"""C=$c
      s"""C=${this.getC}
         |d1=${this.d1}
         |d2=${this.d2}
         |C = d1 + d2 ?\n
         |""".stripMargin

    try {
      val c = getC.mod(cp.large_prime_p)
      val shouldBec = this.d1.add(this.d2).mod(cp.large_prime_p)
      if (!c.equals(shouldBec))
        return false

      outBuffer +=
        s"""a1=${this.a1}
           |g=${cp.generator_g}
           |r1=${this.r1}
           |x=${this.x}
           |d1=${this.d1}
           |a1 = g^r1 . x ^ d1 ?\n
           |""".stripMargin
      println(s"gpowr1\t$gpowr1")
      println(s"xpowd1\t$xpowd1")
      println(s"prod\t$prod")
      if (!cp.generator_g.modPow(r1, cp.large_prime_p).multiply(x.modPow(d1, cp.large_prime_p)).mod(cp.large_prime_p).equals(a1)) {
        println(s"a1 != prod")
        return false
      }

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
      if (!cp.public_key_h.modPow(r1, cp.large_prime_p).multiply(
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
      val gr2 = cp.generator_g.modPow(r2, cp.large_prime_p)
      val xd2 = x.modPow(d2, cp.large_prime_p)
      val shouldBea2 = gr2.multiply(xd2).mod(cp.large_prime_p)
      println(s"gr2\t$gr2")
      println(s"xd2\t$xd2")
      println(s"shouldBea2\t$shouldBea2")
      if (!shouldBea2.equals(a2)) {
        println(s"shouldBea2 != a2")
        return false // TODO : debug using algebra proof in voting protocol paper
      }

      outBuffer +=
        s"""b2=${this.b2}
           |h=${cp.public_key_h}
           |r2=${this.r2}
           |y=${this.y}
           |G=${cp.zkp_generator_G}
           |d2=${this.d2}
           |b2 = h^r2 (y/G)^d2 ?\n
           |""".stripMargin
      val yByG = cp.zkp_generator_G.modInverse(cp.large_prime_p).multiply(y).mod(cp.large_prime_p)
      val hr2 = cp.public_key_h.modPow(r2, cp.large_prime_p)
      val yByGpowd2 = yByG.modPow(d2, cp.large_prime_p)
      val hr2yByGpowd2 = hr2.multiply(yByGpowd2).mod(cp.large_prime_p)
      println(s"yByG\t$yByG")
      println(s"hr2\t$hr2")
      println(s"yByGpowd2\t$yByGpowd2")
      println(s"hr2yByGpowd2\t$hr2yByGpowd2")
      if (!hr2yByGpowd2.equals(b2)) {
        println(s"hr2yByGpowd2 != b2")
        return false
      }
//      val yByG = y.modInverse(cp.zkp_generator_G)
//      if (!cp.public_key_h.modPow(r2, cp.large_prime_p).multiply(
//        yByG.modPow(d2, cp.large_prime_p)).mod(cp.large_prime_p).equals(b2))
//        return false
      //println(outBuffer.toString())
      //println("done")
      true
    }
    catch {
      case ex : Throwable =>
        println(s"Failed to verify because of exception ${ex.toString}")
        false
    }
  }
}
