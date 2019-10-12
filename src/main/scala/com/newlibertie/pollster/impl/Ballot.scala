package com.newlibertie.pollster.impl

/**
  * Implementationv of ballot
  * @param parameters cryptographic parameters of the poll this ballot is about
  * @param vote boolean vote, true = yay, false = nay
  */
class Ballot(cp:CryptographicParameters, vote:Boolean) {

  // Voter votes m^0 or m^1
  // Ballot is a tuple (x,y,a1,b1,a2,b2) -> (g^alpha,
  //                                        h^alpha)

  val alpha = CryptographicParameters.random()
  val x = cp.generator_g.modPow(alpha, cp.large_prime_p)
  val y = cp.public_key_h.modPow(alpha, cp.large_prime_p)
  // todo: b1,a1,b2,a2 (vote)
}
