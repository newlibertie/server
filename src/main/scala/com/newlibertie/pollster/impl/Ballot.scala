package com.newlibertie.pollster.impl

/**
  * Implementationv of ballot
  * @param parameters cryptographic parameters of the poll this ballot is about
  * @param vote boolean vote, true = yay, false = nay
  */
class Ballot(cp:CryptographicParameters, vote:Boolean) {

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

  val a1, a2, b1, b2 =
    if(vote) {  // is positive vote
      val r1 = CryptographicParameters.random()
      val d1 = CryptographicParameters.random()
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
      val r2 = CryptographicParameters.random()
      val d2 = CryptographicParameters.random()
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
}
