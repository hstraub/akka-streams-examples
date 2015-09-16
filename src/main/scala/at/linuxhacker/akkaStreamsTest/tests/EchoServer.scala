package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.stream.scaladsl.{ Tcp, Flow }
import akka.util.ByteString
import com.typesafe.config.ConfigFactory


object EchoServer extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  
  
  val connections = akka.stream.scaladsl.Tcp().bind( "127.0.0.1", 9001 )
  
  val x = connections.runForeach { connection =>
    println( s"New connection from: ${connection.remoteAddress}" )
    val echo = Flow[ByteString]
      .via( Framing.delimiter( ByteString( "\n" ), maximumFrameLength = 256, allowTruncation = true ) )
      .map( _.utf8String.replaceAll( "\r", "" ) )
      .map( _ + "!!!\n" )
      .map{ x => print( x ); x }
      .map( ByteString( _ ) )
      
      connection.handleWith( echo )
    }
  
}