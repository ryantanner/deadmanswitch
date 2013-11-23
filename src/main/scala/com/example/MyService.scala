package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

import java.security.MessageDigest
import java.math.BigInteger

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    pathPrefix("heartbeat" / Segment / Segment / LongNumber) { (endpointId: String, secretValue: String, timestamp: Long) =>
      get {
        if (authenticate(endpointId, secretValue, timestamp)) {
          respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              <html>
                <body>
                  Successful heartbeat
                </body>
              </html>
            }
          }
        } else {
          respondWithStatus(401) {
            complete {
              <html>
                <body>
                  Could not authenticate heartbeat
                </body>
              </html>
            }
          }
        }
      }
    }

  def authenticate(endpointId: String, secretValue: String, timestamp: Long): Boolean = {
    val expectedTime = System.currentTimeMillis
    val sharedSecret = getSharedSecret(endpointId)
    val hash = {
      val digest = MessageDigest.getInstance("MD5").digest((sharedSecret + timestamp.toString).getBytes)
      val bigInt = new BigInteger(1, digest)
      println(bigInt.toString(16))
      bigInt.toString(16)
    }

    (timestamp > (expectedTime - 5000) && (timestamp < (expectedTime + 5000)) &&
      hash == secretValue)
  }

  def getSharedSecret(endpointId: String): String = {
    "secret"
  }

}
