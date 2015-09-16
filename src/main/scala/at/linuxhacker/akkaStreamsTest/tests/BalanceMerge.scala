package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.stream.{ ActorMaterializer, Graph, FlowShape }
import akka.stream.FlowShape
import akka.stream.scaladsl._
import akka.stream.scaladsl.FlowGraph.Implicits._
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory

object Flows {

  def balancerGraph( balancerCount: Int ) =  FlowGraph.partial( ) { implicit builder =>
    val balancer = builder.add( Balance[String]( balancerCount ) )
    val merger = builder.add( Merge[String]( balancerCount ) )
    val flow1 = Flow[String].map{ x => s"$balancer.in $x" }
    val flow2 = Flow[String].map{ x => x }

    for ( _ <- 1 to balancerCount )
      balancer ~> flow1 ~> merger

    FlowShape( balancer.in, merger.out )
  }
  
}

object Graphs {
  
  
  def test( in: Source[String, Unit], out: Sink[Any,Future[Unit]], balancerCount: Int ): RunnableGraph[Unit] = {
  
  FlowGraph.closed( ) { implicit builder =>
    in ~> Flows.balancerGraph(balancerCount) ~> out
  }

  }
}

object BalanceMerge extends App {

  
  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer() 

  val source = Source( 1 to 100 )
  val convFlow = Flow[Int].map { x => x.toString }
  val sink = Sink.foreach( println )
  
  Graphs.test( source.via( convFlow ), sink, 2 ).run
  
  Thread.sleep( 2000 )
  println( "System Shutdown")
  system.shutdown
 
}