import akka.actor.{Actor, PoisonPill, Props}
import akka.stream.{ActorMaterializer, Materializer}

import scala.language.postfixOps

object Main {
  import akka.actor.ActorSystem

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    system.registerOnTermination(System.exit(0))

    implicit val materializer = ActorMaterializer()
    system.actorOf(Props(new FakeBrowserActor), "fakeBrowser")
  }

  class FakeBrowserActor()(implicit mat: Materializer) extends Actor {
    import javax.cache.configuration.FactoryBuilder.SingletonFactory
    import javax.cache.configuration.MutableConfiguration
    import javax.cache.expiry.EternalExpiryPolicy
    import javax.cache.{Cache, Caching}

    import play.api.libs.ws.ahc.cache.{AhcHttpCache, CacheEntry, CacheKey}

    import scala.concurrent.duration._

    import play.api.libs.ws.ahc._

    private implicit val system = context.system

    // Create the standalone WS client with a cache
    private val wsClient = StandaloneAhcWSClient(httpCache = Some(AhcHttpCache(createCache())))

    private def createCache(): Cache[CacheKey, CacheEntry] = {
      val cacheManager = Caching.getCachingProvider.getCacheManager
      val configuration = new MutableConfiguration()
        .setTypes(classOf[CacheKey], classOf[CacheEntry])
        .setStoreByValue(false)
        .setExpiryPolicyFactory(new SingletonFactory(new EternalExpiryPolicy()))
      cacheManager.createCache("play-ws-cache", configuration)
    }

    override def preStart(): Unit = {
      implicit val ec = system.dispatchers.defaultGlobalDispatcher
      val url = "https://playframework.com"

      // query every five seconds, ensuring that the cache works...
      system.scheduler.schedule(0 seconds, 5 seconds, self, GET(url))

      // ...and then shut down.
      system.scheduler.scheduleOnce(30 seconds, self, SHUTDOWN)
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
