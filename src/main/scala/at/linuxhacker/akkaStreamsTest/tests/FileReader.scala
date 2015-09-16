package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.stream.ActorMaterializer
import akka.stream.io.SynchronousFileSource
import akka.stream.io.Implicits._
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import java.io.File
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

object FileReader extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
    
  val file = new File( "test.txt" )
  
  val flow = SynchronousFileSource( file )
    //.to( Sink.ignore )
    .to( Sink.outputStream( () => System.out ) )
    
  val f = flow.run
  
  Await.result( f, 1 minute )
  system.shutdown
}