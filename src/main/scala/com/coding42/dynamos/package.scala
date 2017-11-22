package com.coding42

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import scala.collection.JavaConverters._

import scala.language.implicitConversions

package object dynamos {

  implicit class ToDynamoIdOps[A](a: A)(implicit writer: DynamosWriter[A]) {
    def toDynamoDb: AttributeValue = writer.write(a)
  }

  object Dynamos {
    def fromDynamo[A](i: java.util.Map[String, AttributeValue])(implicit reader: DynamosReader[A]): A = {
      reader.read(new AttributeValue().withM(i))
    }

    def fromDynamo[A](i: java.util.Collection[java.util.Map[String, AttributeValue]])(implicit reader: DynamosReader[A]): Iterable[A] = {
      i.asScala.map(fromDynamo)
    }
  }
}
