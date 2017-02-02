package play.api.libs.ws.ahc.cache

import javax.cache.{Cache, Caching}
import javax.cache.configuration.FactoryBuilder.SingletonFactory
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.EternalExpiryPolicy

import play.api.libs.ws.ahc.StandaloneAhcWSClient

/**
 *
 */
class CachingSpec extends Specification {


  private val route = {
    import akka.http.scaladsl.server.Directives._
    headerValueByName("X-Request-Id") { value =>
      respondWithHeader(RawHeader("X-Request-Id", value)) {
        val httpEntity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>")
        complete(httpEntity)
      }
    } ~ {
      get {
        parameters('key.as[String]) { (key) =>
          val httpEntity = HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello to akka-http, key = $key</h1>")
          complete(httpEntity)
        }
      }
    }
  }

  private val futureServer = {
    Http().bindAndHandle(route, "localhost", testServerPort)
  }

  override def afterAll = {
    futureServer.foreach(_.unbind())
    client.close()
    system.terminate()
  }

  "cache" should {

    "test" in {

      // Create the standalone WS client with a cache
      val wsClient = StandaloneAhcWSClient(httpCache = Some(AhcHttpCache(createCache())))

      def createCache(): Cache[CacheKey, CacheEntry] = {
        val cacheManager = Caching.getCachingProvider.getCacheManager
        val configuration = new MutableConfiguration()
          .setTypes(classOf[CacheKey], classOf[CacheEntry])
          .setStoreByValue(false)
          .setExpiryPolicyFactory(new SingletonFactory(new EternalExpiryPolicy()))
        cacheManager.createCache("play-ws-cache", configuration)
      }
    }

  }
}
