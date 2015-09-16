package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.stream.scaladsl.{ Broadcast, Flow, FlowGraph, Keep, Source }
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.stream.ActorMaterializer
import scala.concurrent._
import com.typesafe.config.ConfigFactory

object HttpHighLevelServerRoutes extends App {

  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher
  
  val route = 
    path( "hello" ) {
      get {
        complete {
          <p>Test</p>
        }
      }
    }
  
  val route2 = 
    path( "order" / IntNumber ) { id =>
      get {
        complete ( Future { Thread.sleep( 3000 ); s"Get with id $id" } )
      } ~
      put {
        complete( s"Put with id $id" )
      }
      
    }
  
  val xy = complete { "tester" }
  val route3 = 
    path( "tester" / IntNumber ) { id =>
      get {
        xy
      } ~
      ( post & entity( as[String] ) ) { id =>
        complete { s"POST id: $id" }
      }
    }
  
  val completeRoute = route ~ route2 ~ route3
  val bindingFuture = Http().bindAndHandle( completeRoute , "localhost", 9001 )
  
  Thread.sleep( 30000 )
  bindingFuture
    .flatMap( _.unbind )
    .onComplete( _ => system.shutdown )
}