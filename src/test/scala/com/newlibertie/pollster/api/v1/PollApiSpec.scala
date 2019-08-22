package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class PollApiSpec  extends WordSpec with Matchers with ScalatestRouteTest {
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
        entityAs[String].indexOf("id") shouldBe 2   // TODO Can json parse
        // TODO : contentType should ===(ContentTypes.`application/json`)
      }
    }


    "support poll get  " in {
      Get("/poll?id=123") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
      Get("/poll") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }


    "support poll update  " in {
      Put("/poll?id=123") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
      Put("/poll") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }


    "support poll delete  " in {
      Delete("/poll?id=123") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
      Delete("/poll") ~> Route.seal(PollApi.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
  }
}
