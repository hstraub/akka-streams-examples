package at.linuxhacker.akkaStreamsTest.tests

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

object InMemory extends App {
  
  /*
   * Example implementation of:
   * https://softwaremill.com/comparing-akka-stream-scalaz-stream/
   */

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  val test = Source( ( 1 to 100 ).toList )
  val flow = test.filter( _ % 2 == 0 ).toMat( Sink.foreach( println ) )( Keep.right )
  //val flow = test.filter( _ % 2 == 0 ).to( Sink.foreach( println ) )
  val flowFuture = flow.run
  
  Await.result( flowFuture, 1 minute )
  
  system.shutdown

}