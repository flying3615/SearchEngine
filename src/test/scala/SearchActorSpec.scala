import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import search.{SearchActor, SearchMessage}

import scala.concurrent.duration._
/**
  * Created by liuyufei on 18/03/17.
  */
class SearchActorSpec extends TestKit(ActorSystem(
  "SearchActorSpec",
  ConfigFactory.parseString(SearchActorSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  val searchActor = system.actorOf(Props[SearchActor],name="searchActor")
  val searchFile = new File(getClass.getResource("/doc4.txt").getFile)

  val searchActorRef = TestActorRef(new SearchActor)

  override def afterAll(): Unit = shutdown()

  "SearchActor" should {
    "respond to the SearchMessage " in {
      within(500 millis) {
        searchActor ! SearchMessage(searchFile.getParentFile,"forecast")
        expectMsgPF(500 millis){
          case msg @ (a:List[String],b:String) =>
            a should have size 1
            b should not be empty
        }
      }
    }

    "throw exception" in {
      intercept[Exception](searchActorRef.receive("error"))
    }
  }
}


object SearchActorSpec {
  val config =
    """
      |akka {
      |   loglevel = "WARNING"
      |}
    """.stripMargin
}
