package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

case object Tick

class TestActor extends Actor with ActorLogging {
  println( "TestActor starting" )
  /*
   * Warning
   * There is no back-pressure signal from the destination actor, i.e. if the actor 
   * is not consuming the messages fast enough the mailbox of the actor will grow. 
   * For potentially slow consumer actors it is recommended to use a bounded mailbox 
   * with zero mailbox-push-timeout-time or use a rate limiting stage in front of this stage.
   * 
   * See: http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0/scala/stream-integrations.html
   * And: http://doc.akka.io/docs/akka/snapshot/scala/mailboxes.html
   * 
   */
  
  var counter = 0
  
  def receive = {
    case Tick => 
      counter += 1
      println( s"Tick object received nr $counter.")
  } 
}

object SinkActorRef extends App {
  
  val customConf = ConfigFactory.parseString( """
akka.loglevel = "INFO"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()  
  
  val tickSource = Source( 0 seconds, 200 millis, Tick )
  val sink = system.actorOf( Props[TestActor] )
  
  val cancellable = tickSource.to( Sink.actorRef( sink, "completed" ) ).run
  
  Thread.sleep( 5000 )
  println( "Shutdown." )
  system.shutdown
  
}