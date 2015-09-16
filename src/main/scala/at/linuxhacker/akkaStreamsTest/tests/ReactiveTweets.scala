package at.linuxhacker.akkaStreamsTest.tests

import akka.actor.{ ActorSystem }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import com.typesafe.config.ConfigFactory
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

final case class Author( handle: String )
final case class Hashtag( name: String )
final case class Tweet( author: Author, timestamp: Long, body: String ) {
  def hashtags: Set[Hashtag] = {
    body.split( " " ).collect{ case t if t.startsWith( "#" ) =>Hashtag( t ) }.toSet
  }
}

object ReactiveTweets extends App {

  val akka = Hashtag( "#akka" )
  val customConf = ConfigFactory.parseString( """
akka.loglevel = "DEBUG"
akka.actor.debug.lifecycle = "on"
akka.actor.debug.receive = "on"
akka.actor.debug.autoreceive = "on"
""" )

  implicit val system = ActorSystem( "test", customConf )
  implicit val mat = ActorMaterializer()
  
  val tweets: Source[Tweet, Unit] = Source( List( 
      Tweet( Author( "author1" ), 1, "Test #akka" ),
      Tweet( Author( "author2" ), 2, "no tags" ) ) )
  
  val authors: Source[Author, Unit] = tweets
    .filter( _.hashtags.contains( akka ) )
    .map( _.author )
    
  val f = authors.runWith( Sink.foreach( println ) )
  // authors.runForeach( println )
  
  Await.result( f, 1 minute )
  system.shutdown
}