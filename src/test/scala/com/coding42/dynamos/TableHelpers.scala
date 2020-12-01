package com.coding42.dynamos

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeDefinition, CreateTableRequest, CreateTableResponse, KeySchemaElement, KeyType, ProvisionedThroughput, ScalarAttributeType}

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

object TableHelpers {

  def createTable(tableName: String, client: DynamoDbAsyncClient): Future[CreateTableResponse] = {

    val attributeDefinition = AttributeDefinition
      .builder()
      .attributeName("id")
      .attributeType(ScalarAttributeType.S)
      .build()

    val keySchemaElement = KeySchemaElement
      .builder()
      .attributeName("id")
      .keyType(KeyType.HASH)
      .build()

    val provisionedThrougput = ProvisionedThroughput
      .builder()
      .readCapacityUnits(5L)
      .writeCapacityUnits(5L)
      .build()

    val table = CreateTableRequest
      .builder()
      .attributeDefinitions(List(attributeDefinition).asJava)
      .tableName(tableName)
      .keySchema(List(keySchemaElement).asJava)
      .provisionedThroughput(provisionedThrougput)
      .build()

    client.createTable(table).toScala
  }
}
