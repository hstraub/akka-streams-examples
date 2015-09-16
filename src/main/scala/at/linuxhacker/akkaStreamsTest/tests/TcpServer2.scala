package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.http.scaladsl._
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.typesafe.config.ConfigFactory


object TcpServer2 extends App {
  
  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer() 
  
  val connections = Tcp().bind( "127.0.0.1", 9001 )
  connections.runForeach { connection =>
    println( s"New connection from $connection" )
    val x = Flows.balancerGraph(2)
    val flow = Flow[ByteString].map { x => x.utf8String }.via( x ).map { x => ByteString( x ) }
    connection.handleWith( flow )
  }
  
  
}