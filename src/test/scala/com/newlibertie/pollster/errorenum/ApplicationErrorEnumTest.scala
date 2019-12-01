package com.newlibertie.pollster.errorenum

import org.scalatest._
import com.typesafe.scalalogging.LazyLogging
class ApplicationErrorEnumTest extends WordSpec with Matchers with LazyLogging {

  class TestApplicationErrorEnum (errEnumName: String, initial:Int, capacity:Int) extends BaseErrorEnum(errEnumName, initial, capacity)
  object TestApplicationErrorEnum extends TestApplicationErrorEnum("TestApplicationError", 0, 10){
    val Unknown         = AEVal(0, "Unknown")
  }

  "the enum creation" should {
    "detect duplicate enum name" in {
      class TestApplicationErrorEnum2 (errEnumName: String, initial:Int, capacity:Int) extends BaseErrorEnum(errEnumName, initial, capacity)
      object TestApplicationErrorEnum2 extends TestApplicationErrorEnum2("TestApplicationError", 0, 10){
        val Unknown         = AEVal(0, "Unknown")
      }
      var stat = -1
      try {
        var err:AnyRef = TestApplicationErrorEnum.Unknown
        err = TestApplicationErrorEnum2.Unknown
      }
      catch {
        case ex: Exception =>
          logger.info(ex.getMessage)
          stat = if (ex.getMessage().equalsIgnoreCase("requirement failed: Failed to register: TestApplicationError")) 0 else -1
      }
      stat shouldEqual 0
    }
    "detect overlapping id range" in {
      var stat = -1
      class TestApplicationErrorEnum3 (errEnumName: String, initial:Int, capacity:Int) extends BaseErrorEnum(errEnumName, initial, capacity)
      object TestApplicationErrorEnum3 extends TestApplicationErrorEnum3("TestApplicationError3", 0, 10){
        val Unknown         = AEVal(0, "Unknown")
      }
      try {
        var err:AnyRef = TestApplicationErrorEnum.Unknown
        err = TestApplicationErrorEnum3.Unknown
      }
      catch {
        case ex: Exception =>
          logger.info(ex.getMessage)
          stat = if (ex.getMessage().equalsIgnoreCase("requirement failed: Failed to register: TestApplicationError3")) 0 else -1
        case t: Throwable =>
          logger.info(t.getLocalizedMessage)
          stat = -1
      }
      stat shouldEqual 0
    }
    "detect duplicate id" in {
      var stat = -1
      class TestApplicationErrorEnum4 (errEnumName: String, initial:Int, capacity:Int) extends BaseErrorEnum(errEnumName, initial, capacity)
      try {
        object TestApplicationErrorEnum4 extends TestApplicationErrorEnum4("TestApplicationError4", 10, 10){
          val Unknown         = AEVal(0, "Unknown")
          val Unknown2        = AEVal(0, "Unknown2")
        }
        TestApplicationErrorEnum4.Unknown
      }
      catch {
        case t: Throwable =>
          logger.info(t.getMessage)
          stat = if (t.getMessage.startsWith("assertion failed: Duplicate id: ")) 0 else -1
      }
      stat shouldEqual 0
    }
  }
}
