package com.newlibertie.pollster.impl

import org.scalatest.{FlatSpec, Matchers}

class PollSpec extends FlatSpec with Matchers {

  "Poll " should " be created " in {
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
    p.p.id shouldEqual Some("abacadabra")
    p.p.title shouldEqual "abacadabra"
    p.p.tags shouldEqual List("abacadabra", "abacadabra2")
    p.cp.p.toString shouldEqual "123497"
    p.cp.h.toString shouldEqual "29942"
  }
}
