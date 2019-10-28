package com.coding42.dynamos

import java.net.URI

import org.scalatest.{Matchers, OptionValues, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import scala.collection.JavaConverters._
import DefaultDynamosFormat._
import DynamosWriter._
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeDefinition,
  AttributeValue,
  CreateTableRequest,
  GetItemRequest,
  GetItemResponse,
  KeySchemaElement,
  KeyType,
  ProvisionedThroughput,
  PutItemRequest,
  ScalarAttributeType
}

import scala.compat.java8.FutureConverters._

class DynamosSpec extends WordSpec with ScalaFutures with OptionValues with IntegrationPatience with Matchers {

  val client: DynamoDbAsyncClient = DynamoDbAsyncClient
    .builder()
    .endpointOverride(URI.create("http://localhost:8000"))
    .build()

  private val tableName = "test-table"

  private val attributeDefinition = AttributeDefinition
    .builder()
    .attributeName("id")
    .attributeType(ScalarAttributeType.S)
    .build()

  private val keySchemaElement = KeySchemaElement
    .builder()
    .attributeName("id")
    .keyType(KeyType.HASH)
    .build()

  private val provisionedThrougput = ProvisionedThroughput
    .builder()
    .readCapacityUnits(5L)
    .writeCapacityUnits(5L)
    .build()

  private val table = CreateTableRequest
    .builder()
    .attributeDefinitions(List(attributeDefinition).asJava)
    .tableName(tableName)
    .keySchema(List(keySchemaElement).asJava)
    .provisionedThroughput(provisionedThrougput)
    .build()

  val listTablesResult = client.createTable(table).toScala.futureValue

  case class Test1(id: String, long: Long, double: Double, boolean: Boolean, string: String)

  implicit val test1Reader: DynamosReader[Test1] = DynamosReader.gen[Test1]

  case class TestList(id: String, list: List[String])

  implicit val testListReader: DynamosReader[TestList] = DynamosReader.gen[TestList]

  case class TestSet(id: String, set: Set[String])

  implicit val testSetReader: DynamosReader[TestSet] = DynamosReader.gen[TestSet]

  case class TestMap(id: String, map: Map[String, Int])

  implicit val testMapReader: DynamosReader[TestMap] = DynamosReader.gen[TestMap]

  case class TestOption(id: String, option: Option[Float])

  implicit val testOptionReader: DynamosReader[TestOption] = DynamosReader.gen[TestOption]

  sealed trait TestTrait

  case class SubclassA(id: String, str: String) extends TestTrait

  case class SubclassB(id: String, i: Int) extends TestTrait

  implicit val testTraitReader: DynamosReader[TestTrait] = DynamosReader.gen[TestTrait]

  "can put and get element created by hand" in {
    val item = Map(
      "id"    -> AttributeValue.builder().s("test1").build(),
      "value" -> AttributeValue.builder().s("aValue").build()
    )

    val putRequest = PutItemRequest
      .builder()
      .tableName(tableName)
      .item(item.asJava)
      .build()

    client.putItem(putRequest).toScala.futureValue

    val getRequest = GetItemRequest
      .builder()
      .tableName(tableName)
      .key(Map("id" -> AttributeValue.builder().s("test1").build()).asJava)
      .build()
    val result: GetItemResponse = client.getItem(getRequest).toScala.futureValue
    result.item().asScala shouldBe item
  }

  "Handles simple case classes" in {
    val item = Test1("myId", 1234, 12.42, true, "string here!")
    storeAndRetrieve("myId", item)
  }

  "Handles top level maps" in {
    val item = Map("id" -> "mapId", "value" -> "aValue")
    storeAndRetrieve("mapId", item)
  }

  "Handles lists" in {
    val id   = "lists Id"
    val item = TestList(id, Range(0, 100).toList.map(_.toString))
    storeAndRetrieve(id, item)
  }

  "Handles empty lists" in {
    val id   = "empty lists Id"
    val item = TestList(id, Nil)
    storeAndRetrieve(id, item)
  }

  "Handles sets" in {
    val id   = "sets Id"
    val item = TestSet(id, Range(0, 100).map(_.toString).toSet)
    storeAndRetrieve(id, item)
  }

  "Handles empty sets" in {
    val id   = "empty sets Id"
    val item = TestSet(id, Set.empty)
    storeAndRetrieve(id, item)
  }

  "Handles maps" in {
    val id   = "maps Id"
    val item = TestMap(id, Range(0, 100).map(n => n.toString -> n).toMap)
    storeAndRetrieve(id, item)
  }

  "Handles empty maps" in {
    val id   = "empty maps Id"
    val item = TestMap(id, Map.empty)
    storeAndRetrieve(id, item)
  }

  "Handles options" in {
    val id   = "option Id"
    val item = TestOption(id, Some(42f))
    storeAndRetrieve(id, item)
  }

  "Handles empty options" in {
    val id   = "empty option Id"
    val item = TestOption(id, None)
    storeAndRetrieve(id, item)
  }

  "Handles empty strings" in {
    val id   = "EmptyStringId"
    val item = Test1(id, 1234, 12.42, true, "")
    storeAndRetrieve(id, item)
  }

  "Handles sealed traits subclasses" in {
    val idA              = "SubClassA"
    val itemA: TestTrait = SubclassA(idA, "myStr")
    storeAndRetrieve(idA, itemA)

    val idB              = "SubClassB"
    val itemB: TestTrait = SubclassB(idB, 42)
    storeAndRetrieve(idB, itemB)
  }

  "fromDynamo parses lists of items" in {
    val initial   = (1 to 10).map(i => TestOption(i.toString, Some(i)))
    val converted = initial.map(_.toDynamoDb.m()).asJava

    Dynamos.fromDynamo[TestOption](converted).toList shouldBe initial.map(Right(_))
  }

  private def storeAndRetrieve[A: DynamosWriter: DynamosReader](id: String, item: A) = {
    val putRequest = PutItemRequest
      .builder()
      .tableName(tableName)
      .item(item.toDynamoDb.m())
      .build()
    client.putItem(putRequest).toScala.futureValue

    val getRequest = GetItemRequest
      .builder()
      .tableName(tableName)
      .key(Map("id" -> id).toDynamoKey)
      .build()
    val result = client.getItem(getRequest).toScala.futureValue
    Dynamos.fromDynamoOp[A](result).value shouldBe Right(item)
  }
}
