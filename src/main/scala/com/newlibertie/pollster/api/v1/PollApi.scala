package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging

import com.newlibertie.pollster.DataAdapter
import com.newlibertie.pollster.impl.Poll
import net.liftweb.json._

object PollApi extends LazyLogging {
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
  lazy val routes: Route = path("poll") {
    get {
      parameters("id") { id: String =>
        val pollStr = DataAdapter.getPoll(id)
        pollStr match {
          case poll: String =>
            complete(HttpResponse(entity = poll))
          case -1 => complete(StatusCodes.NotFound)
          case _ => complete(StatusCodes.BadRequest)
        }
      }
    } ~
    post { // Create a Poll
      entity(as[String]) {
        pollDefinition => {
          val poll = Poll(pollDefinition)
          Poll.write(poll) match {
            case Some(id) => complete(StatusCodes.OK, List(`Content-Type`(`text/plain(UTF-8)`)), "id")
            case _ => complete(StatusCodes.NotFound)
              }
            }
          }
        } ~
    put { // update
      entity(as[String]) {
        pollDefinition => {
          try {
            val jValue = parse(pollDefinition)
            val pollJsonMap = jValue.values.asInstanceOf[Map[String, String]]
            val id = pollJsonMap.get("id").get
            //val id = parsedJson.get("id").asInstanceOf[String]
            //println(parsedJson)
            //val map = parse(mapStr, true)
            DataAdapter.updatePoll(id, pollJsonMap) match {
              case 1 => complete(StatusCodes.OK)
              case _ => complete(StatusCodes.NotFound)
            }
          }
          catch {
            case ex: Exception =>
              logger.error(s"failed to put the poll definition ${pollDefinition}", ex)
              complete(StatusCodes.BadRequest)

          }
        }
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
