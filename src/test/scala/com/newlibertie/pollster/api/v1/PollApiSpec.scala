package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
//import com.newlibertie.pollster.DataAdapter.logger
import com.newlibertie.pollster.impl.Poll
import org.scalatest._
import com.typesafe.scalalogging.LazyLogging

@Ignore
class PollApiSpec extends WordSpec with Matchers with ScalatestRouteTest with LazyLogging {
  "The service" should {

    "support poll create  " in {
      Post("/poll").withEntity( // test a success case
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
      Post("/poll").withEntity( // testing for invalid JSON string input
        """
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
        status shouldEqual StatusCodes.BadRequest
      }
    }

    "support poll get create update delete " in {
      // first create a new Poll, use its id to retrieve it back
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

      val pollId: String = Poll(pollDefinitionStr).create() match {
        case Some(s: String) => s //return the string to set your value
        case _ => logger.info("Failed to create a Poll for get test.")
          "Not created"
      }
      // test the get success case with pollId of the just create Poll above
      Get(s"/poll?id=$pollId") ~> Route.seal(PollApi.routes) ~> check {
        logger.info("entityAs[String]: " + entityAs[String])
        status shouldEqual StatusCodes.OK
        entityAs[String].length()  should be > 10 // TODO Can json parse
      }
      // test the get failure case on non-existing PollId
      Get(s"/poll?id=NOT-THERE$pollId") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
      Put("/poll").withEntity(
        s"""
          |{
          |  "id":"${pollId}",
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
      // test the delete success case with the pollId of the created Poll
      Delete(s"/poll?id=$pollId") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    // test bad input case of missing id in the Json string
    "support poll update  " in {
      Put("/poll").withEntity(
        """
          |{
          |  "id":"NOT-THERE",
          |  "title":"abacadabra-updated",
          |  "tags":["abacadabra3", "abacadabra4"],
          |  "creator_id":"abacadabra",
          |  "opening_ts": "2019-08-01T02:51:00Z" ,
          |  "closing_ts": "2019-10-01T02:51:00Z" ,
          |  "poll_type":"MULTIPLE_CHOICE",
          |  "poll_spec":"abacadabra-updated"
          |}
        """.stripMargin) ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    // test the bad parameter value and absent parameter cases
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
