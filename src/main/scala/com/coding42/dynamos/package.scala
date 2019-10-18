package com.coding42

import java.{util => jutil}

import com.amazonaws.services.dynamodbv2.model.{AttributeValue, GetItemResult}
import com.coding42.util.EitherUtil

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
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

    def fromDynamo[A](i: GetItemResult)(implicit reader: DynamosReader[A]): DynamosResult[Option[A]] =
      fromDynamo(i.getItem)

    def fromDynamo[A](
      i: java.util.Map[String, AttributeValue]
    )(implicit reader: DynamosReader[A]): DynamosResult[Option[A]] =
      Option(i) match {
        case Some(map) => reader.read(new AttributeValue().withM(map)).map(Some(_))
        case None      => Right(None)
      }

    def fromDynamoOp[A](i: GetItemResult)(implicit reader: DynamosReader[A]): Option[DynamosResult[A]] =
      fromDynamoOp(i.getItem)

    def fromDynamoOp[A](
      i: java.util.Map[String, AttributeValue]
    )(implicit reader: DynamosReader[A]): Option[DynamosResult[A]] =
      Option(i).map { map =>
        reader.read(new AttributeValue().withM(map))
      }

    def fromDynamo[A: DynamosReader](
      i: java.util.Collection[java.util.Map[String, AttributeValue]]
    ): Iterable[DynamosResult[A]] =
      i.asScala.flatMap(fromDynamoOp[A](_))

  }

}
