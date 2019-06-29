package com.newlibertie.pollster.impl

import java.math.BigInteger
import net.liftweb.json._

object Poll {

  val BITS = 1000

  def apply(params: PollParams): Poll = new Poll(params)

  def apply(pollDetails:String): Poll = {
    import java.security.SecureRandom;
    val random = new SecureRandom()
     new Poll(
      PollParams(
        BigInteger.probablePrime(BITS, random),
        new BigInteger(BITS, random),
        new BigInteger(BITS, random),
        pollDetails)
    )
  }

  // TODO :
  def write() = {

  }

  def read() = {

  }
}

case class PollParams(
                       p:BigInteger,        // large prime
                       g:BigInteger,        //  generator
                       s:BigInteger,        //  secret key
                       pollDetails:String   // jsom string, title,
                     )
{
  implicit val formats = DefaultFormats
  val jValue = parse(pollDetails)
}

class Poll(params:PollParams) {


}
