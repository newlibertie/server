package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class PollSpec  extends WordSpec with Matchers with ScalatestRouteTest {
  "The service" should {

    "support poll create  " in {
      Post("/poll") ~> Route.seal(Poll.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
    }


    "support poll get  " in {
      Get("/poll?id=123") ~> Route.seal(Poll.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
      Get("/poll") ~> Route.seal(Poll.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }


    "support poll update  " in {
      Put("/poll?id=123") ~> Route.seal(Poll.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
      Put("/poll") ~> Route.seal(Poll.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }


    "support poll delete  " in {
      Delete("/poll?id=123") ~> Route.seal(Poll.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
      Delete("/poll") ~> Route.seal(Poll.routes) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
  }
}
