package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.actor.{ ActorSubscriber, MaxInFlightRequestStrategy }
import akka.stream.actor.ActorSubscriberMessage._
import com.typesafe.config.ConfigFactory

case class Ticket( id: Int )
case class TicketDone( ticket: Ticket )

class Worker extends Actor with ActorLogging {
  
  log.info( "Worker started.")
  
  def receive = {
    case ticket: Ticket => 
      log.info( "Worker received Ticket $ticket.id" )
      Thread.sleep( 1000 )
      context.sender ! TicketDone( ticket )
  }
}

class MySubscriber extends ActorSubscriber with ActorLogging {
  log.info( "MySubscriber started.")
  
  val worker = context.actorSelection( "akka://test/user/worker" )
  val maxQueueSize = 1
  var count = 0
  
  override val requestStrategy = new MaxInFlightRequestStrategy( max = maxQueueSize ) {
    override def inFlightInternally = count
  }
  
  def receive = {
    case OnNext( ticket: Ticket ) =>
      log.info( s"Tick received id: $ticket.id"  )
      count += 1
      worker ! ticket
      
    case ticketDone: TicketDone =>
      log.info( s"TicketDone $ticketDone.ticket.id" )
      count -= 1
  }
  
}

object MyActorSubscriber extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "INFO"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()  
  
  val worker = system.actorOf( Props[Worker], "worker" )
  println( worker.path )
  
  val source = Source( 1 to 100 ).map( Ticket( _ ) )
  val source2 = Source( 300 to 400 ).map( Ticket( _ ) )
  val sink = Sink.actorSubscriber( Props[MySubscriber] )
  source.to( sink ).run
  Thread.sleep( 3000 )
  source2.to( sink ).run
  val x = source2.to( sink )
 
  Thread.sleep( 10000 )
  println( "Shutdown" )
  system.shutdown
  
}