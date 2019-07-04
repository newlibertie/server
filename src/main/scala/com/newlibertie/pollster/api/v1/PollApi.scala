package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.newlibertie.pollster.impl.Poll

object PollApi {


  /**
    * Rest api
    * Create a Poll
    * Update a Poll (restricted)
    * Delete a Poll (restricted)
    * Open a Poll (restricted)
    * Close a Poll (restricted)
    * Get Poll Details
    * Compute Poll Result (restricted)
    */
  lazy val routes:Route = {
      path("poll") {
        get {
          parameters("id") { (id: String) =>
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                "{}"
              )
            )
          }
        } ~
        post {                    // Create a Poll
          entity(as[String]) {
            pollDefinition => {
              val poll = Poll(pollDefinition)
              Poll.write(poll)
              complete(s"received ${pollDefinition}")
            }
          }
        } ~
        put {
          parameters("id") { (id: String) =>
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                "{}"
              )
            )
          }
        } ~
        delete {
          parameters("id") { (id: String) =>
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                "{}"
              )
            )
          }
        }
      } ~
      path( "closePoll") {
        get {
          parameters("id") { (id: String) =>
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                "{}"
              )
            )
          }
        }
      } ~
      path( "computeResults") {
        get {
          parameters("id") { (id: String) =>
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                "{}"
              )
            )
          }
        }
      }
    }
}
