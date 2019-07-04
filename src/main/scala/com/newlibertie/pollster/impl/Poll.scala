package com.newlibertie.pollster.impl

import java.math.BigInteger

import net.liftweb.json._

object Poll {

  def apply(params: PollParameters): Poll = new Poll(params)

  def apply(pollDetails:String): Poll = {
    implicit val formats = DefaultFormats
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

  // TODO :
  def write(poll: Poll) = {

  }

  def read() = {

  }
}

class Poll(val p:PollParameters, val cp:CryptographicParameters = new CryptographicParameters()) {
}
