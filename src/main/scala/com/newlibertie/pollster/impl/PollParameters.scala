package com.newlibertie.pollster.impl

import java.util.Date


case class PollParameters
(
   id:Option[String],
   title:String,
   tags:List[String],
   creator_id:String,
   opening_ts:Date,
   closing_ts:Date,
   creation_ts:Option[Date],
   last_modification_ts:Option[Date],
   poll_type:String,
   poll_spec:String,
)
