# This project

Learning Akka Streams 1.0 with simple examples.

# Examples

* **InMemory**: running a simple stream.
* **DefineStreams**: running a simple Sink.fold stream - calculate the sum of an integer stream.
* **ReactiveTweets**: stream filters case class objects.
* **FileReader**: reading file content and output the content to stdout
* **EchoServer**: an echo tcp server implementation in four lines :)
* **SimpleServer**: tcp server understands one command.
* **ReplClient**: tcp server understands two commands.
* **SinkActorRef**: connect a simple actor as sink.
* **MyActorSubscriber**: ActorSubscriber example
* **HttpClientFlowBased**: reading from urls and write the content to stdout and calculates the packet and byte count. Two implementations: onComplete and functional.
* **HttpClientFlowBased2**: reading from urls and write the content to stdout and calculates the packet and byte count (functional).
* **HttpClientFlowBased3**: reading from Elasticsearch or CouchDb and store the content into Elasticsearch. Implementation with two asynchrous streams.
* **Group**: Using Flow.group and groupWithin: Chunk up this stream into groups of the given size or within a time window. Todo: terminate a running stream.
* **BalanceMerge**: Using Balance and merge. Split the implementation in a Flows and Graphs object.
* **TcpServer2**: Same as EchoServer, but reusing the Flows and Graphs definitions from BalanceMerge.
* **HttpLowLevelServer**: using the akka http low level server API.
* **HttpLowLevelServerAsync**: the async implementation.
* **HttpLowLevelServerAsyncFlow**: running a flow on the HttpRequest data.
* **HttpHighLevelServerMinimal**: using the akka http high level server API and requires akka-http-xml-experimental (build.sbt).
* **HttpHighLevelServerRoutes**: playing with route composition.

## Requirements

* Some examples needs a running Elasticsearch installation. See [Elasticsearch Setup and Installation instructions](https://www.elastic.co/guide/en/elasticsearch/reference/current/setup.html)

# Build instructions

Requirements: java >= 1.6, [sbt](http://www.scala-sbt.org/), optional [scala-ide](http://scala-ide.org/)

```
git clone ...
```

Build and run:

```
sbt run
```

Prepare Scala-Ide:

```
sbt eclipse
```

and import project.

# Documents

## 

* [Akka Streams 1.0 API documentation / Scala](http://doc.akka.io/api/akka-stream-and-http-experimental/1.0/?_ga=1.213244755.69529561.1441703011#package)
* [Reference documentation / Scala](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0/scala.html?_ga=1.213244755.69529561.1441703011)

## Typesafe Webinars

* [Reactive Streams 1.0.0 and why you should care](https://www.youtube.com/watch?v=jwnrZ4bK5hs)

## Worth reading

* [Do's and Dont's When Deploying Akka in Production](http://boldradius.com/blog-post/U-jexSsAACwA_8nr/dos-and-donts-when-deploying-akka-in-production)
* [Reactive Kafka source code](https://github.com/softwaremill/reactive-kafka)
* [Akka Reactive Streams example](https://github.com/pkinsky/akka-streams-example)
* [Connecting dots with Akka Streams - microservice, implementing sink and source - error handling](http://fehmicansaglam.net/connecting-dots-with-akka-stream/)
* [Comparing Akka Streams and Scalaz Streams with code examples](https://softwaremill.com/comparing-akka-stream-scalaz-stream/)
* [Java Code Geeks - Building a REST service in Scala with Akka HTTP, Akka Streams and reactive mongo](http://www.javacodegeeks.com/2015/02/building-a-rest-service-in-scala-with-akka-http-akka-streams-and-reactive-mongo.html)
