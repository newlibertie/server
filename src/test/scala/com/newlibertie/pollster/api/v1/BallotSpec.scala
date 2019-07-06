package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class BallotSpec  extends WordSpec with Matchers with ScalatestRouteTest {
  "The service" should {

    "return a MethodNotAllowed error for PUT and GET requests" in {
      Put("/ballot") ~> Route.seal(Ballot.routes) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
      Get("/ballot") ~> Route.seal(Ballot.routes) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
    }

    "accept a posted ballot " in {
      Post("/ballot") ~> Route.seal(Ballot.routes) ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
