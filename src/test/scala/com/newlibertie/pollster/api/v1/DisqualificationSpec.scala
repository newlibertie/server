package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class DisqualificationSpec  extends WordSpec with Matchers with ScalatestRouteTest {
  "The service" should {

    "return a MethodNotAllowed error for PUT and GET requests" in {
      Put("/disqualification") ~> Route.seal(Disqualification.routes) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET, POST"
      }
    }

    "accept a posted disqualification " in {
      Post("/disqualification") ~> Route.seal(Disqualification.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "return the current disqualification " in {
      Get("/disqualification?id=1") ~> Route.seal(Disqualification.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
