package com.newlibertie.pollster

import java.sql.{Connection, DriverManager, ResultSet}

import akka.http.scaladsl.model.StatusCodes
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.newlibertie.pollster.impl.Poll
import com.typesafe.scalalogging.LazyLogging

import scala.collection.immutable.Map

object DataAdapter extends LazyLogging {
  val user: String = System.getProperty("user", "clowdsource")
  val pass: String = System.getProperty("password", "G00dN3w5")
  val url: String = System.getProperty( "jdbcurl" ,
    "jdbc:mysql://localhost/nldb?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC")

  val cachedConnection:ThreadLocal[Connection] = new ThreadLocal()

  def getConnection: Connection = {
    val existingConnection = cachedConnection.get()
    if (existingConnection == null || !existingConnection.isValid(1)) {
      val newConnection = DriverManager.getConnection(url, user, pass)
      cachedConnection.set(newConnection)
      newConnection
    }
    else {
      existingConnection
    }
  }

  def createPoll(poll: Poll) = {
    val query =
      s"""
        |INSERT INTO nldb.polls
        | (
        |   id, title, tags, creator_id, opening_ts, closing_ts, creation_ts, poll_type, poll_spec,
        |   large_prime_p, generator_g, private_key_s
        | )
        |VALUES (
        |  '${poll.p.id.get}',
        |  '${poll.p.title}',
        |  '${poll.p.tags.toString()}',
        |  '${poll.p.creator_id}',
        |  '${poll.p.opening_ts.toInstant.toString.replace('T', ' ').dropRight(1)}',
        |  '${poll.p.closing_ts.toInstant.toString.replace('T', ' ').dropRight(1)}',
        |  '${poll.p.creation_ts.get.toInstant.toString.replace('T', ' ').dropRight(1)}',
        |  '${poll.p.poll_type}',
        |  '${poll.p.poll_spec}',
        |  '${poll.cp.large_prime_p}',
        |  '${poll.cp.generator_g}',
        |  '${poll.cp.private_key_s}'
        |)
      """.stripMargin
    logger.info(query)
    val statement = getConnection.createStatement
    val numRows = statement.executeUpdate(query)
    if (numRows != 1) {
      logger.error("failed to insert " + query)
      -1
    }
    else {
      logger.info("poll.p.id: " + poll.p.id)
      poll.p.id
    }
  }

  def getPoll(id: String) = {
    val query =
      s"""
         |SELECT
         |   id, title, tags, creator_id, opening_ts, closing_ts, creation_ts, poll_type, poll_spec,
         |   large_prime_p, generator_g, private_key_s
         | FROM nldb.polls
         | WHERE id = '$id'
      """.stripMargin
    logger.info(query)
    val statement = getConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
    val rs: ResultSet = statement.executeQuery(query)
    if (!rs.first()) {
      logger.error("failed to retrieve using: " + query)
      -1
    }
    else {
      val md = rs.getMetaData
      val colCount = md.getColumnCount
      var mutableMap = scala.collection.mutable.Map[String, Any]()

      for (i <- 1 to colCount) {
        mutableMap += (md.getColumnName(i) -> rs.getObject(i))
      }

      val mapper = new ObjectMapper()
      mapper.registerModule(DefaultScalaModule)
      val str = mapper.writeValueAsString(mutableMap)
      logger.info(str)
      str
    }
  }
  private val idempotentFunc = (x:Any) => x.toString
  private val onceOnlyKeySet: Set[String] = Set("id", "creator_id", "creation_ts", "large_prime_p", "generator_g", "private_key_s")
  private val pollKeys: Map[Any, Function[Any, String]] = Map(("id", idempotentFunc), ("title", idempotentFunc),
    ("tags", idempotentFunc), ("creator_id", idempotentFunc),
    ("opening_ts", x => x.toString.replace('T', ' ').dropRight(1)),
    ("closing_ts", x => x.toString.replace('T', ' ').dropRight(1)),
    ("creation_ts", x => x.toString.replace('T', ' ').dropRight(1)),
    ("poll_type", idempotentFunc), ("poll_spec", idempotentFunc),
    ("large_prime_p", idempotentFunc), ("generator_g", idempotentFunc), ("private_key_s", idempotentFunc))

  def updatePoll(id: String, map:Map[String, AnyVal]): Any = {
    if (map isEmpty)
      return StatusCodes.BadRequest
    val sb  = new StringBuilder("UPDATE nldb.polls SET ")
    map
      .foreach { case (k, v: Any) =>
        if (!onceOnlyKeySet.contains(k) && pollKeys.contains(k)){
          sb.append(k).append("='").append(pollKeys(k)(v)).append("',")
        }
      }
    sb.deleteCharAt(sb.length()-1).append(s" WHERE id='$id'")
    val str = sb.toString()
    logger.info(str)
    val statement = getConnection.createStatement()
    statement.executeUpdate(str)
  }
//  def updatePoll(id: String): Any = {
//      return StatusCodes.BadRequest
//  }
}