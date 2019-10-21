package com.coding42.dynamos

import magnolia.{CaseClass, Magnolia, SealedTrait}
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

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
      AttributeValue.builder().m(parametersMap.toMap.asJava).build()
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): DynamosWriter[T] = new DynamosWriter[T] {
    override def write(a: T): AttributeValue =
      sealedTrait.dispatch(a) { subtype =>
        val updatedMap = subtype.typeclass
          .write(subtype.cast(a))
          .m()
          .asScala
          .updated("dynamos-type", AttributeValue.builder().s(subtype.typeName.full).build())
        AttributeValue.builder().m(updatedMap.asJava).build()
      }
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
