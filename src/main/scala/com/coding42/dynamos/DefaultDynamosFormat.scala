package com.coding42.dynamos

trait DefaultDynamosFormat extends DefaultDynamosReaders with DefaultDynamosWriters {

}

object DefaultDynamosFormat extends DefaultDynamosFormat
