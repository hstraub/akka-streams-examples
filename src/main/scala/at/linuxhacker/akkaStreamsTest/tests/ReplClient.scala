package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.stream.scaladsl.{ Tcp, Flow }
import akka.stream.stage.{ Context, PushStage, SyncDirective }
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

object ReplClient extends App {
  
  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  
  val connection = Tcp().outgoingConnection( "127.0.0.1", 25 )
  
  val replParser = new PushStage[String, ByteString] {
    override def onPush( elem: String, ctx: Context[ByteString] ): SyncDirective = {
      elem match {
        case "q" => ctx.pushAndFinish( ByteString( "Bye\n" ) )
        case _ => ctx.push( ByteString(  s"$elem\n" ) )
      }
    }
  }
  
  val repl = Flow[ByteString]
    .via( Framing.delimiter( ByteString( "\n" ), maximumFrameLength = 256, allowTruncation = true ) )
    .map( _.utf8String )
    .map( text => println( "Text: " + text ) )
    .map( _ => readLine( "> " ) )
    .transform( () => replParser )
    
  connection.join( repl ).run
  
}