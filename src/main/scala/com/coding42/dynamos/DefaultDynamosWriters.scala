package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import scala.collection.JavaConverters._

trait DefaultDynamosWriters {

  implicit object StringWriter extends DynamosWriter[String] { // TODO handle empty strings
    override def write(a: String): AttributeValue = new AttributeValue(a)
  }

  implicit object AttributeValueWriter extends DynamosWriter[AttributeValue] {
    override def write(a: AttributeValue): AttributeValue = a
  }

  implicit def iterableWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[Iterable[A]] = // TODO
    (a: Iterable[A]) => new AttributeValue().withL(a.map(aWriter.write).toSeq.asJava)

  implicit def mapWriter[A](implicit aWriter: DynamosWriter[A]): DynamosWriter[collection.Map[String, A]] =
    (a: collection.Map[String, A]) => new AttributeValue().withM(a.mapValues(aWriter.write).asJava)

}

object DefaultDynamosWriters extends DefaultDynamosWriters