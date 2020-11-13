package com.newlibertie.pollster.impl

import java.math.BigInteger

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

class BallotSpec extends FlatSpec with Matchers {
  final val isdbg = false
  val big_p: BigInteger = CryptographicParameters.probablePrime()
  val p: Poll = Poll(
    Predef.augmentString(
      x = if (isdbg)
        s"""
      |{
      |  "id":"abacadabra",
      |  "title":"abacadabra",
      |  "tags":["abacadabra", "abacadabra2"],
      |  "creator_id":"abacadabra",
      |  "opening_ts": "2019-07-01T02:51:00Z" ,
      |  "closing_ts": "2019-07-01T02:51:00Z" ,
      |  "creation_ts": "2019-07-01T02:51:00Z" ,
      |  "last_modification_ts": "2019-07-01T02:51:00Z" ,
      |  "poll_type":"abacadabra",
      |  "poll_spec":"abacadabra",
      |  "p" : "3032992489",
      |  "g" : "1199476689",
      |  "s" : "315998446"
      |}
      """
    else{
        s"""
           |{
           |  "id":"abacadabra",
           |  "title":"abacadabra",
           |  "tags":["abacadabra", "abacadabra2"],
           |  "creator_id":"abacadabra",
           |  "opening_ts": "2019-07-01T02:51:00Z" ,
           |  "closing_ts": "2019-07-01T02:51:00Z" ,
           |  "creation_ts": "2019-07-01T02:51:00Z" ,
           |  "last_modification_ts": "2019-07-01T02:51:00Z" ,
           |  "poll_type":"abacadabra",
           |  "poll_spec":"abacadabra",
           |  "p" : "$big_p",
           |  "g" : "${CryptographicParameters.random().mod(big_p)}",
           |  "s" : "${CryptographicParameters.random().mod(big_p)}"
           |}
      """
      }).stripMargin)

  "Ballot" should "pass verification for positive vote" in {
    val b = new Ballot(p.cp, "test-voter positive")
    b.cast(true)
    val transcript = ListBuffer[String]()
    b.verify(transcript) shouldBe true
    transcript.foreach(line => println(line))
  }

  "Ballot" should "pass verification for negative vote" in {
    val b = new Ballot(p.cp, "test-voter negative")
    b.cast(false)
    val transcript = ListBuffer[String]()
    b.verify(transcript) shouldBe true
    transcript.foreach(line => println(line))
  }
}
