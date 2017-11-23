package com.coding42.dynamos

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.List
import scala.language.higherKinds

trait DefaultDynamosReaders {

  implicit val stringReader: DynamosReader[String] = (a: AttributeValue) => a.getS

  implicit val longReader: AttributeValue => Long = (a: AttributeValue) => a.getN.toLong

  implicit val doubleReader: AttributeValue => Double = (a: AttributeValue) => a.getN.toDouble

  implicit val boolReader: AttributeValue => Boolean = (a: AttributeValue) => a.getBOOL

  implicit def collectionReader[A, C[A]](implicit aReader: DynamosReader[A], bf: CanBuildFrom[List[A], A, C[A]]): DynamosReader[C[A]] =
    (a: AttributeValue) => {
      val as: List[A] = a.getL.asScala.map(aReader.read).toList
      bf(as).result()
    }

  implicit def mapReader[A](implicit aReader: DynamosReader[A]): DynamosReader[Map[String, A]] =
    (a: AttributeValue) => a.getM.asScala.map { case (k, v) => k -> aReader.read(v) }.toMap

}

object DefaultDynamosReaders extends DefaultDynamosReaders