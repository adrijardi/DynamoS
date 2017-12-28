package com.coding42.dynamos

import com.coding42.dynamos.DynamosReader.Typeclass
import magnolia.Magnolia

trait DynamosFormat[A] extends DynamosReader[A] with DynamosWriter[A]

// TODO
//object DynamosFormat {
//  implicit def gen[T] = new DynamosFormat[T] = {
//
//  }
//}