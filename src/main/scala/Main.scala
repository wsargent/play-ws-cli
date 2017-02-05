import javax.cache.{CacheManager, Caching}

import akka.actor.{Actor, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}

import scala.language.postfixOps
import scala.reflect.ClassTag

object Main {
  import akka.actor.ActorSystem

  private val route: Route = {
    import akka.http.scaladsl.server.Directives._
    respondWithHeader(RawHeader("Cache-Control", "public,max-age=5")) {
      val httpEntity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>")
      complete(httpEntity)
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    system.registerOnTermination(System.exit(0))

    val futureServer = {
      Http().bindAndHandle(route, "localhost", port = 9000)
    }

    val cacheManager = Caching.getCachingProvider.getCacheManager
    system.actorOf(Props(new FakeBrowserActor(cacheManager)), "fakeBrowser")
  }

  class FakeBrowserActor(cacheManager: CacheManager)(implicit mat: Materializer) extends Actor {
    import javax.cache.{Cache, Caching}

    import play.api.libs.ws.ahc._

    import scala.concurrent.duration._

    private implicit val system = context.system

    // Create the standalone WS client with a cache
    private val wsClient = StandaloneAhcWSClient(cache = Some(createCache()))

    private def createCache[K: ClassTag, V: ClassTag](): Cache[K, V] = {
      import javax.cache.configuration.FactoryBuilder.SingletonFactory
      import javax.cache.configuration.MutableConfiguration
      import javax.cache.expiry.EternalExpiryPolicy
      import scala.reflect._

      val kClass = classTag[K].runtimeClass
      val vClass = classTag[V].runtimeClass
      val configuration = new MutableConfiguration()
        .setTypes(kClass, vClass)
        .setStoreByValue(false)
        .setExpiryPolicyFactory(new SingletonFactory(new EternalExpiryPolicy()))
          .asInstanceOf[MutableConfiguration[K, V]]
      cacheManager.createCache("play-ws-cache", configuration)
    }

    override def preStart(): Unit = {
      implicit val ec = system.dispatchers.defaultGlobalDispatcher
      val url = "http://localhost:9000"

      // query every three seconds, ensuring that the cache works...
      system.scheduler.schedule(0 seconds, 3 seconds, self, GET(url))

      // ...and then shut down.
      system.scheduler.scheduleOnce(10 seconds, self, SHUTDOWN)
    }

    override def postStop(): Unit = {
      println("Shutting down ws client!")
      wsClient.close()
      Caching.getCachingProvider.getCacheManager.close()

      println("Shutting down actor!")
      system.terminate()
    }

    override def receive: Receive = {
      case GET(url) =>
        implicit val ec = context.dispatcher
        wsClient.url(url).get().map(response => response.statusText)
      case SHUTDOWN =>
        self ! PoisonPill
    }

  }

  case class GET(url: String)
  case object SHUTDOWN

}
