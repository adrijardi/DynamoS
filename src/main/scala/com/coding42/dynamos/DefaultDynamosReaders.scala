package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.Try

trait DefaultDynamosReaders {

  case class ReadingException(msg: String) extends Exception(msg) // TODO use either instead

  implicit object StringReader extends DynamosReader[String] {
    override def read(a: AttributeValue): String = if(a.isNULL) "" else a.getS
  }

  implicit object LongReader extends DynamosReader[Long] {
    override def read(a: AttributeValue): Long =
      Option(a.getN)
        .map(_.toLong)
        .getOrElse(throw ReadingException("cannot read Long"))
  }

  implicit object IntReader extends DynamosReader[Int] {
    override def read(a: AttributeValue): Int =
      Option(a.getN)
        .map(_.toInt)
        .getOrElse(throw ReadingException("cannot read Int"))
  }

  implicit object DoubleReader extends DynamosReader[Double] {
    override def read(a: AttributeValue): Double =
      Option(a.getN)
        .map(_.toDouble)
        .getOrElse(throw ReadingException("cannot read Double"))
  }

  implicit object FloatReader extends DynamosReader[Float] {
    override def read(a: AttributeValue): Float =
      Option(a.getN)
        .map(_.toFloat)
        .getOrElse(throw ReadingException("cannot read Float"))
  }

  implicit object BoolReader extends DynamosReader[Boolean] {
    override def read(a: AttributeValue): Boolean =
      Option(a.getBOOL)
        .getOrElse(throw ReadingException("cannot read Boolean"))
  }

  implicit def optionReader[A : DynamosReader]: DynamosReader[Option[A]] = new DynamosReader[Option[A]] {
    override def read(a: AttributeValue): Option[A] =
      if(a.isNULL) {
        None
      } else {
        Try(implicitly[DynamosReader[A]].read(a))
          .recover {
            case _: ReadingException => throw ReadingException("Error while reading Option[A]") // TODO
          }
          .toOption
      }
  }

  implicit def collectionReader[A, C[_]]
  (implicit aReader: DynamosReader[A], cbf: CanBuildFrom[Nothing, A, C[A]]): DynamosReader[C[A]] =
    new DynamosReader[C[A]] {
      override def read(a: AttributeValue): C[A] = a.getL
        .asScala
        .map(aReader.read)
        .to[C]
    }

  implicit def mapReader[A](implicit aReader: DynamosReader[A]): DynamosReader[Map[String, A]] = new DynamosReader[Map[String, A]] {
    override def read(a: AttributeValue): Map[String, A] =
      a.getM.asScala.map { case (k, v) => k -> aReader.read(v) }.toMap
  }

}

object DefaultDynamosReaders extends DefaultDynamosReaders
