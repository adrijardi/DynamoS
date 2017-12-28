package com.coding42

import java.{util => jutil}

import com.amazonaws.services.dynamodbv2.model.{AttributeValue, GetItemResult}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

package object dynamos {

  case class DynamosParsingError(field: String)

  type DynamosResult[+A] = Either[DynamosParsingError, A]

  implicit class ToDynamoKey[A : DynamosWriter](map: Map[String, A]) {
    def toDynamoKey: jutil.Map[String, AttributeValue] = map.mapValues(_.toDynamoDb).asJava
  }

  implicit class ToDynamoIdOps[A](a: A)(implicit writer: DynamosWriter[A]) {
    def toDynamoDb: AttributeValue = writer.write(a)
  }

  object Dynamos {
    def fromDynamo[A](i: GetItemResult)(implicit reader: DynamosReader[A]): Option[DynamosResult[A]] = fromDynamo(i.getItem)

    def fromDynamo[A](i: java.util.Map[String, AttributeValue])(implicit reader: DynamosReader[A]): Option[DynamosResult[A]] = {
      Option(i).map { map =>
        reader.read(new AttributeValue().withM(map))
      }
    }
  }
}
