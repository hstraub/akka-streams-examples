package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.io.Implicits._
//import akka.stream.scaladsl.{ Broadcast, Flow, FlowGraph, Keep, Sink }
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success, Try }

object HttpClientFlowBased extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "INFO"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher
  
  /* old part
  def onCompleteFunction( x: Try[HttpResponse] ): Unit = {
    x match {
      case Success( response ) =>
        getData( response )
        toStdout( response )
      case Failure( e ) => println( e )
    }
  }

  def toStdout( response: HttpResponse ): Unit = {
    val flow = Flow[ByteString].map( _.utf8String )
    val dataSource = response.entity.getDataBytes()
    val dataSink = Sink.foreach( println )
    val dataGraph = dataSource.via( flow ).to( dataSink )
    dataGraph.run( mat )
  }

  def getData( response: HttpResponse ): Unit = {
    val flow = Flow[ByteString].map( _.utf8String )
    val dataSource = response.entity.getDataBytes()
    val dataSink = Sink.fold[Int, String]( 0 )( ( a, b ) => a + 1 )
    val sumFuture = dataSource.via( flow ).runWith( dataSink, mat )
    sumFuture.onComplete { x =>
      x match {
        case Success( sum ) => println( sum )
        case Failure( e ) => println( s"Error: $e" )
      }
    }
  }
  * 
  */

  def combined( url: String, response: HttpResponse ): Unit = {
    val convertFlow = Flow[ByteString].map( _.utf8String )
    val dataSource = response.entity.getDataBytes()
    val dataCountSink = Sink.fold[Int, String]( 0 )( ( a, b ) => a + 1 )
    val dataCountFlow = Flow[String].fold( 0 )( ( a, b ) => a + 1 )
    val dataByteCountFlow = Flow[String].fold( 0 )( (r, e) => r + e.length )
    val stdoutSink = Sink.foreach( println )
    /*
    val dataSink = Sink.fold[Int, String]( 0 )( ( a, b ) => a + 1 )
    val test1 = dataSink.mapMaterializedValue { x =>
        x.onComplete { y =>
          y match {
            case Success( sum ) => println( s">>>>>>>>>>>>>>>>>Summe: $sum" )
            case Failure( e ) => println( s"Error: $e" )
          }
        }
      }
    */
    val flowGraph = FlowGraph.closed( ) {  implicit b =>
      import FlowGraph.Implicits._
      val x1 = b.add( Broadcast[String](2) )

      dataSource ~> convertFlow ~> x1
      // x1 ~> stdoutSink
      x1 ~> dataCountFlow.map { x => s"url: $url Summe: $x" } ~> stdoutSink
      x1 ~> dataByteCountFlow.map { x => s"url: $url: Summe ByteCount: $x" } ~> stdoutSink
      // oldPart: x1 ~>  test1

    }
    flowGraph.run
  }
  
  
  val url1 = "http://localhost:9200" // Elasticsearch input url
  val url2 = "http://localhost:5984/testdb/test_structure" // CouchDb input url
  val urls = List( url1, url2  )
  urls.foreach { url =>
    val response = Http().singleRequest( HttpRequest( uri = url ) )
    response.map { x => combined( url, x ) }
    //response.onComplete { x => onCompleteFunction( x ) }
  }
  
  Thread.sleep( 10000 ) // Playing with timeout values and enable debug -> Cancelling ... (after: 5000 ms)
  println( "******** Shutdown All Connection Pools ************")
  Http().shutdownAllConnectionPools()

  Thread.sleep( 5000 )
  println( "******** Shutdown ************")
  system.shutdown
  
}