package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object HttpHighLevelServerMinimal extends App {

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
  
  val bindingFuture = Http().bindAndHandle( route, "localhost", 9001 )
  
  Thread.sleep( 30000 )
  bindingFuture
    .flatMap( _.unbind )
    .onComplete( _ => system.shutdown )
}