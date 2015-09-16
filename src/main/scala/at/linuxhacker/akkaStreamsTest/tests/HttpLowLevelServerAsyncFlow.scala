package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.scaladsl.FlowGraph.Implicits._
import akka.util.ByteString
import scala.concurrent._
import com.typesafe.config.ConfigFactory

object MyHttpFlows {
  val stdoutSink = Sink.foreach( println )
  val convertFlow = Flow[ByteString].map( _.utf8String )
  val jsonStringFlow = Flow[String].fold( "" )( ( r, e ) => r + e )
  val dataCountFlow = Flow[String].fold( 0 )( ( a, b ) => a + 1 )
  val dataByteCountFlow = Flow[String].fold( 0 )( ( r, e ) => r + e.length )  
}

object MyRequestHandlers {
  
  def myHandler( request: HttpRequest )( implicit mat: ActorMaterializer, ec: ExecutionContextExecutor ): Future[HttpResponse] = {
    request match {

      case HttpRequest( GET, Uri.Path( "/" ), _, _, _ ) =>
        Future { HttpResponse( entity = HttpEntity( MediaTypes.`text/html`, "<html><body>Test</body></html>" ) ) }

      case HttpRequest( GET, Uri.Path( "/ping" ), _, _, _ ) =>
        Future { HttpResponse( entity = "PONG!" ) }

      /*
       *  transmit data with curl:
       *  $ curl -XPOST -d@testfile http://localhost/tester
       */
      case HttpRequest( POST, Uri.Path( "/tester" ), _, _, _ ) =>
        // See http Client with runFold: https://gist.github.com/searler/d7b8580ec864b126558a
        val resultFuture = request.entity.dataBytes.via( MyHttpFlows.convertFlow ).runFold( "" )( _ + _ )
        
        resultFuture.map { x => HttpResponse( entity = x ) }

      case HttpRequest( GET, Uri.Path( "/crash" ), _, _, _ ) =>
        sys.error( "BOOM!" )

      case _: HttpRequest =>
        Future { HttpResponse( 404, entity = "Unknown resouse!" ) }

    }
  }
  
}


object HttpLowLevelServerAsyncFlow extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher
  
  
  val serverSource = Http().bind( interface = "localhost", port = 9001 )
  
  serverSource.runForeach { connection =>
    connection.handleWithAsyncHandler { request => MyRequestHandlers.myHandler( request ) }
    //connection.handleWith( Flow[HttpRequest].mapAsync(3)(MyRequestHandlers.myHandler( _ ) ) )
  }
  
  Thread.sleep( 30000 )
  system.shutdown
  
}