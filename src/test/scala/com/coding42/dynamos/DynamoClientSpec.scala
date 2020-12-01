package com.coding42.dynamos

import java.net.URI

import com.coding42.dynamos.request.{GetItemReq, Hash}
import com.coding42.dynamos.response.GetItemResp
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, PutItemRequest}
import com.coding42.dynamos.DefaultDynamosFormat._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

class DynamoClientSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience {

  val awsClient: DynamoDbAsyncClient = DynamoDbAsyncClient
    .builder()
    .endpointOverride(URI.create("http://localhost:8000"))
    .region(Region.EU_WEST_1)
    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "pass")))
    .build()

  private val tableName = "test-table-client"

//  val createTablesResult = TableHelpers.createTable(tableName, awsClient).futureValue

  val client = new DynamoClient(awsClient)

  case class TestItem(field: String)

  implicit val test1Reader: DynamosReader[TestItem] = DynamosReader.gen[TestItem]
  implicit val test1Reader: DynamosWriter[TestItem] = DynamosWriter.gen[TestItem]

  "returns empty when no values found" in {
    client.getItem[TestItem](GetItemReq(tableName, Hash("id", AttributeValue.builder().s("someID").build()))).unsafeRunSync() shouldBe GetItemResp(None)
  }

  "returns item values when found" in {
    awsClient.putItem(PutItemRequest.builder.item(T)
    client.getItem[TestItem](GetItemReq(tableName, Hash("id", AttributeValue.builder().s("someID").build()))).unsafeRunSync() shouldBe GetItemResp(None)
  }
}
