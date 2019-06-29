package com.newlibertie.pollster.impl

import org.scalatest.{FlatSpec, Matchers}

class PollSpec extends FlatSpec with Matchers {

  "Poll " should " be created " in {
    val p = Poll(
      """
        |{}
      """.stripMargin)
  }
}
