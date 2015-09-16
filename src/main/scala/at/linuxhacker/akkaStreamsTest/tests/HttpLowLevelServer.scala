package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent._
import com.typesafe.config.ConfigFactory

object HttpLowLevelServer extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  
  val serverSource = Http().bind( interface = "localhost", port = 9001 )
  
  val requestHandler: HttpRequest => HttpResponse = {

    case HttpRequest( GET, Uri.Path( "/" ), _, _, _ ) =>
      HttpResponse( entity = HttpEntity( MediaTypes.`text/html`, "<html><body>Test</body></html>" ) )

    case HttpRequest( GET, Uri.Path( "/ping" ), _, _, _ ) =>
      HttpResponse( entity = "PONG!" )

    case HttpRequest( GET, Uri.Path( "/crash" ), _, _, _ ) =>
      sys.error( "BOOM!" )

    case _: HttpRequest =>
      HttpResponse( 404, entity = "Unknown resouse!" )
      
  }
  
  val bindingFuture: Future[Http.ServerBinding] = 
    serverSource.to( Sink.foreach { connection =>
      println( s"Accepted new connection from $connection.remoteAddress" )
      //connection.handleWithSyncHandler( requestHandler)
      connection.handleWith { Flow[HttpRequest].map( requestHandler ) }
    } ).run
  
}