package com.newlibertie.pollster

import java.sql.{ResultSet, ResultSetMetaData, Types}

import com.newlibertie.pollster.DataAdapter.logger
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json.JsonAST.prettyRender
import net.liftweb.json.{JArray, JField, JObject, JsonDSL}

import scala.collection.mutable.ListBuffer

object ResultSet2JSON extends LazyLogging {
  def getJsonArr(rs: ResultSet): JArray = {
    var jsonObjList = new ListBuffer[JObject]()
    if (rs.first()) {
      do
        jsonObjList += getJsonObj(rs)
      while (rs.next())
    }
    JArray(jsonObjList.toList)
  }
  def getJsonObj(rs: ResultSet): JObject = {
    val resultSetMetaData: ResultSetMetaData = rs.getMetaData
    val colCount: Int = resultSetMetaData.getColumnCount
    var obj = new ListBuffer[JField]()
    for (i <- 1 to colCount)
    {
      val colName = resultSetMetaData.getColumnName(i)
      obj += (resultSetMetaData.getColumnType(i) match
      {
        case Types.BOOLEAN
        => new JField(colName, JsonDSL.boolean2jvalue(rs.getBoolean(i)))
        case Types.BIGINT
        => new JField(colName, JsonDSL.bigint2jvalue(rs.getLong(i)))
        case Types.DOUBLE | Types.REAL | Types.NUMERIC | Types.DECIMAL
        => new JField(colName, JsonDSL.double2jvalue(rs.getDouble(i)))
        case Types.FLOAT
        => new JField(colName, JsonDSL.float2jvalue(rs.getFloat(i)))
        case Types.INTEGER | Types.TINYINT | Types.SMALLINT
        => new JField(colName, JsonDSL.int2jvalue(rs.getInt(i)))
        case Types.NVARCHAR | Types.VARCHAR | Types.CHAR | Types.LONGNVARCHAR
        => new JField(colName, JsonDSL.string2jvalue(rs.getNString(i)))
        case Types.DATE
        => new JField(colName, JsonDSL.string2jvalue(rs.getDate(i).toString))
        case Types.TIMESTAMP | Types.TIMESTAMP_WITH_TIMEZONE | Types.TIME
        => new JField(colName, JsonDSL.string2jvalue(rs.getTimestamp(i).toString))
        case _
        => new JField(colName, JsonDSL.string2jvalue(rs.getString(i)))
      })
    }
    val JObj = new JObject(obj.toList)
    logger.info(prettyRender(JObj))
    JObj
  }
}
