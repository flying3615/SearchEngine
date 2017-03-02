package db

import akka.actor.Actor
import slick.ast.ColumnOption.PrimaryKey
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source

/**
  * Created by liuyufei on 27/02/17.
  */

class DB extends Actor {

  val synonyms = TableQuery[Synonyms]

  var db: Database = _

  def initDB = {
    val inputData = Source.fromResource("synonym_words.txt").getLines().map(s => {
      val item = s.split(":")
      Synonym(item(0), item(1))
    }).toList

    println("input size " + inputData.size)

    val allTasks = synonyms.schema.create.flatMap(_ => synonyms ++= inputData).flatMap(_ => synonyms.result)
    db.run(allTasks)

  }


  def closeDB = {
    if (db != null) db.close()
  }

  def connectDB = {
    db = Database.forConfig("h2mem")
  }

  def searchSynonymsWithRoot(root_word: String) = {
    val conditionQuery = synonyms.filter(_.root_word === root_word).result
    Await.result(db.run(conditionQuery), 2 seconds)
  }

  def searchSynonyms(root_word: String) = {
    val conditionQuery = synonyms.filter(_.root_word === root_word).map(_.synonyms).result
    Await.result(db.run(conditionQuery), 2 seconds)
  }

  override def receive: Receive = {
    case InitDBMessage =>
      val initDBResult = Await.result(initDB, 2 seconds)
      //tell send DB is OK
      sender ! initDBResult.size
    case EnableDBMessage => connectDB; println("enable DB")
    case DisableDBMessage => closeDB; println("disable DB")
    case GetSynonyms(root_word) => sender ! searchSynonymsWithRoot(root_word)
    case _ => println("error to receive msg " + _)
  }
}

case object InitDBMessage

case object EnableDBMessage

case object DisableDBMessage

case class GetSynonyms(root_word: String)


