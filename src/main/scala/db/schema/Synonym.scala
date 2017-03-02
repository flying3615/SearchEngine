package db

import akka.actor.Actor
import akka.actor.Actor.Receive
import slick.ast.ColumnOption.PrimaryKey
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Success}

/**
  * Created by liuyufei on 27/02/17.
  */

class DB extends Actor {

  val synonyms = TableQuery[Synonyms]

  case class Synonym(root_word: String, synonyms: String)

  // Definition of the Synonym table, table of Synonym
  class Synonyms(tag: Tag) extends Table[Synonym](tag, "Synonym") {
    def root_word = column[String]("root_word", PrimaryKey)

    // This is the primary key column
    def synonyms = column[String]("synonyms")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (root_word, synonyms) <> (Synonym.tupled, Synonym.unapply)
  }

  var db:Database = _

  def initDB = {
    val inputData = Source.fromResource("synonym_words.txt").getLines().map(s => {
      val item = s.split(":")
      Synonym(item(0), item(1))
    }).toList

    println("input size " + inputData.size)

    val allTasks = synonyms.schema.create.flatMap(_ => synonyms ++= inputData).flatMap(_ => synonyms.result)
    db.run(allTasks)

    //    println(Await.result(trueResult, Duration.Inf).take(1))
    //
    //    val conditionQuery = synonyms.filter(_.root_word === "abandonment").map(_.synonyms).result
    //    db.run(conditionQuery)
  }


  def closeDB = {
    if (db != null) db.close()
  }

  def connectDB = {
    db = Database.forConfig("h2mem")
  }

  override def receive: Receive = {
    case InitDBMessage =>
      val initDBResult = Await.result(initDB, 2 seconds)
      sender ! initDBResult.size
    case EnableDBMessage =>connectDB;println("enable DB")
    case DisableDBMessage => closeDB; println("disable DB")
    case _ =>
  }
}

case object InitDBMessage

case object EnableDBMessage

case object DisableDBMessage


