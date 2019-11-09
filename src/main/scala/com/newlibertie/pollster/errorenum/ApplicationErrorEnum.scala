package com.newlibertie.pollster.errorenum

class ApplicationErrorEnum (errEnumName: String, initial:Int, capacity:Int) extends BaseErrorEnum(errEnumName, initial, capacity)

object ApplicationError extends DatabaseErrorEnum("DatabaseError", 0, 1000){
  val Unknown         = AEVal(0, "Unknown")
  val AssertionError  = AEVal(1, "AssertionError")
  val ExceptionError  = AEVal(2, "ExceptionError")
}
