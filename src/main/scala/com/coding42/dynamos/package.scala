package com.coding42

import java.{util => jutil}

import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemResponse}

import scala.collection.JavaConverters._
import scala.language.{higherKinds, implicitConversions}

package object dynamos {

  case class DynamosParsingError(field: String)

  type DynamosResult[+A] = Either[DynamosParsingError, A]

  implicit class ToDynamoKey[A: DynamosWriter](map: Map[String, A]) {
    def toDynamoKey: jutil.Map[String, AttributeValue] = map.mapValues(_.toDynamoDb).asJava
  }

  implicit class ToDynamoIdOps[A](a: A)(implicit writer: DynamosWriter[A]) {
    def toDynamoDb: AttributeValue = writer.write(a)
  }

  object Dynamos {

    def fromDynamo[A](i: GetItemResponse)(implicit reader: DynamosReader[A]): DynamosResult[Option[A]] =
      fromDynamo(i.item())

    def fromDynamo[A](
      i: java.util.Map[String, AttributeValue]
    )(implicit reader: DynamosReader[A]): DynamosResult[Option[A]] =
      Option(i) match {
        case Some(map) => reader.read(AttributeValue.builder().m(map).build()).map(Some(_))
        case None      => Right(None)
      }

    def fromDynamoOp[A](i: GetItemResponse)(implicit reader: DynamosReader[A]): Option[DynamosResult[A]] =
      fromDynamoOp(i.item())

    def fromDynamoOp[A](
      i: java.util.Map[String, AttributeValue]
    )(implicit reader: DynamosReader[A]): Option[DynamosResult[A]] =
      Option(i)
        .filter(_.isEmpty == false)
        .map { map =>
          reader.read(AttributeValue.builder().m(map).build())
        }

    def fromDynamo[A: DynamosReader](
      i: java.util.Collection[java.util.Map[String, AttributeValue]]
    ): Iterable[DynamosResult[A]] =
      i.asScala.flatMap(fromDynamoOp[A](_))

  }

}
