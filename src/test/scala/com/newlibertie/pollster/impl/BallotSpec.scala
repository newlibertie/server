package com.newlibertie.pollster.impl

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

class BallotSpec extends FlatSpec with Matchers {

  val p = Poll(
    """
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
      |  "p" : "123493",
      |  "g" : "31",
      |  "s" : "11113"
      |}
      """.stripMargin)

  "Ballot" should "pass verification for positive vote" in {
    val b = new Ballot(p.cp, "test-voter")
    b.cast(true)
    val transcript = ListBuffer[String]();
    b.verify(transcript) shouldBe true
    transcript.foreach(line => println(line))
  }

  "Ballot" should "pass verification for negative vote" in {
    val b = new Ballot(p.cp, "test-voter")
    b.cast(false)
    val transcript = ListBuffer[String]();
    b.verify(transcript) shouldBe true
    transcript.foreach(line => println(line))
  }
}
