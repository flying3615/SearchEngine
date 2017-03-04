package db

import akka.actor.Actor
import slick.jdbc.MySQLProfile.api._

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
    db = Database.forConfig("mysqldb")
  }

  def searchSynonymsWithRoot(root_word: String) = {
    val conditionQuery = synonyms.filter(_.root_word === root_word).result
    Await.result(db.run(conditionQuery), 2 seconds)
  }

  def searchSynonyms(root_word: String) = {
    val conditionQuery = synonyms.filter(_.root_word === root_word).map(_.synonyms).result
    Await.result(db.run(conditionQuery), 2 seconds)
  }


  def updateSynonyms(synonym: Synonym) = db.run(synonyms update synonym)


  def deleteSynonym(synonym: Synonym) = db.run(synonyms.filter(_.root_word === synonym.root_word).delete)


  override def receive: Receive = {
    case InitDBMessage =>
      //infinite time...
      val initDBResult = Await.result(initDB, 10 seconds)
      //tell sender DB is OK
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


