package com.newlibertie.pollster.impl

import java.math.BigInteger

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

  final val isdbg = false
  var x,y :BigInteger = _

  var a1, b1, a2, b2 :BigInteger = _

  var d1, d2, r1, r2 :BigInteger = _
  var c_val:BigInteger = _

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

    val alpha = if (isdbg)
      new BigInteger("11774")
    else
      CryptographicParameters.random(CryptographicParameters.BITS/2).mod(cp.large_prime_p)
    this.c_val = if (isdbg)
      new BigInteger("257600")
    else
      cp.large_prime_p.divide(alpha).subtract(BigInteger.ONE)

    // TODO : insecure, delete please
    println(s"voter\t${this.voter}")
    println(s"alpha\t$alpha")

    this.x = cp.generator_g.modPow(alpha, cp.large_prime_p)
    if (vote) { // is positive vote
      this.y = cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(cp.zkp_generator_G).mod(cp.large_prime_p)
      this.d1 = if (isdbg)
        new BigInteger("63832")
      else
        CryptographicParameters.random(CryptographicParameters.BITS/2).mod(this.c_val)   // TODO : adjust and check if "we will use SHA-512 for zkp" can work with c = d1 + d2
      this.d2 = this.c_val.subtract(this.d1)
      val omega = if (isdbg)
        new BigInteger("2281424433")
      else
        alpha.multiply(this.d1.max(this.d2)).add(BigInteger.ONE)
      println(s"omega\t$omega")
      this.r1 = if (isdbg)
        new BigInteger("123456")
      else
        CryptographicParameters.random(CryptographicParameters.BITS).mod(cp.large_prime_p)
      this.a1 = cp.generator_g.modPow(r1, cp.large_prime_p)
        .multiply(x.modPow(d1, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.b1 = cp.public_key_h.modPow(r1, cp.large_prime_p)
        .multiply(y.multiply(cp.zkp_generator_G).modPow(d1, cp.large_prime_p))
        .mod(cp.large_prime_p)
      this.a2 = cp.generator_g.modPow(omega, cp.large_prime_p)
      this.b2 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      this.r2 = omega.subtract(alpha.multiply(this.d2).mod(omega))
    }
    else { // vote is negative
      this.y = cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(
          cp.zkp_generator_G.modInverse(cp.large_prime_p)
        ).mod(cp.large_prime_p)
      this.d2 = if (isdbg)
        new BigInteger("193768")
      else
        CryptographicParameters.random(CryptographicParameters.BITS).mod(this.c_val)
      this.d1 = this.c_val.subtract(this.d2)
      val omega = if (isdbg)
        new BigInteger("2281424433")
      else
        alpha.multiply(this.d1.max(this.d2)).add(BigInteger.ONE)

      println(s"omega\t$omega")
      this.r2 = if (isdbg)
        new BigInteger("123456")
      else
        CryptographicParameters.random(CryptographicParameters.BITS).mod(cp.large_prime_p)
      this.a1 = cp.generator_g.modPow(omega, cp.large_prime_p)
      this.b1 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      this.a2 = cp.generator_g.modPow(r2, cp.large_prime_p)
        .multiply(x.modPow(d2, cp.large_prime_p))
        .mod(cp.large_prime_p)

      val yByG = cp.zkp_generator_G.modInverse(cp.large_prime_p).multiply(y).mod(cp.large_prime_p)
      val hr2 = cp.public_key_h.modPow(r2, cp.large_prime_p)
      val yByGpowd2 = yByG.modPow(d2, cp.large_prime_p)
      val hr2yByGpowd2 = hr2.multiply(yByGpowd2).mod(cp.large_prime_p)
      this.b2 = hr2yByGpowd2
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
    println(s"c_val\t${this.c_val}")
    println(s"d1\t${this.d1}")
    println(s"d2\t${this.d2}")
    println(s"r1\t${this.r1}")
    println(s"r2\t${this.r2}")
    println(s"x\t${this.x}")
    println(s"y\t${this.y}")
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
      s"""C=${this.c_val}
         |d1=${this.d1}
         |d2=${this.d2}
         |C = d1 + d2 ?\n
         |""".stripMargin

    try {
      val c = this.c_val
      val shouldBec = this.d1.add(this.d2)
      if (!c.equals(shouldBec)) {
        println(s"shouldBec\t$shouldBec")
        return false
      }

      outBuffer +=
        s"""a1=${this.a1}
           |g=${cp.generator_g}
           |r1=${this.r1}
           |x=${this.x}
           |d1=${this.d1}
           |a1 = g^r1 . x ^ d1 ?\n
           |""".stripMargin
      val gpowr1 = cp.generator_g.modPow(r1, cp.large_prime_p)
      val xpowd1 = x.modPow(d1, cp.large_prime_p)
      val prod = gpowr1.multiply(xpowd1).mod(cp.large_prime_p)
      println(s"gpowr1\t$gpowr1")
      println(s"xpowd1\t$xpowd1")
      println(s"prod\t$prod")
      if (!prod.equals(a1)){
        println(s"a1 != prod")
        return false
      }
//      if (!cp.generator_g.modPow(r1, cp.large_prime_p).multiply(x.modPow(d1, cp.large_prime_p)).mod(cp.large_prime_p).equals(a1)) {
//        println(s"a1 != $a1")
//        return false
//      }

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
        yG.modPow(d1, cp.large_prime_p)).mod(cp.large_prime_p).equals(b1)) {
        println(s"b1 !=\t$b1")
        return false
      }

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
        return false                        // TODO : debug using algebra proof in voting protocol paper
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
