package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
//import com.newlibertie.pollster.DataAdapter.logger
import com.newlibertie.pollster.impl.Poll
import org.scalatest.{Matchers, WordSpec}
import com.typesafe.scalalogging.LazyLogging

class PollApiSpec extends WordSpec with Matchers with ScalatestRouteTest with LazyLogging {
  "The service" should {

    "support poll create  " in {
      Post("/poll").withEntity(
        """
          |{
          |  "title":"abacadabra",
          |  "tags":["abacadabra", "abacadabra2"],
          |  "creator_id":"abacadabra",
          |  "opening_ts": "2019-07-01T02:51:00Z" ,
          |  "closing_ts": "2019-07-01T02:51:00Z" ,
          |  "poll_type":"SIMPLE",
          |  "poll_spec":"abacadabra"
          |}
          |
        """.stripMargin) ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[String].indexOf("id") shouldBe 0   // TODO Can json parse
        contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
      }
    }

    "support poll get  " in {
      val pollDefinitionStr = """
      |{
        |  "title":"abacadabra",
        |  "tags":["abacadabra", "abacadabra2"],
        |  "creator_id":"abacadabra",
        |  "opening_ts": "2019-07-01T02:51:00Z" ,
        |  "closing_ts": "2019-07-01T02:51:00Z" ,
        |  "poll_type":"SIMPLE",
        |  "poll_spec":"abacadabra"
        |}
      """.stripMargin

      val poll = Poll(pollDefinitionStr)
      val pollId: String = poll.create() match {
        case None => ""//Or handle the lack of a value another way: throw an error, etc.
        case Some(s: String) => s //return the string to set your value
      }

      Get(s"/poll?id=$pollId") ~> Route.seal(PollApi.routes) ~> check {
        logger.info("entityAs[String]: " + entityAs[String])
        status shouldEqual StatusCodes.OK
        entityAs[String].length()  should be > 10 // TODO Can json parse
      }
      Get(s"/poll?id=NOT-THERE$pollId") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "support poll update  " in {
      Put("/poll").withEntity(
        """
          |{
          |  "id":"d5203114-0504-4067-9de9-10ea0cf50785",
          |  "title":"abacadabra-updated",
          |  "tags":["abacadabra3", "abacadabra4"],
          |  "creator_id":"abacadabra",
          |  "opening_ts": "2019-08-01T02:51:00Z" ,
          |  "closing_ts": "2019-10-01T02:51:00Z" ,
          |  "poll_type":"MULTIPLE_CHOICE",
          |  "poll_spec":"abacadabra-updated"
          |}
        """.stripMargin) ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "support poll delete  " in {
      Delete("/poll?id=123") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
      Delete("/poll") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
  }
}
