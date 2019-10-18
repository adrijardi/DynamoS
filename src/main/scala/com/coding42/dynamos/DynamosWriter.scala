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

  def combine[T](caseClass: CaseClass[Typeclass, T]): DynamosWriter[T] = new DynamosWriter[T] {
    override def write(a: T): AttributeValue = {
      val parametersMap = caseClass.parameters.map { p =>
        p.label -> p.typeclass.write(p.dereference(a))
      }
      new AttributeValue().withM(parametersMap.toMap.asJava)
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): DynamosWriter[T] = new DynamosWriter[T] {
    override def write(a: T): AttributeValue =
      sealedTrait.dispatch(a) { subtype =>
        val updatedMap = subtype.typeclass
          .write(subtype.cast(a))
          .getM
          .asScala
          .updated("dynamos-type", new AttributeValue(subtype.typeName.full))
        new AttributeValue().withM(updatedMap.asJava)
      }
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
