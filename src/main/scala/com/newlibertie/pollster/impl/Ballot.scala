package com.newlibertie.pollster.impl

import java.math.BigInteger

/**
  * Implementationv of ballot
  * @param parameters cryptographic parameters of the poll this ballot is about
  * @param voter String identifier identifying the voter
  * @param vote boolean vote, true = yay, false = nay
  */
class Ballot(cp:CryptographicParameters, voter:String, vote:Boolean) {

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

  val x = cp.generator_g.modPow(alpha, cp.large_prime_p)

  val y = if(vote)
    cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(cp.zkp_generator_G).mod(cp.large_prime_p)
  else
    cp.public_key_h.modPow(alpha, cp.large_prime_p).multiply(
      cp.zkp_generator_G.modInverse(cp.large_prime_p)
    )

  val r1 = CryptographicParameters.random()
  val r2 = CryptographicParameters.random()

  val (d1, d2) = zkp_params_d1d2()

  val a1, a2, b1, b2 =
    if(vote) {  // is positive vote
      val a1 = cp.generator_g.modPow(r1, cp.large_prime_p)
        .multiply( x.modPow(d1, cp.large_prime_p) )
        .mod(cp.large_prime_p)
      val b1 = cp.public_key_h.modPow(r1, cp.large_prime_p)
        .multiply( y.multiply(cp.zkp_generator_G).modPow(d1, cp.large_prime_p) )
        .mod(cp.large_prime_p)
      val a2 = cp.generator_g.modPow(omega, cp.large_prime_p)
      val b2 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      (a1, a2, b1, b2)
    }
    else {      // vote is negative
      val a1 = cp.generator_g.modPow(omega, cp.large_prime_p)
      val b1 = cp.public_key_h.modPow(omega, cp.large_prime_p)
      val a2 = cp.generator_g.modPow(r2, cp.large_prime_p)
        .multiply( x.modPow(d2, cp.large_prime_p) )
        .mod(cp.large_prime_p)
      val b2 =  cp.public_key_h.modPow(r2, cp.large_prime_p)
        .multiply( cp.zkp_generator_G.modInverse(cp.large_prime_p).multiply(y).modPow(d2, cp.large_prime_p) )
        .mod(cp.large_prime_p)
      (a1, a2, b1, b2)
    }

  private def zkp_params_d1d2() = {
    // take hash of voter string and poll details.  the hash of this constant is used to calculate the
    // parameters d1 and d2 that are used to produce the zero knowledge proof that the encrypted ballot is a
    // valid ballot for the poll in question
    val stringToHash = String.format("PollPublicKey(%s):Voter(%s)", cp.public_key_h.toString, voter)
    val sha256bin = java.security.MessageDigest.getInstance("SHA-256").digest(stringToHash.getBytes("utf-8"))
    val c = new BigInteger(1, sha256bin)

    var result:(BigInteger, BigInteger);
    while(true) {
      val d1 = CryptographicParameters.random(c.bitLength())
      if(d1.compareTo(c) < 0) {
        result = (d1, c.subtract(d1))
      }
    }
    result
  }
}
