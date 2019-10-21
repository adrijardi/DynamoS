package com.coding42.dynamos


import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.collection.JavaConverters._

trait DefaultDynamosWriters {

  implicit object StringWriter extends DynamosWriter[String] {
    override def write(a: String): AttributeValue =
      if (a.nonEmpty) {
        AttributeValue.builder().s(a).build()
      } else {
        AttributeValue.builder().nul(true).build()
      }
  }

  implicit object LongWriter extends DynamosWriter[Long] {
    override def write(a: Long): AttributeValue = AttributeValue.builder().n(a.toString).build()
  }

  implicit object IntWriter extends DynamosWriter[Int] {
    override def write(a: Int): AttributeValue = AttributeValue.builder().n(a.toString).build()
  }

  implicit object DoubleWriter extends DynamosWriter[Double] {
    override def write(a: Double): AttributeValue = AttributeValue.builder().n(a.toString).build()
  }

  implicit object FloatWriter extends DynamosWriter[Float] {
    override def write(a: Float): AttributeValue = AttributeValue.builder().n(a.toString).build()
  }

  implicit object BoolWriter extends DynamosWriter[Boolean] {
    override def write(a: Boolean): AttributeValue = AttributeValue.builder().bool(a).build()
  }

  implicit object AttributeValueWriter extends DynamosWriter[AttributeValue] {
    override def write(a: AttributeValue): AttributeValue = a
  }

  implicit def seqWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Seq[A]] = new DynamosWriter[Seq[A]] {
    override def write(a: Seq[A]): AttributeValue = AttributeValue.builder().l(a.map(aWriter.write).asJava).build()
  }

  implicit def setWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Set[A]] = new DynamosWriter[Set[A]] {
    override def write(a: Set[A]): AttributeValue = AttributeValue.builder().l(a.map(aWriter.write).asJava).build()
  }

  implicit def mapWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[collection.Map[String, A]] =
    new DynamosWriter[collection.Map[String, A]] {
      override def write(a: collection.Map[String, A]): AttributeValue =
        AttributeValue.builder().m(a.mapValues(aWriter.write).asJava).build()
    }

  implicit def optionWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Option[A]] =
    new DynamosWriter[Option[A]] {
      override def write(a: Option[A]): AttributeValue =
        a.map(aWriter.write).getOrElse(AttributeValue.builder().nul(true).build())
    }

}

object DefaultDynamosWriters extends DefaultDynamosWriters
