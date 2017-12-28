package com.coding42.dynamos

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.dynamodb.impl.DynamoSettings
import akka.stream.alpakka.dynamodb.scaladsl.DynamoClient
import org.scalatest.{Matchers, OptionValues, WordSpec}
import akka.stream.alpakka.dynamodb.scaladsl.DynamoImplicits._
import com.amazonaws.services.dynamodbv2.model._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import scala.concurrent.ExecutionContextExecutor
import scala.collection.JavaConverters._
import DefaultDynamosFormat._
import DynamosWriter._
import DynamosReader._

class DynamosSpec extends WordSpec with ScalaFutures with OptionValues with IntegrationPatience with Matchers {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val client = DynamoClient(DynamoSettings(system))

  private val tableName = "test-table"

  private val table = new CreateTableRequest(
    List(new AttributeDefinition("id", ScalarAttributeType.S)).asJava,
    tableName,
    List(new KeySchemaElement("id", KeyType.HASH)).asJava,
    new ProvisionedThroughput(5, 5)
  )
  val listTablesResult: CreateTableResult = client.single(table).futureValue

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
    val item = Map("id" -> new AttributeValue("test1"), "value" -> new AttributeValue("aValue"))

    client.single(new PutItemRequest(tableName, item.asJava)).futureValue
    val result = client.single(new GetItemRequest(tableName, Map("id" -> new AttributeValue("test1")).asJava)).futureValue
    result.getItem.asScala shouldBe item
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
    val id = "lists Id"
    val item = TestList(id, Range(0, 100).toList.map(_.toString))
    storeAndRetrieve(id, item)
  }

  "Handles empty lists" in {
    val id = "empty lists Id"
    val item = TestList(id, Nil)
    storeAndRetrieve(id, item)
  }

  "Handles sets" in {
    val id = "sets Id"
    val item = TestSet(id, Range(0, 100).map(_.toString).toSet)
    storeAndRetrieve(id, item)
  }

  "Handles empty sets" in {
    val id = "empty sets Id"
    val item = TestSet(id, Set.empty)
    storeAndRetrieve(id, item)
  }

  "Handles maps" in {
    val id = "maps Id"
    val item = TestMap(id, Range(0, 100).map(n => n.toString -> n).toMap)
    storeAndRetrieve(id, item)
  }

  "Handles empty maps" in {
    val id = "empty maps Id"
    val item = TestMap(id, Map.empty)
    storeAndRetrieve(id, item)
  }

  "Handles options" in {
    val id = "option Id"
    val item = TestOption(id, Some(42f))
    storeAndRetrieve(id, item)
  }

  "Handles empty options" in {
    val id = "empty option Id"
    val item = TestOption(id, None)
    storeAndRetrieve(id, item)
  }

  "Handles empty strings" in {
    val id = "EmptyStringId"
    val item = Test1(id, 1234, 12.42, true, "")
    storeAndRetrieve(id, item)
  }

  "Handles sealed traits subclasses" in {
    val idA = "SubClassA"
    val itemA: TestTrait = SubclassA(idA, "myStr")
    storeAndRetrieve(idA, itemA)

    val idB = "SubClassB"
    val itemB: TestTrait = SubclassB(idB, 42)
    storeAndRetrieve(idB, itemB)
  }

  private def storeAndRetrieve[A : DynamosWriter : DynamosReader](id: String, item: A) = {
    client.single(new PutItemRequest(tableName, item.toDynamoDb.getM)).futureValue

    val result = client.single(new GetItemRequest(tableName, Map("id" -> id).toDynamoKey)).futureValue
    Dynamos.fromDynamo[A](result).value shouldBe Right(item)
  }
}
