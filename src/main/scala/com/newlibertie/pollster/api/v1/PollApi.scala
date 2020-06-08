package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import com.newlibertie.pollster.errorenum.{ApplicationError, DatabaseError, DatabaseErrorEnum}
import com.newlibertie.pollster.impl.Poll
import com.typesafe.scalalogging.LazyLogging

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
        try {
          Poll.read(id) match {
            case p: Poll => complete(HttpResponse(entity = p.toJsonString))
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
        catch {
          case DatabaseError.RecordNotFound => complete(StatusCodes.NotFound)
          case ApplicationError.ExceptionError => complete(StatusCodes.InternalServerError)
          case _: Throwable => complete(StatusCodes.InternalServerError)
        }
      }
    } ~
    post { // Create a Poll
      entity(as[String]) {
        pollDefinition => {
          try {
            val poll = Poll(pollDefinition)
            poll.create match {
              case Some(id) => complete(StatusCodes.OK, List(`Content-Type`(`text/plain(UTF-8)`)), "id")
              case _ => complete(StatusCodes.NotFound)
            }
          }
          catch {
            case ex: Exception =>
              logger.error(s"failed to put the poll definition $pollDefinition", ex)
              complete(StatusCodes.BadRequest)
          }
        }
      }
    } ~
    put { // update
      entity(as[String]) {
        pollDefinition => {
          try {
            Poll(pollDefinition) match {
              case poll: Poll => poll.update() match {
                case 1 => complete(StatusCodes.OK)
                case 0 => complete(StatusCodes.NotFound)
              }
              case _ => complete(StatusCodes.BadRequest)
            }
          }
          catch {
            case ex: Exception =>
              logger.error(s"failed to put the poll definition $pollDefinition", ex)
              complete(StatusCodes.BadRequest)
          }
        }
      }
    } ~
    delete {
      parameters("id") { id: String =>
        _commonCode(id, "deletePoll")
      }
    }
  }~
  path("closePoll") {
    get {
      parameters("id") { id: String =>
        _commonCode(id, "closePoll")
      }
    }
  } ~
  path("computeResults") {
    get {
      parameters("id") { id: String =>
        complete(
          HttpEntity(
            ContentTypes.`application/json`,
            "{}"
          )
        )
      }
    }
  }
  def _commonCode(id:String, methodName:String): StandardRoute = {
    try {
      Poll.read(id) match {
        case p: Poll => if (if (methodName == "closePoll") p.canClose() else p.canDelete()) {
          try {
            (if (methodName == "closePoll") p.closePoll() else p.deletePoll()) match {
              case 1 => complete(StatusCodes.OK)
              case 0 => complete(StatusCodes.NotFound)
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
          catch {
            case ex: Exception =>
              logger.error(s"failed to delete the poll with id : $id", ex)
              complete(StatusCodes.InternalServerError)
          }
        }
        else complete(StatusCodes.Unauthorized)
      }
    }
    catch {
      case DatabaseError.RecordNotFound => complete(StatusCodes.NotFound)
      case ApplicationError.ExceptionError => complete(StatusCodes.InternalServerError)
      case _: DatabaseErrorEnum#AEVal => complete(StatusCodes.BadRequest)
      case _: Throwable => complete(StatusCodes.InternalServerError)
    }
  }
}
