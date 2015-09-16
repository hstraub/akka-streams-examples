package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent._
import com.typesafe.config.ConfigFactory

object HttpLowLevelServerAsync extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  implicit val ex = system.dispatcher
  
  val serverSource = Http().bind( interface = "localhost", port = 9001 )
  
  val requestHandler: HttpRequest => Future[HttpResponse] = {

    case HttpRequest( GET, Uri.Path( "/" ), _, _, _ ) =>
      Future { HttpResponse( entity = HttpEntity( MediaTypes.`text/html`, "<html><body>Test</body></html>" ) ) }

    case HttpRequest( GET, Uri.Path( "/tester" ), _, _, _ ) =>
      Future { HttpResponse( entity = HttpEntity( MediaTypes.`text/html`, "<html><body>Test</body></html>" ) ) }

    case HttpRequest( GET, Uri.Path( "/ping" ), _, _, _ ) =>
      Future { HttpResponse( entity = "PONG!" ) }

    case HttpRequest( GET, Uri.Path( "/crash" ), _, _, _ ) =>
      sys.error( "BOOM!" )

    case _: HttpRequest =>
      Future { HttpResponse( 404, entity = "Unknown resouse!" ) }
      
  }
  
  serverSource.runForeach { connection =>
    connection.handleWithAsyncHandler { x => requestHandler( x ) }
    //connection.handleWith( Flow[HttpRequest].mapAsync(3)(requestHandler( _ ) ) )
  }
  
  Thread.sleep( 30000 )
  system.shutdown
  
}