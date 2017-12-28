package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import magnolia.{CaseClass, Magnolia, SealedTrait}
import scala.collection.JavaConverters._

import scala.language.experimental.macros

trait DynamosReader[+A] {
  def read(a: AttributeValue): A
}

object DynamosReader {

  type Typeclass[T] = DynamosReader[T]

  def combine[T](caseClass: CaseClass[Typeclass, T]): DynamosReader[T] = new DynamosReader[T] {
    override def read(a: AttributeValue): T = {
      val params = a.getM.asScala // TODO check what happens when getM is not valid
      caseClass.construct { p => p.typeclass.read(params(p.label)) }
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): DynamosReader[T] = new DynamosReader[T] {
    override def read(a: AttributeValue): T = {
      val typeName = a.getM.asScala("dynamos-type")
      val subtype = sealedTrait.subtypes.find(_.label == typeName.getS).get
      subtype.typeclass.read(a)
    }
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
