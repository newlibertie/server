package com.newlibertie.pollster.impl

import java.math.BigInteger

import com.newlibertie.pollster.DataAdapter
import net.liftweb.json._

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

  def write(poll: Poll) = {
    DataAdapter.createPoll(poll)
  }

  def read(id:String) = {
    DataAdapter.getPoll(id)

  }
}

class Poll(val p:PollParameters, val cp:CryptographicParameters = new CryptographicParameters()) {
}