package com.coding42.dynamos.request

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

sealed trait Key

// TODO use something more typesafe than AttributeValue
final case class Hash(name: String, value: AttributeValue) extends Key

final case class HashRange(hashName: String, hashValue: AttributeValue, rangeName: String, rangeValue: AttributeValue) extends Key
