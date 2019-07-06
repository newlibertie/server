package com.newlibertie.pollster

import java.sql.{Connection, DriverManager, ResultSet, Statement}

import com.newlibertie.pollster.impl.Poll
import com.typesafe.scalalogging.LazyLogging

object DataAdapter extends LazyLogging {
  val user = System.getProperty("user", "clowdsource");
  val pass = System.getProperty("password", "G00dN3w5") ;
  val url = System.getProperty( "jdbcurl" ,
    "jdbc:mysql://localhost/nldb?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC") ;

  val cachedConnection:ThreadLocal[Connection] = new ThreadLocal();

  def getConnection() = {
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
    val statement = getConnection().createStatement
    val numRows = statement.executeUpdate(query)
    if (numRows != 1) {
      logger.error("failed to insert " + query)
      -1
    }
    else {
      poll.p.id
    }
  }
}
