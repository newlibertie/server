package com.newlibertie.pollster.impl

import java.math.BigInteger
import java.util.Date

import com.newlibertie.pollster.DataAdapter
import net.liftweb.json._

import scala.collection.mutable

object Poll {

  def apply(params: PollParameters): Poll = new Poll(params)

  def apply(pollDetails:String): Poll = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val jValue = parse(pollDetails)
    val pollParameters = jValue.extract[PollParameters]
    val pollJsonMap = jValue.values.asInstanceOf[Map[String, String]]
    val cryptographicParameters = if(  // string did not prescribe existing crypto params, then create new
      !pollJsonMap.contains("p") ||
        !pollJsonMap.contains("g") ||
        !pollJsonMap.contains("s")){
      new CryptographicParameters()
    }
    else {                             // else extract out the ones provided
      CryptographicParameters(
        new BigInteger(jValue.values.asInstanceOf[Map[String, String]]("p")),
        new BigInteger(jValue.values.asInstanceOf[Map[String, String]]("g")),
        new BigInteger(jValue.values.asInstanceOf[Map[String, String]]("s")))
    }
    new Poll(pollParameters, cryptographicParameters)
  }
  def read(id:String) = {
    DataAdapter.getPoll(id) match {
      case m:mutable.Map[String, Any] =>
        val tagsStr: String = m.get("tags").get.toString
        val tagsStrLen = tagsStr.length
        new Poll(PollParameters(Some(m.get("id").toString),
          m.get("title").toString,
          tagsStr.substring(5, tagsStrLen - 1).split(",").toList,
          m.get("creator_id").toString,
          m.get("opening_ts").get.asInstanceOf[Date],
          m.get("closing_ts").get.asInstanceOf[Date],
          Some(m.get("creation_ts").get.asInstanceOf[Date]),
          Some(m.get("last_modification_ts").get.asInstanceOf[Date]),
          m.get("poll_spec").get.toString,
          m.get("poll_type").get.toString),
        new CryptographicParameters(new BigInteger(m.get("large_prime_p").get.toString),
          new BigInteger(m.get("generator_g").get.toString),
          new BigInteger(m.get("private_key_s").get.toString)))
      case _ =>  -1
    }
  }
}

class Poll(val p:PollParameters, val cp:CryptographicParameters = new CryptographicParameters()) {
  def create() = {
    DataAdapter.createPoll(this)
  }
  def update() = {
    DataAdapter.updatePoll(this)
  }
  def canDelete():Boolean = {
    p.opening_ts before new Date() // TODO check if any ballot has been cast to disallow
  }
  def canClose():Boolean = {
    val cur_ts = new Date()
    (p.opening_ts before cur_ts) && (p.closing_ts after cur_ts)
  }
  def canOpen():Boolean = {
    p.opening_ts after new Date()
  }
  def deletePoll(): Int = {
    DataAdapter.deletePoll(p.id.get)
  }
  override def toString: String = {
    val sb  = new StringBuilder("{")
    sb.append(p.toString).append(",").append(cp.toString).append("}").toString()
  }
}