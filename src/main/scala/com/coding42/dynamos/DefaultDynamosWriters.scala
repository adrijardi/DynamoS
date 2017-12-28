package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._

trait DefaultDynamosWriters {

  implicit object StringWriter extends DynamosWriter[String] {
    override def write(a: String): AttributeValue =
      if(a.nonEmpty) {
        new AttributeValue(a)
      } else {
        new AttributeValue().withNULL(true)
      }
  }

  implicit object LongWriter extends DynamosWriter[Long] {
    override def write(a: Long): AttributeValue = new AttributeValue().withN(a.toString)
  }

  implicit object IntWriter extends DynamosWriter[Int] {
    override def write(a: Int): AttributeValue = new AttributeValue().withN(a.toString)
  }

  implicit object DoubleWriter extends DynamosWriter[Double] {
    override def write(a: Double): AttributeValue = new AttributeValue().withN(a.toString)
  }

  implicit object FloatWriter extends DynamosWriter[Float] {
    override def write(a: Float): AttributeValue = new AttributeValue().withN(a.toString)
  }

  implicit object BoolWriter extends DynamosWriter[Boolean] {
    override def write(a: Boolean): AttributeValue = new AttributeValue().withBOOL(a)
  }

  implicit object AttributeValueWriter extends DynamosWriter[AttributeValue] {
    override def write(a: AttributeValue): AttributeValue = a
  }

  implicit def seqWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Seq[A]] = new DynamosWriter[Seq[A]] {
    override def write(a: Seq[A]): AttributeValue = new AttributeValue().withL(a.map(aWriter.write).asJava)
  }

  implicit def setWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Set[A]] = new DynamosWriter[Set[A]] {
    override def write(a: Set[A]): AttributeValue = new AttributeValue().withL(a.map(aWriter.write).asJava)
  }

  implicit def mapWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[collection.Map[String, A]] =
    new DynamosWriter[collection.Map[String, A]] {
      override def write(a: collection.Map[String, A]): AttributeValue =
        new AttributeValue().withM(a.mapValues(aWriter.write).asJava)
    }

  implicit def optionWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Option[A]] = new DynamosWriter[Option[A]] {
    override def write(a: Option[A]): AttributeValue = a.map(aWriter.write).getOrElse(new AttributeValue().withNULL(true))
  }

}

object DefaultDynamosWriters extends DefaultDynamosWriters
