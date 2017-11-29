package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.Try
import scala.util.control.NonFatal

trait DefaultDynamosReaders {

  case class ReadingException(msg: String) extends Exception(msg) // TODO

  implicit val stringReader: DynamosReader[String] = (a: AttributeValue) => if(a.isNULL) "" else a.getS

  implicit val longReader: DynamosReader[Long] = (a: AttributeValue) =>
    Option(a.getN)
      .map(_.toLong)
      .getOrElse(throw ReadingException("cannot read Long"))

  implicit val intReader: DynamosReader[Int] = (a: AttributeValue) =>
    Option(a.getN)
      .map(_.toInt)
      .getOrElse(throw ReadingException("cannot read Int"))

  implicit val doubleReader: DynamosReader[Double] = (a: AttributeValue) =>
    Option(a.getN)
      .map(_.toDouble)
      .getOrElse(throw ReadingException("cannot read Double"))

  implicit val floatReader: DynamosReader[Float] = (a: AttributeValue) =>
    Option(a.getN)
      .map(_.toFloat)
      .getOrElse(throw ReadingException("cannot read Float"))

  implicit val boolReader: DynamosReader[Boolean] = (a: AttributeValue) =>
    Option(a.getBOOL)
      .getOrElse(throw ReadingException("cannot read Boolean"))

  implicit def optionReader[A : DynamosReader]: DynamosReader[Option[A]] = (a: AttributeValue) =>
    if(a.isNULL) {
      None
    } else {
      Try(implicitly[DynamosReader[A]].read(a))
        .recover {
          case _: ReadingException => throw ReadingException("Error while reading Option[A]") // TODO
        }
        .toOption
    }

  implicit def collectionReader[A, C[_]](implicit aReader: DynamosReader[A], cbf: CanBuildFrom[Nothing, A, C[A]]): DynamosReader[C[A]] =
    (a: AttributeValue) => {
      a.getL
        .asScala
        .map(aReader.read)
        .to[C]
    }

  implicit def mapReader[A](implicit aReader: DynamosReader[A]): DynamosReader[Map[String, A]] =
    (a: AttributeValue) => a.getM.asScala.map { case (k, v) => k -> aReader.read(v) }.toMap

}

object DefaultDynamosReaders extends DefaultDynamosReaders
