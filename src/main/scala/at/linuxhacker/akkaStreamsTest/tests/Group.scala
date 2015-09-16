package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import com.typesafe.config.ConfigFactory
import scala.util.control.NonFatal

object Group extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "INFO"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()   
  implicit val ec = system.dispatcher
  
  /*
   * Flow.grouped see:
   * http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/?_ga=1.217075793.69529561.1441703011#akka.stream.scaladsl.Flow
   */
  val source = Source( 1l to 100l )
  val sink = Sink.foreach( println )
  val flowGroup = Flow[Long].grouped( 20 ).map { x => x.mkString( "," ) }
  val flowGroupWithin = Flow[Long].groupedWithin( 20, 800 millis ).map { x => x.mkString( "," ) }
  val x = source.via( flowGroup ).toMat(sink)(Keep.right).run
  
  Await.result( x, 10 seconds )
  val x2 = Source( 0 seconds, 200 millis, { System.currentTimeMillis } ).via( flowGroup ).toMat(sink)(Keep.right).run
  
  try Await.result( x2, 20 seconds )
  catch { case NonFatal( e ) =>  println( "Timedout" ) }
  /*
   * FIXME: How I can terminate the running Stream x2?
   * See: http://stackoverflow.com/questions/31378978/how-to-properly-stop-akka-streams-from-the-outside
   */
  
  val x3 = Source( 0 seconds, 200 millis, { System.currentTimeMillis } ).via( flowGroupWithin ).toMat(sink)(Keep.right).run
  
  try Await.result( x3, 20 seconds )
  catch { case NonFatal( e ) =>  println( "Timedout" ) }
  finally {
    println( "System Shutdown" )
    system.shutdown
  }
  
}