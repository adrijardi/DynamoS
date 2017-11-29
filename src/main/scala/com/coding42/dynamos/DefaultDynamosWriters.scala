package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._

trait DefaultDynamosWriters {

  implicit val stringWriter: DynamosWriter[String] = (a: String) =>
    if(a.nonEmpty) {
      new AttributeValue(a)
    } else {
      new AttributeValue().withNULL(true)
    }

  implicit val longWriter: DynamosWriter[Long] = (a: Long) => new AttributeValue().withN(a.toString)

  implicit val intWriter: DynamosWriter[Int] = (a: Int) => new AttributeValue().withN(a.toString)

  implicit val doubleWriter: DynamosWriter[Double] = (a: Double) => new AttributeValue().withN(a.toString)

  implicit val floatWriter: DynamosWriter[Float] = (a: Float) => new AttributeValue().withN(a.toString)

  implicit val boolWriter: DynamosWriter[Boolean] = (a: Boolean) => new AttributeValue().withBOOL(a)

  implicit val attributeValueWriter: DynamosWriter[AttributeValue] = (a: AttributeValue) => a

  implicit def seqWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Seq[A]] =
    (a: Seq[A]) => new AttributeValue().withL(a.map(aWriter.write).asJava)

  implicit def setWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Set[A]] =
    (a: Set[A]) => new AttributeValue().withL(a.map(aWriter.write).asJava)

  implicit def mapWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[collection.Map[String, A]] =
    (a: collection.Map[String, A]) => new AttributeValue().withM(a.mapValues(aWriter.write).asJava)

  implicit def optionWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Option[A]] =
    (a: Option[A]) =>
      a.map(aWriter.write).getOrElse(new AttributeValue().withNULL(true))

}

object DefaultDynamosWriters extends DefaultDynamosWriters
