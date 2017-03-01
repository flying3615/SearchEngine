package db

import slick.ast.ColumnOption.PrimaryKey
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source

/**
  * Created by liuyufei on 27/02/17.
  */

object DB extends App {

  val synonyms = TableQuery[Synonyms]

  case class Synonym(root_word:String,synonyms:String)

  // Definition of the Synonym table, table of Synonym
  class Synonyms(tag: Tag) extends Table[Synonym](tag, "Synonym") {
    def root_word = column[String]("root_word", PrimaryKey)
    // This is the primary key column
    def synonyms = column[String]("synonyms")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (root_word, synonyms) <> (Synonym.tupled,Synonym.unapply)
  }

  val db = Database.forConfig("h2mem")

  val inputData = Source.fromResource("synonym_words.txt").getLines().map(s=>{
    val item = s.split(":")
    Synonym(item(0),item(1))
  }).toList

  println("input size "+inputData.size)

  val allTasks = synonyms.schema.create.flatMap(_=>synonyms ++= inputData).flatMap(_=>synonyms.result)
  val trueResult = db.run(allTasks)

  println(Await.result(trueResult, Duration.Inf).take(1))

  val conditionQuery = synonyms.filter(_.root_word === "abandonment").map(_.synonyms).result
  println(conditionQuery.statements.head)

  closeDB

  def closeDB = {
    if(db!=null) db.close()
  }

}



