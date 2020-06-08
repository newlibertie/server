package com.newlibertie.pollster.errorenum

import scala.collection.{AbstractSet, SetLike, generic, immutable, mutable}
import java.lang.reflect.{Method => JMethod}
import scala.reflect.NameTransformer._
import java.util.regex.Pattern

/***
  * A class at the root of all error enum derivations
  *   responsible for validating uniqueness of the name of the errEnumName and non-overlapping id ranges
  *   registers the EnumClass
  * @param errEnumName  The unique name of the derived class
  * @param initial  the id of the first enum variable in the class with name $errEnumName
  * @param capacity the number of unique ids for the enum variables in the class with name $errEnumName
  */
case class BaseError(errEnumName: String, initial:Int, capacity:Int) {
  thisBaseError =>
  require(BaseError.register(errEnumName, initial, capacity), "Failed to register: "+errEnumName)
  type AppErr = AEVal
  import scala.language.implicitConversions
  //implicit def valueAppErrVal(x: Int): AEVal = x.asInstanceOf[AEVal]
  /* Note that `readResolve` cannot be private, since otherwise
     the JVM does not invoke it when deserializing subclasses. */
  protected def readResolve(): AnyRef = thisBaseError.getClass.getField(MODULE_INSTANCE_NAME).get(null)

  /** The name of this enumeration.
    */
  override def toString = (
    (getClass.getName stripSuffix MODULE_SUFFIX_STRING split '.' last)
      split Pattern.quote(NAME_JOIN_STRING) last
    )



  /** The mapping from the integer used to identify values to the actual
    * values. */
  private val vmap: mutable.Map[Int, AEVal] = new mutable.HashMap

  /** The cache listing all values of this enumeration. */
  @transient private var vset: ValueSet = null
  @transient private var vsetDefined = false

  /** The mapping from the integer used to identify values to their
    * names. */
  private val nmap: mutable.Map[Int, String] = new mutable.HashMap

  /** The values of this enumeration as a set.
    */
  def values: ValueSet = {
    if (!vsetDefined) {
      vset = new ValueSet(immutable.SortedSet.empty[Int] ++ (vmap.values map (_.id)))
      vsetDefined = true
    }
    vset
  }

  /** The integer to use to identify the next created value. */
  protected var nextId = initial
  /** The value of this enumeration with given id `x`
    */
  final def apply(x: Int): AEVal = vmap(x)

  private def populateNameMap() {
    val fields = getClass.getDeclaredFields
    def isValDef(m: JMethod) = fields exists (fd => fd.getName == m.getName && fd.getType == m.getReturnType)

    // The list of possible Value methods: 0-args which return a conforming type
    val methods = getClass.getMethods filter (m => m.getParameterTypes.isEmpty &&
      classOf[AEVal].isAssignableFrom(m.getReturnType) &&
      m.getDeclaringClass != classOf[Enumeration] &&
      isValDef(m))
    methods foreach { m =>
      val name = m.getName
      // invoke method to obtain actual `Value` instance
      val value = m.invoke(this).asInstanceOf[AEVal]
      // verify that outer points to the correct Enumeration: ticket #3616.
      if (value.outerEnum eq thisBaseError) {
        val id = Int.unbox(classOf[AEVal] getMethod "id" invoke value)
        nmap += ((id, name))
      }
    }
  }

  /* Obtains the name for the value with id `i`. If no name is cached
   * in `nmap`, it populates `nmap` using reflection.
   */
  private def nameOf(i: Int): String = synchronized { nmap.getOrElse(i, { populateNameMap() ; nmap(i) }) }

  //final case class CustomException(private val message: String = "",
//                                  private val cause: Throwable = None.orNull)
//    extends Exception(message, cause)
  protected def generateName(i: Int): String = {s"$errEnumName.Error#$i"}
  protected def generateName(s: String): String = {s"$errEnumName.$s"}

  final case class AEVal(i: Int, name: String, private val cause: Throwable = None.orNull) extends Exception(name+initial+i){
    def this(_i: Int)       = this(_i, generateName(_i))
    def this(name: String) = this(nextId, name)
    def this()             = this(nextId)

    assert(!vmap.isDefinedAt(i), "Duplicate id: " + i)
    vmap(i) = this
    vsetDefined = false
    nextId = i + 1
    //if (nextId > topId) topId = nextId
    def id = i
    override def toString() =
      if (name != null) name
      else try thisBaseError.nameOf(i)
      catch { case _: NoSuchElementException => "<Invalid enum: no field for #" + i + ">" }

    protected def readResolve(): AnyRef = {
      val enum = thisBaseError.readResolve().asInstanceOf[BaseError]
      if (enum.vmap == null) this
      else enum.vmap(i)
    }
    /** a marker so we can tell whose values belong to whom come reflective-naming time */
    private[BaseError] val outerEnum = thisBaseError

    def compare(that: AEVal): Int = this.id - that.id
    override def equals(other: Any) = other match {
      case that: BaseError#AEVal  => (outerEnum eq that.outerEnum) && (id == that.id)
      case _                        => false
    }
    override def hashCode: Int = id.##
  }

  protected object AEVal{
    protected def generateName(i: Int): String = {s"$errEnumName.Error#$i"}
    protected def generateName(s: String): String = {s"$errEnumName.$s"}
    def apply(i:Int):AEVal ={
      val name = AEVal.generateName(i)
      require(values.find(_.toString == name) == None, "Duplicate AEVal.name: " + name)
      AEVal(i, name)
    }
    def apply(name: String):AEVal  = {
      val name2 = generateName(name)
      require(values.find(_.toString == name2) == None, "Duplicate AEVal.name: " + name2)
      AEVal(nextId-initial, name2)
    }
    def apply():AEVal = {
      AEVal(nextId-initial)
    }
  }
  /** A class for sets of values.
    *  Iterating through this set will yield values in increasing order of their ids.
    *
    *  @param ids The set of ids of values, organized as a `SortedSet`.
    */
  class ValueSet private[BaseError] (val ids: immutable.SortedSet[Int])
    extends AbstractSet[AEVal]
      with Set[AEVal]
      with SetLike[AEVal, ValueSet] {

    override def empty = ValueSet.empty
    def contains(v: AEVal) = ids contains (v.id)
    def + (value: AEVal) = new ValueSet(ids + value.id)
    def - (value: AEVal) = new ValueSet(ids - value.id)
    def iterator = ids.iterator map thisBaseError.apply
    override def stringPrefix = thisBaseError + ".ValueSet"
  }

  /** A factory object for value sets */
  object ValueSet {
    import generic.CanBuildFrom

    /** The empty value set */
    val empty = new ValueSet(immutable.SortedSet.empty)
    /** A value set consisting of given elements */
    def apply(elems: AEVal*): ValueSet = empty ++ elems
    /** A builder object for value sets */
    def newBuilder: mutable.Builder[AEVal, ValueSet] = new mutable.SetBuilder(empty)
    /** The implicit builder for value sets */
    implicit def canBuildFrom: CanBuildFrom[ValueSet, AEVal, ValueSet] =
      new CanBuildFrom[ValueSet, AEVal, ValueSet] {
        def apply(from: ValueSet) = newBuilder
        def apply() = newBuilder
      }
  }

}

/**
  * Helping Object providing validations and register each EnumClass derived from BaseErrorEnum
  */
private object BaseError {
  class ErrEnumRange(val initial: Int, val capacity: Int)
  private val registry: mutable.Map[String, ErrEnumRange] = new mutable.HashMap
  def register(errEnumName: String, initial:Int, capacity:Int):Boolean = {
    if (registry.contains(errEnumName) ||
      (registry.valuesIterator.exists(er => er.initial < (initial + capacity) &&
        initial < (er.initial + er.capacity)))){
      false
    }
    else
    {
      registry(errEnumName) = new ErrEnumRange(initial, capacity)
      true
    }
  }
}
