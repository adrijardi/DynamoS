package com.coding42.dynamos

// TODO is this needed?
trait DynamosFormat[A] extends DynamosReader[A] with DynamosWriter[A]
