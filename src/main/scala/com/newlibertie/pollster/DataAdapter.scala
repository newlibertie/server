package com.newlibertie.pollster

import java.sql.{Connection, DriverManager, ResultSet, SQLException, SQLTimeoutException}
import java.util.Date

import com.newlibertie.pollster.errorenum.{ApplicationError, DatabaseError}
import com.newlibertie.pollster.impl.Poll
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write

import scala.collection.mutable

object DataAdapter extends LazyLogging {
  val user = System.getProperty("user", "clowdsource");
  val pass = System.getProperty("password", "G00dN3w5") ;
  val url = System.getProperty( "jdbcurl" ,
    "jdbc:mysql://localhost/nldb?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC") ;

  val cachedConnection:ThreadLocal[Connection] = new ThreadLocal();
  implicit val formats = DefaultFormats

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

  private def executeUpdateQuery(sql: String): Int = {
    try getConnection.createStatement().executeUpdate(sql)
    catch {
      case _: SQLException => throw DatabaseError.Access
      case _: SQLTimeoutException => throw DatabaseError.Timeout
      case _: Throwable => throw ApplicationError.ExceptionError
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
        |  '${write(poll.p.tags)}',
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
    val numRows = executeUpdateQuery(query)
    if (numRows != 1) {
      logger.error("failed to insert " + query)
      throw DatabaseError.ConstraintViolation
    }
    else {
      logger.info("poll.p.id: " + poll.p.id)
      poll.p.id
    }
  }

  def getPoll(id: String):mutable.Map[String, Any] = {
    val query =
      s"""
         |SELECT
         |   id, title, tags, creator_id, opening_ts, closing_ts, creation_ts, last_modification_ts, poll_type, poll_spec,
         |   large_prime_p, generator_g, private_key_s
         | FROM nldb.polls
         | WHERE id = '$id'
      """.stripMargin
    logger.info(query)
    val rs: ResultSet =
      getConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY).executeQuery(query)
    if (!rs.first()) {
      logger.error("failed to retrieve using: " + query)
      throw DatabaseError.RecordNotFound
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
         |   tags = '${write(poll.p.tags)}',
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
    executeUpdateQuery(s"DELETE FROM polls WHERE id = '$id'")
  }
  def closePoll(poll:Poll): Int = {
    val newClosingTS = new Date()
    val query =
      s"""
         |UPDATE nldb.polls
         |SET
         |   closing_ts = '${newClosingTS.toInstant.toString.replace('T', ' ').dropRight(1)}'
         |WHERE id = '${poll.p.id.get}'
      """.stripMargin
    logger.info(query)
    getConnection.createStatement().executeUpdate(query)
  }
}
