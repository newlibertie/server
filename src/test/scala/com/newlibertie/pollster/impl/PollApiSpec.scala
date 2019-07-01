package com.newlibertie.pollster.impl

import org.scalatest.{FlatSpec, Matchers}

class PollApiSpec extends FlatSpec with Matchers {

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
        |  "poll_spec":"abacadabra"
        |}
      """.stripMargin)
    println(p)
  }
}
