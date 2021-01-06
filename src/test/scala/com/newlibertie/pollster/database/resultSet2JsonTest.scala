package com.newlibertie.pollster.database

import com.newlibertie.pollster.ResultSet2JSON
//import org.scalatest._
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json.JsonAST.prettyRender
import org.scalatest.{FlatSpec, Matchers}

import java.sql.{Connection, DriverManager, ResultSet}

class annotatedTest extends FlatSpec with Matchers with LazyLogging {
  Class.forName("org.apache.derby.jdbc.EmbeddedDriver")
  val dbConnection: Connection = DriverManager.getConnection("jdbc:derby:testdb;create=true")

  dbConnection.createStatement().execute("DROP TABLE testTable")
  dbConnection.createStatement().execute("CREATE TABLE testTable (name varchar(20)," +
    "age int," +
    "lastName varchar(30))")

 dbConnection.createStatement().executeUpdate(
  "INSERT INTO testTable (name, age, lastName)\n" +
    "VALUES ('Name_One', 35, 'LastName_One')," +
    "('Name_Two', 45, 'LastName_Two')")

  dbConnection.commit()

  "resultSetPrettyPrint" should "have string" in {
    val rs1: ResultSet = dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.TYPE_SCROLL_SENSITIVE)
      .executeQuery("select * from APP.testTable")
    val ppStr = prettyRender(ResultSet2JSON.getJsonArr(rs1)).stripMargin
    println(ppStr)
    val expectedStr =
      """[
        |  {
        |    "NAME":"Name_One",
        |    "AGE":35,
        |    "LASTNAME":"LastName_One"
        |  },
        |  {
        |    "NAME":"Name_Two",
        |    "AGE":45,
        |    "LASTNAME":"LastName_Two"
        |  }
        |]""".stripMargin
    expectedStr shouldEqual ppStr
  }
  //dbConnection.close()
}
