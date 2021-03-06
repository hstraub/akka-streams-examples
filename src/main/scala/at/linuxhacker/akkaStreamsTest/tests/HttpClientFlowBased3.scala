package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.io.Implicits._
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success, Try }

object HttpClientFlowBased3 extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "INFO"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val stdoutSink = Sink.foreach( println )
  val convertFlow = Flow[ByteString].map( _.utf8String )
  val jsonStringFlow = Flow[String].fold( "" )( ( r, e ) => r + e )
  val dataCountFlow = Flow[String].fold( 0 )( ( a, b ) => a + 1 )
  val dataByteCountFlow = Flow[String].fold( 0 )( ( r, e ) => r + e.length )

  def store( destination: String, json: String ): Unit = {
    val r = Http().singleRequest( HttpRequest( uri = destination, method = HttpMethods.POST, entity = HttpEntity( MediaTypes.`application/json`, json ) ) )
    r.map { response =>
      val source = response.entity.getDataBytes()
      val flowGraph = FlowGraph.closed( ) { implicit b =>
        import FlowGraph.Implicits._
        source ~> convertFlow ~> jsonStringFlow.map { x => s"ElasticSearch returns: $x" } ~> stdoutSink
      }
      flowGraph.run
      println( s"StatusCode: $response.status" )
    }
  }
  
  
  def combined( url: String, destUrl: String, response: HttpResponse ): Unit = {
    val dataSource = response.entity.getDataBytes()
    val jsonStoreFlow = Flow[String].map { x => store( destUrl, x ) }
    val flowGraph = FlowGraph.closed() {  implicit b =>
      import FlowGraph.Implicits._
      val x1 = b.add( Broadcast[String](3) )

      dataSource ~> convertFlow ~> x1
      //x1 ~> stdoutSink
      x1 ~> jsonStringFlow ~> jsonStoreFlow ~> Sink.ignore
      x1 ~> dataCountFlow.map { x => s"url: $url Summe: $x" } ~> stdoutSink
      x1 ~> dataByteCountFlow.map { x => s"url: $url: Summe ByteCount: $x" } ~> stdoutSink
    }
    flowGraph.run
    /* 
     * Futures for RunnableGraph see:
     * https://github.com/akka/akka/blob/12d7dbf41337a095e1b9a5fd253458311375e316/akka-stream-tests/src/test/scala/akka/stream/scaladsl/GraphBalanceSpec.scala#L144-L148
     */
  }
  
  val esUrl = "http://vnagmon02:9200/test/test" // Elasticsearch destination url
  val url1 = "http://vnagmon02:9200" // Elasticsearch input url 
  val url2 = "http://vkavdoc01:5984/testdb/test_structure" // CouchDB input url
  val urls = List( url1, url2  )
  urls.foreach { url =>
    val response = Http().singleRequest( HttpRequest( uri = url ) )
    response.map { x => combined( url, esUrl, x ) }
  }
  
  
  Thread.sleep( 3000 ) // Playing with timeout values and enable debug -> Cancelling ... (after: 5000 ms)

  println( "******** Shutdown All Connection Pools ************")
  Http().shutdownAllConnectionPools()

  Thread.sleep( 5000 )
  println( "******** Shutdown ************")

  system.shutdown
  
}