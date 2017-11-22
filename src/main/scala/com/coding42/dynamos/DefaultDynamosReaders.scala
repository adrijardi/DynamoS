package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import scala.collection.JavaConverters._

trait DefaultDynamosReaders {

  implicit object StringReader extends DynamosReader[String] {
    override def read(a: AttributeValue): String = a.getS
  }

  implicit def setReader[A](implicit aReader: DynamosReader[A]): DynamosReader[Set[A]] =
    (a: AttributeValue) => a.getL.asScala.map(aReader.read).toSet

}

object DefaultDynamosReaders extends DefaultDynamosReaders