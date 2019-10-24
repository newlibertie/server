package com.newlibertie.pollster.impl

import org.scalatest.{FlatSpec, Matchers}

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
      |  "g" : "33",
      |  "s" : "11115"
      |}
      """.stripMargin)

  "Ballot" should "have x and y" in {
    val b = new Ballot(p.cp, true)
    b.x should not equal b.y
  }
}
