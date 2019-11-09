package com.newlibertie.pollster.errorenum

class DatabaseErrorEnum(errEnumName: String, initial:Int, capacity:Int) extends BaseErrorEnum(errEnumName, initial, capacity)

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
