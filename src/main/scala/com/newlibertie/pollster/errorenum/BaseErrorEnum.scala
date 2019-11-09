package com.newlibertie.pollster.errorenum

import scala.collection.mutable

class BaseErrorEnum(val errEnumName: String, val initial:Int, val capacity:Int) extends Enumeration{
  require(EnumStore.register(errEnumName, initial, capacity), "Failed to register: "+errEnumName)
  type AppErr = AEVal
  import scala.language.implicitConversions
  implicit def valueAppErrVal(x: Value): AEVal = x.asInstanceOf[AEVal]

  case class AEVal(i: Int, name: String) extends super.Val(initial+i, name){
    protected def nextId: Int =  BaseErrorEnum.super.Value.nextId
  }

  protected object AEVal{
    protected def generateName(i: Int): String = {s"$errEnumName.Error#$i"}
    protected def generateName(s: String): String = {s"$errEnumName.$s"}
    def apply(i:Int):AEVal ={
      val name = AEVal.generateName(i)
      require(BaseErrorEnum.super.values.find(_.toString == name) == None, "Duplicate AEVal.name: " + name)
      AEVal(i, name)
    }
    def apply(name: String):AEVal  = {
      val name2 = generateName(name)
      require(BaseErrorEnum.super.values.find(_.toString == name2) == None, "Duplicate AEVal.name: " + name2)
      AEVal(nextId-initial, name2)
    }
    def apply():AEVal = {
      AEVal(nextId-initial)
    }
  }
}

private object EnumStore {
  class ErrEnumRange(val initial: Int, val capacity: Int)
  val registery: mutable.Map[String, ErrEnumRange] = new mutable.HashMap
  def register(errEnumName: String, initial:Int, capacity:Int):Boolean = {
    if (registery.contains(errEnumName) ||
      (registery.valuesIterator.exists(er => er.initial < (initial + capacity) &&
        initial < (er.initial + er.capacity)))){
      false
    }
    else
    {
      registery(errEnumName) = new ErrEnumRange(initial, capacity)
      true
    }
  }
}
