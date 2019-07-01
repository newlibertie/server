package com.newlibertie.pollster.impl

import java.math.BigInteger
import java.util.Date

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

case class PollDetails(
                        id:String,
                        title:String,
                        tags:List[String],
                        creator_id:String,
                        opening_ts:Date,
                        closing_ts:Date,
                        creation_ts:Date,
                        last_modification_ts:Date,
                        poll_type:String,
                        poll_spec:String,
                      )

case class PollParams  (
                       p:BigInteger,        // large prime
                       g:BigInteger,        //  generator
                       s:BigInteger,        //  secret key
                       pollDetails:String   // jsom string, title,
                     )
{
  val h =  g.modPow(s, p)           // public key
  implicit val formats = DefaultFormats
  val jValue = parse(pollDetails)
  val pollSpec = jValue.extract[PollDetails]
}

class Poll(_params:PollParams) {
  val params = _params

}
