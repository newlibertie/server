package com.newlibertie.pollster.impl

import java.util.{Date, UUID}


case class PollParameters
(
   var id:Option[String],
   title:String,
   tags:List[String],
   creator_id:String,
   opening_ts:Date,
   closing_ts:Date,
   var creation_ts:Option[Date],
   var last_modification_ts:Option[Date],
   poll_type:String,
   poll_spec:String
)
{
  if(id == None) {
    id = Some(UUID.randomUUID.toString)
  }
  if(creation_ts == None) {
    creation_ts = Some(new Date())
  }
  override def toString: String = {
      """
        |"id":"${id.get}",
        |"tittle":"${title}",
        |"tags":"${tags.toString()}",
        |"creator_id":"${creator_id}",
        |"opening_ts":"${opening_ts.toInstant.toString.replace('T', ' ').dropRight(1)}",
        |"closing_ts":"${closing_ts.toInstant.toString.replace('T', ' ').dropRight(1)}",
        |"creation_ts":"${creation_ts.get.toInstant.toString.replace('T', ' ').dropRight(1)}",
        |"last_modification_ts":"${last_modification_ts.get.toInstant.toString.replace('T', ' ').dropRight(1)}",
        |"poll_type":"${poll_type}",
        |"poll_spec":"${poll_spec}"
        |""".stripMargin
  }
}
