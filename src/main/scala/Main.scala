import akka.actor.{ActorSystem, Props}
import akka.pattern.Patterns._
import db.{DB, EnableDBMessage, InitDBMessage}
import ui.UI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}
/**
  * Created by liuyufei on 27/02/17.
  */
object Main extends App{

  val system = ActorSystem("SearchEngine")
  val dbActor = system.actorOf(Props[DB],name="dbActor")
  //start db connection
  dbActor ! EnableDBMessage
  //init db data
  val futureOfFuture = ask(dbActor, InitDBMessage, 10 seconds)
  futureOfFuture.onComplete{
    case Success(value) =>  println("init db result "+value)
    case Failure(e) => e.printStackTrace()
  }
  val ui = new UI(dbActor)
  ui.visible = true

}
