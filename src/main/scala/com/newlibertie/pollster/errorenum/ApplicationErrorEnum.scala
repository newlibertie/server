package com.newlibertie.pollster.errorenum

/**
  * Class to define errors related to application logic
  * @param errEnumName  The unique name of the derived class
  * @param initial  the id of the first enum variable in the class with name $errEnumName
  * @param capacity the number of unique ids for the enum variables in the class with name $errEnumName
  */
class ApplicationErrorEnum (errEnumName: String, initial:Int, capacity:Int) extends BaseError(errEnumName, initial, capacity)

/**
  * companion object to define error variables for application errors
  */
object ApplicationError extends ApplicationErrorEnum("ApplicationError", 0, 1000){
  val Unknown         = AEVal(0, "Unknown")
  val AssertionError  = AEVal(1, "AssertionError")
  val ExceptionError  = AEVal(2, "ExceptionError")
}
