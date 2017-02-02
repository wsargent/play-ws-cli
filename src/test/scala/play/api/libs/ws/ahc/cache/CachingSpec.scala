package play.api.libs.ws.ahc.cache

import javax.cache.{Cache, Caching}
import javax.cache.configuration.FactoryBuilder.SingletonFactory
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.EternalExpiryPolicy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.{BeforeAfter, Specification}
import org.specs2.specification.AfterAll
import play.api.libs.ws.ahc.StandaloneAhcWSClient

/**
 *
 */
class CachingSpec(implicit ee: ExecutionEnv) extends Specification with BeforeAfter with AfterAll with FutureMatchers {

  implicit val system = ActorSystem("test")
  implicit val materializer = ActorMaterializer()
  var wsClient: Option[StandaloneAhcWSClient] = None

  private val route: Route = {
    import akka.http.scaladsl.server.Directives._
    respondWithHeader(RawHeader("Cache-Control", "public")) {
      val httpEntity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>")
      complete(httpEntity)
    }
  }

  private val futureServer = {
    Http().bindAndHandle(route, "localhost", port = 9000)
  }

  override def before = {
    wsClient = Some(StandaloneAhcWSClient(httpCache = Some(AhcHttpCache(createCache()))))
  }

  override def after = {
    val cacheManager = Caching.getCachingProvider.getCacheManager
    cacheManager.destroyCache("play-ws-cache")
    wsClient.foreach(_.close())
  }

  override def afterAll = {
    futureServer.foreach(_.unbind())(materializer.executionContext)
    system.terminate()
  }

  def createCache(): Cache[CacheKey, CacheEntry] = {
    val cacheManager = Caching.getCachingProvider.getCacheManager
    val configuration = new MutableConfiguration()
      .setTypes(classOf[CacheKey], classOf[CacheEntry])
      .setStoreByValue(false)
      .setExpiryPolicyFactory(new SingletonFactory(new EternalExpiryPolicy()))
    cacheManager.createCache("play-ws-cache", configuration)
  }

  def ws: StandaloneAhcWSClient = wsClient.get

  "GET" should {

    "work once" in {
      ws.url("http://localhost:9000/").get().map { response =>
        response.body must be("<h1>Say hello to akka-http</h1>")
      }.await
    }

  }
}
