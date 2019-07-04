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
   poll_spec:String,
)
{
  if(id == None) {
    id = Some(UUID.randomUUID.toString)
  }
  if(creation_ts == None) {
    creation_ts = Some(new Date())
  }
}
