package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source, Keep }
import com.typesafe.config.ConfigFactory
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

object DefineStreams extends App {
  
  val akka = Hashtag( "#akka" )
  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  
  val source = Source( 1 to 10 )
  val sink = Sink.fold[Int, Int](0)( _ + _ )
  
  val runnable = source.toMat( sink )( Keep.right )
  val runnable2 = source.to( sink ) // has Unit
  
  val sum = runnable.run
  val sum2 = source.runWith( sink )
  
  val res = Await.result( sum, 1 minute )
  println( s"Sum: $res")
  
  val res2 = Await.result( sum2, 1 minute )
  println( s"Sum2: $res2" )
  
  system.shutdown
  
}