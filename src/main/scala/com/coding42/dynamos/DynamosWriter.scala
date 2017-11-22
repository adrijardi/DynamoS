package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import magnolia.{CaseClass, Magnolia, SealedTrait}

import scala.collection.JavaConverters._
import scala.language.experimental.macros


trait DynamosWriter[-A] {
  def write(a: A): AttributeValue
}

object DynamosWriter {

  type Typeclass[T] = DynamosWriter[T]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = new DynamosWriter[T] {
    override def write(t: T): AttributeValue = {
      val parametersMap = caseClass.parameters.map { p =>
        p.label -> p.typeclass.write(p.dereference(t))
      }
      new AttributeValue().withM(parametersMap.toMap.asJava)
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = new DynamosWriter[T] {
    override def write(t: T): AttributeValue = {
      sealedTrait.dispatch(t) { subtype =>
        subtype.typeclass.write(subtype.cast(t))
      }
    }
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
