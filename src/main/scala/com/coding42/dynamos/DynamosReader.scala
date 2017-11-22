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

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = new DynamosReader[T] {
    override def read(t: AttributeValue): T = {
      val params = t.getM.asScala // TODO check what happens when getM is not valid
      caseClass.construct { p => p.typeclass.read(params(p.label))}
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = new DynamosReader[T] {
    override def read(t: AttributeValue): T = ??? // TODO we need to know the type somehow from the data
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
