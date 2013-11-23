package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

import java.security.MessageDigest
import java.math.BigInteger

class MyServiceSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system
  
  "MyService" should {

    val time = System.currentTimeMillis.toString
    val secretValue = {
      val digest = MessageDigest.getInstance("MD5").digest(("secret" + time).getBytes)
      val bigInt = new BigInteger(1, digest)
      println(bigInt.toString(16))
      bigInt.toString(16)
      //bigInt.toString(16).reverse.padTo(32, "0").reverse.mkString
    }

    "return success for valid heartbeats" in {
      Get(s"/heartbeat/myEndpointId/$secretValue/$time") ~> sealRoute(myRoute) ~> check {
        responseAs[String] must contain("Successful heartbeat")
      }
    }

    "return 401 for invalid timestamps" in {
      val badTime = (System.currentTimeMillis - 10000).toString
      Get(s"/heartbeat/myEndpointId/$secretValue/$badTime") ~> sealRoute(myRoute) ~> check {
        status === Unauthorized
      }
    }

    "return 401 for invalid secrets" in {
      Get(s"/heartbeat/myEndpointId/fdafdsafadfasdf/$time") ~> sealRoute(myRoute) ~> check {
        status === Unauthorized
      }
    }

  }
}
