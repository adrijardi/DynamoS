package com.coding42.dynamos

import com.coding42.util.EitherUtil
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

trait DefaultDynamosReaders {

  implicit object StringReader extends DynamosReader[String] {
    override def read(a: AttributeValue): DynamosResult[String] =
      if (a.nul())
        Right("")
      else
        Option(a.s())
          .toRight(DynamosParsingError("String"))
  }

  implicit object LongReader extends DynamosReader[Long] {
    override def read(a: AttributeValue): DynamosResult[Long] =
      Option(a.n())
        .map(_.toLong)
        .toRight(DynamosParsingError("Long"))
  }

  implicit object IntReader extends DynamosReader[Int] {
    override def read(a: AttributeValue): DynamosResult[Int] =
      Option(a.n())
        .map(_.toInt)
        .toRight(DynamosParsingError("Int"))
  }

  implicit object DoubleReader extends DynamosReader[Double] {
    override def read(a: AttributeValue): DynamosResult[Double] =
      Option(a.n())
        .map(_.toDouble)
        .toRight(DynamosParsingError("Double"))
  }

  implicit object FloatReader extends DynamosReader[Float] {
    override def read(a: AttributeValue): DynamosResult[Float] =
      Option(a.n())
        .map(_.toFloat)
        .toRight(DynamosParsingError("Float"))
  }

  implicit object BoolReader extends DynamosReader[Boolean] {
    override def read(a: AttributeValue): DynamosResult[Boolean] =
      Option(a.bool())
        .map(Boolean.unbox)
        .toRight(DynamosParsingError("Boolean"))
  }

  implicit def optionReader[A: DynamosReader]: DynamosReader[Option[A]] = new DynamosReader[Option[A]] {
    override def read(a: AttributeValue): DynamosResult[Option[A]] =
      if (a.nul()) {
        Right(None)
      } else {
        implicitly[DynamosReader[A]]
          .read(a)
          .fold(
            err => Left(DynamosParsingError(s"Option[${err.field}]")),
            a => Right(Some(a))
          )
      }
  }

  implicit def collectionReader[A, C[_]](
    implicit aReader: DynamosReader[A],
    cbf: CanBuildFrom[Nothing, A, C[A]]
  ): DynamosReader[C[A]] =
    new DynamosReader[C[A]] {
      override def read(a: AttributeValue): DynamosResult[C[A]] =
        EitherUtil.sequence {
          a.l.asScala
            .map(aReader.read)
            .toList
        }.right.map(_.to[C])
    }

  implicit def mapReader[A](implicit aReader: DynamosReader[A]): DynamosReader[Map[String, A]] =
    new DynamosReader[Map[String, A]] {
      override def read(a: AttributeValue): DynamosResult[Map[String, A]] =
        EitherUtil.map {
          a.m.asScala.map { case (k, v) => k -> aReader.read(v) }.toMap
        }

    }

}

object DefaultDynamosReaders extends DefaultDynamosReaders
