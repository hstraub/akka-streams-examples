package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.stream.scaladsl.{ Concat, Flow, FlowGraph, Source, Tcp }
import akka.stream.stage.{ Context, PushStage, SyncDirective }
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

object SimpleServer extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  
  val helpMessage = """Available commands:
BYE
HELP"""
  
  val connections = Tcp().bind( "127.0.0.1", 9001 )

  connections.runForeach { connection =>

    val serverLogic = Flow() { implicit b =>
      import FlowGraph.Implicits._

      // Server logic, parses incoming commands
      val commandParser = new PushStage[String, String] {
        override def onPush( elem: String, ctx: Context[String] ): SyncDirective = {
          elem match {
            case "BYE" => ctx.finish
            case "HELP" => ctx.push( helpMessage )
            case _ => ctx.push( elem + "!" )
          }
        }
      }

      import connection._
      val welcomeMessage = s"Welcome to $localAddress, you are from $remoteAddress.\n"
      val welcome = Source.single( ByteString( welcomeMessage ) )
      val echo = b.add( Flow[ByteString]
        .via( Framing.delimiter( ByteString( "\n" ), maximumFrameLength = 256, allowTruncation = true ) )
        .map( _.utf8String )
        .map( _.replaceAll( "\r", "" ) )
        .transform( () => commandParser )
        .map( _ + "\n" )
        .map( ByteString( _ ) ) )

      val concat = b.add( Concat[ByteString]() )
      welcome ~> concat.in( 0 )
      echo.outlet ~> concat.in( 1 )
      ( echo.inlet, concat.out )

    }
    
    connection.handleWith( serverLogic )
    
  }
  
}