package com.newlibertie.pollster

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import com.newlibertie.pollster.api._

object Main extends App {


  implicit val system = ActorSystem("pollster")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  println("starting pollster service ")
  val routes = {
    pathPrefix("v1") {
      v1.Ballot.routes ~ v1.PollApi.routes ~ v1.Disqualification.routes
    }
  }

  val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", 1959)


  try {
    println("press enter to exit")
    System.in.read();
  }
  finally {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
