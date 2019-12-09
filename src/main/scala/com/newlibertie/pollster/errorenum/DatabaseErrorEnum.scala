package com.newlibertie.pollster.errorenum

/**
  * Class to define errors related to database
  * @param errEnumName  The unique name of the derived class
  * @param initial  the id of the first enum variable in the class with name $errEnumName
  * @param capacity the number of unique ids for the enum variables in the class with name $errEnumName
  */
class DatabaseErrorEnum(errEnumName: String, initial:Int, capacity:Int) extends BaseError(errEnumName, initial, capacity)

/**
  * companion object to define error variables for database errors
  */
object DatabaseError extends DatabaseErrorEnum("DatabaseError", 1000, 1000){
  val RecordNotFound              = AEVal(0, "RecordNotFound")
  val ConstraintViolation         = AEVal(1, "ConstraintViolation")
  val RecordIntegrityError        = AEVal(2, "RecordIntegrityError")
  val ConnectIdentifierResolution = AEVal(3, "ConnectIdentifierResolution")
  val InternalCodeError           = AEVal(4, "InternalCodeError")
  val DataTypeMisMatch            = AEVal(5, "DataTypeMisMatch")
  val Access                      = AEVal(6, "Access")
  val Timeout                     = AEVal(7, "Timeout")
}
