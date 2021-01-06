package com.newlibertie.pollster.database

import java.lang.annotation.{ElementType, Retention, RetentionPolicy, Target}
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._
import org.scalatest.{FlatSpec, Matchers}

@Retention(RetentionPolicy.RUNTIME)
@Target(Array(ElementType.TYPE))
abstract class nldbAnnotation() extends scala.annotation.StaticAnnotation
@Target(Array(ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER))
case class nldbField(fieldName:Option[String]=None) extends nldbAnnotation
@Target(Array(ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER))
case class nldbTable(tableName:Option[String]=None) extends nldbAnnotation

//@Target(Array(TYPE))
//@Retention(RetentionPolicy.RUNTIME)
//trait nldbTrait

@Target(Array(ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER))
@nldbTable(tableName = Some("polls")) case class Annotated_l(
                        @nldbField field1: Integer,
                        @nldbField field2: String,
                        field3: Boolean,
                        field4: Double
                      ) //extends nldbTrait

object GetAnnot{
  val nldbTypeSymbol: Symbol = typeOf[nldbField].typeSymbol
  val nldbTableTypeSymbol = typeOf[nldbTable].typeSymbol
  val objAnnotated: Annotated_l = Annotated_l(field1=12, field2="name2", field3 = true, field4 = 3.4)
  def fieldName2ValMap[T](annotatedObj: T): Map[String, AnyRef] = {
    annotatedObj.getClass.getDeclaredFields.map{
      field=>
        field.setAccessible(true)
        val res = (field.getName, field.get(annotatedObj))
        field.setAccessible(false)
        res
    }.toMap
  }
  def getTypeTag[T: TypeTag](obj: T): TypeTag[T] = typeTag[T]
  @throws(classOf[Exception])
  def create[T:TypeTag](annotatedObj:T): String = {

    val objType: universe.Type = typeOf[T];
    val objTypeSymbol: universe.Symbol = objType.typeSymbol
    val nldbTableName: universe.Symbol#NameType = getTypeTag(nldbTable).tpe.typeSymbol.name
    val annotation: universe.Annotation = objTypeSymbol.annotations.filter(_.tree.tpe.typeSymbol.name == nldbTableName).head

    val annotationProperties = getAnnotationProperties(annotation)
    println(annotationProperties.toString())

    val strList = annotation.toString.split('"')
    if (strList.size != 3) throw new Exception(s"Expected $nldbTableName value not found for ${objTypeSymbol.toString}")
    val sb: StringBuilder = new StringBuilder(1000, "INSERT INTO nldb.")
    val fieldNameList = objTypeSymbol.asClass.primaryConstructor.typeSignature.paramLists.flatten.filter(f=> f.annotations.exists(_.tree.tpe.typeSymbol == typeOf[nldbField].typeSymbol)).map(_.name.toString)
    sb.append(strList(1)).append(fieldNameList.mkString(" (", ",", ")"))

    val valMap = fieldName2ValMap(annotatedObj)
    val valList = fieldNameList.map(fieldName=>valMap.getOrElse(fieldName, "NULL"))
    sb.append(valList.mkString(" VALUES (", ",", ")")).toString
  }
  def getAnnotationProperties(annotation: Annotation) = {
    annotation.tree.children(1).children.head
      .foreach(a => {
        val x = getTypeTag(a)
        println(x, x.tpe.typeSymbol, x.tpe.termSymbol, x.tpe.resultType)})
    val returnValue = annotation.tree.children.head
      .filter(f = a => a.isInstanceOf[AssignOrNamedArg])
      .map(_.asInstanceOf[AssignOrNamedArg])
      .map(ap => (ap.lhs, ap.rhs))
    returnValue
  }

  def listProperties[T: TypeTag]: List[Annotation] = {
    typeOf[T].typeSymbol.asClass
      .primaryConstructor
      .typeSignature
      .paramLists.flatten.flatMap(_.annotations)
  }

/*
  val annotatedClassSymbol: ClassSymbol = typeOf[Annotated_l].typeSymbol.asClass
  val annotatedAnnotations: Seq[Annotation] = annotatedClassSymbol.annotations
  val annotatedAnnotationType: Type = typeOf[nldbField]
  annotatedAnnotations.find(a => a.tree.tpe == annotatedAnnotationType)
  def annotationTypeName(annot: Annotation): String = annot.tree.tpe.typeSymbol.name.toString

  val objSymbol: Symbol = typeOf[Annotated_l].typeSymbol
  val annotatedProps: Iterable[Symbol] = objSymbol.info.decls.filter(_.annotations.nonEmpty)
  val mainCtor: universe.Symbol = objSymbol.info.decls.find(d => d.isMethod && d.asMethod.isPrimaryConstructor).get
  val params: List[universe.Symbol] = mainCtor.typeSignature.paramLists.head
  val annotatedParams: List[universe.Symbol] = params.filter(p => p.annotations.exists(_.tree.tpe == annotatedAnnotationType))
  val annotatedParamFieldList: List[String] = params.map(_.name.toString)
*/
}

class resultSet2JsonTest extends FlatSpec with Matchers {
  import com.newlibertie.pollster.database.GetAnnot.{create, objAnnotated}
  "resultSetPrettyPrint" should "have string" in {
    val retVal = create(objAnnotated)
    println(retVal.toString())
  }
}
/*
//Access access Case class field Value from String name of
case class Person(name: String, age: Int)
val a = Person("test",10)
classOf[Person].getDeclaredFields.map{ f =>
  f.setAccessible(true)
  val res = (f.getName, f.get(a))
  f.setAccessible(false)
  res }
  //Get field names list from case class, By using User.getClass

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._
case class SimpleAnnotation() extends scala.annotation.StaticAnnotation
@SimpleAnnotation case class SimpleClass()
val simpleClassSymbol = typeOf[SimpleClass].typeSymbol.asClass
val simpleAnnotations = simpleClassSymbol.annotations
val simpleAnnotationType = typeOf[SimpleAnnotation]

simpleAnnotations.find(a => a.tree.tpe == simpleAnnotationType)

case class Unique() extends scala.annotation.StaticAnnotation
case class Foo(@Unique var str: String) {}

val annotations = listProperties[Foo].filter(_.tree.tpe =:= typeOf[Unique])

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
val tb = currentMirror.mkToolBox()
val result = tb.eval(tb.untypecheck(annotations.head.tree)).asInstanceOf[Unique]


scala> typeOf[Annotated_l].members.filter(!_.isMethod).map(_.name.toString)
res58: Iterable[String] = List("field2 ", "field1 ")

scala> typeOf[Annotated_l].members.filter(!_.isMethod).map(_.info)
res62: Iterable[reflect.runtime.universe.Type] = List(String, Integer)

scala> classOf[Annotated_l].isAnnotationPresent(classOf[Documented])
res64: Boolean = true

scala> typeOf[objAnnotated.type ].typeSymbol.asClass
res115: reflect.runtime.universe.ClassSymbol = class Annotated_l


scala> typeOf[objAnnotated.type ].typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.flatten.flatMap(_.annotations).filter(_.tree.tpe =:= typeOf[nldbField])
res117: List[reflect.runtime.universe.Annotation] = List(com.newlibertie.pollster.database.nldbField, com.newlibertie.pollster.database.nldbField)

scala> typeOf[objAnnotated.type ].typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.flatten.filter(f=>(f.annotations.exists(_.tree.tpe.typeSymbol == typeOf[nldbField].typeSymbol)))
res122: List[reflect.runtime.universe.Symbol] = List(value field1, value field2)

scala> objAnnotated.getClass.getDeclaredFields
res156: Array[java.lang.reflect.Field] = Array(private final java.lang.Integer Annotated_l.field1, private final java.lang.String Annotated_l.field2)

val fld2ValMap = objAnnotated.getClass.getDeclaredFields.map{
   f=>
     f.setAccessible(true)
     val res = (f.getName, f.get(objAnnotated))
     f.setAccessible(false)
     res
 }.toMap

scala> val fldNameLst = typeOf[objAnnotated.type ].typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.flatten.filter(f=>(f.annotations.exists(_.tree.tpe.typeSymbol == typeOf[nldbField].typeSymbol))).map(_.name.toString)
fldNameLst: List[String] = List(field1, field2)
val fld2ValMap = objAnnotated.getClass.getDeclaredFields.map{
   f=>
     f.setAccessible(true)
     val res = (f.getName, f.get(objAnnotated))
     f.setAccessible(false)
     res
 }.toMap
def fld2ValMap(objAnnotated: Annotated_l) = {
  objAnnotated.getClass.getDeclaredFields.map{
   f=>
     f.setAccessible(true)
     val res = (f.getName, f.get(objAnnotated))
     f.setAccessible(false)
     res
 }.toMap
}

fldNameLst.map(fn => (fn, res188.get(fn)))
res190: List[(String, Option[Object])] = List((field1,Some(12)), (field2,Some(name2)))

scala> val fld2ValMapVal = fld2ValMap(objAnnotated)
fld2ValMapVal: scala.collection.immutable.Map[String,Object] = Map(field1 -> 12, field2 -> name2)

fldNameLst.map(fn => (fn, fld2ValMapVal.get(fn)))

 */