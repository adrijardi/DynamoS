package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import magnolia.{CaseClass, Magnolia, SealedTrait}

import scala.collection.JavaConverters._
import scala.language.experimental.macros
import scala.util.Try

trait DynamosReader[+A] {
  def read(a: AttributeValue): DynamosResult[A]
}

object DynamosReader {

  type Typeclass[T] = DynamosReader[T]

  private case class DynamosInternalException(error: DynamosParsingError) extends Exception

  def combine[A](caseClass: CaseClass[Typeclass, A]): DynamosReader[A] = new DynamosReader[A] {
    override def read(a: AttributeValue): DynamosResult[A] = {
      val params = a.getM.asScala // TODO check what happens when getM is not valid
      Try {
        caseClass.construct { p =>
          p.typeclass
            .read(params(p.label))
            .fold(
              err => throw DynamosInternalException(err),
              identity
            )
        }
      }.map(Right(_))
        .recover {
          case DynamosInternalException(error) =>
            Left(error)
        }
        .get
    }
  }

  def dispatch[A](sealedTrait: SealedTrait[Typeclass, A]): DynamosReader[A] = new DynamosReader[A] {
    override def read(a: AttributeValue): DynamosResult[A] = {
      val typeName = a.getM.asScala("dynamos-type")
      val subtype  = sealedTrait.subtypes.find(_.label == typeName.getS).get
      subtype.typeclass.read(a)
    }
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
