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
      |  "p" : "123497",
      |  "g" : "31",
      |  "s" : "11115"
      |}
      """.stripMargin)

  "Ballot" should "have x and y" in {
    val b = new Ballot(p.cp, "test-voter")
    b.cast(true)
    val transcript = ListBuffer[String]();
    b.verify(transcript)   // shouldBe true
    transcript.foreach(line => println(line))
  }

  // try multiple runs
  "Ballot" should "pass 1K verification for negative vote" in {
    for (a <- 0 until 1024){
      val b = new Ballot(p.cp, "test-voter negative")
      b.cast(false)
      val transcript = ListBuffer[String]()
      b.verify(transcript) shouldBe true
      transcript.foreach(line => println(line))
    }
  }
  "Ballot" should "pass 1K verification for positive vote" in {
    for (a <- 0 until 1024){
      val b = new Ballot(p.cp, "test-voter negative")
      b.cast(true)
      val transcript = ListBuffer[String]()
      b.verify(transcript) shouldBe true
      transcript.foreach(line => println(line))
    }
  }
}
