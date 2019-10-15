package com.newlibertie.pollster

import java.sql.{Connection, DriverManager, ResultSet}

import com.newlibertie.pollster.impl.Poll
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

object DataAdapter extends LazyLogging {
  val user = System.getProperty("user", "clowdsource");
  val pass = System.getProperty("password", "G00dN3w5") ;
  val url = System.getProperty( "jdbcurl" ,
    "jdbc:mysql://localhost/nldb?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC") ;

  val cachedConnection:ThreadLocal[Connection] = new ThreadLocal();

  def getConnection: Connection = {
    val existingConnection = cachedConnection.get();
    if (existingConnection == null || !existingConnection.isValid(1)) {
      val newConnection = DriverManager.getConnection(url, user, pass);
      cachedConnection.set(newConnection);
      newConnection
    }
    else {
      existingConnection;
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
        |  '${poll.p.tags}',
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
         |   id, title, tags, creator_id, opening_ts, closing_ts, creation_ts, last_modification_ts, poll_type, poll_spec,
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
      var mutableMap: mutable.Map[String, Any] = scala.collection.mutable.Map[String, Any]()
      for (i <- 1 to colCount) {
        mutableMap += (md.getColumnName(i) -> rs.getObject(i))
      }
      mutableMap
    }
  }
  def updatePoll(poll:Poll): Any = {
    val query =
      s"""
         |UPDATE nldb.polls
         |SET
         |   title = '${poll.p.title}',
         |   tags = '${poll.p.tags.toString()}',
         |   opening_ts = '${poll.p.opening_ts.toInstant.toString.replace('T', ' ').dropRight(1)}',
         |   closing_ts = '${poll.p.closing_ts.toInstant.toString.replace('T', ' ').dropRight(1)}',
         |   poll_type = '${poll.p.poll_type}',
         |   poll_spec = '${poll.p.poll_spec}'
         |WHERE id = '${poll.p.id.get}'
      """.stripMargin
    logger.info(query)
    getConnection.createStatement().executeUpdate(query)
  }
  def deletePoll(id: String): Int = {
    getConnection.createStatement().executeUpdate("DELETE FROM polls WHERE id = '" + id + "'")
  }
}
