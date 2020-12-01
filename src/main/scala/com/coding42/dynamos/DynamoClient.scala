package com.coding42.dynamos

import cats.effect.IO
import com.coding42.dynamos.request.{GetItemReq, Hash, HashRange}
import com.coding42.dynamos.response.GetItemResp
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.{GetItemRequest, GetItemResponse}

import scala.collection.JavaConverters._

class DynamoClient(awsClient: DynamoDbAsyncClient) {

  // TODO improve type
  def getItem[A: DynamosReader](request: GetItemReq): IO[GetItemResp[DynamosResult[A]]] = {
    val key = request.key match {
      case Hash(name, value) => Map(name -> value)
      case HashRange(hashName, hashValue, rangeName, rangeValue) => Map(hashName -> hashValue, rangeName -> rangeValue)
    }
    val fut = awsClient.getItem(GetItemRequest.builder.tableName(request.tableName).key(key.asJava).build)
    val io = IO.async[GetItemResponse] { cb =>
      fut.whenComplete( (r, e) => cb(if(r != null) Right(r) else Left(e)))
    }

    io.map(res => GetItemResp(Dynamos.fromDynamoOp(res.item())))
  }
}
