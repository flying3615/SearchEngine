package db

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
  * Created by liuyufei on 27/02/17.
  */

object schema extends App {

  val synonyms = TableQuery[Synonym]

  // Definition of the Synonym table
  class Synonym(tag: Tag) extends Table[(Int, String, Int)](tag, "Synonym") {
    def id = column[Int]("SYN_ID", O.AutoInc, O.PrimaryKey)

    // This is the primary key column
    def name = column[String]("SYN_NAME")

    def fk_id = column[Int]("SYN_FK_ID")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, fk_id)

    // A reified foreign key relation that can be navigated to create a join
    def root_word = foreignKey("SYN_FK", fk_id, synonyms)(_.id)
  }

  val db = Database.forConfig("h2mem")

  try {

    val setup = DBIO.seq(
      // Create the tables, including primary and foreign keys
      synonyms.schema.create,

      // Insert some suppliers
      synonyms += (1, "hotel", 0),
      synonyms += (2, "motel", 1),
      synonyms += (3, "inn", 1)
    )

    db.run(setup).onComplete{
      case Success(value)=>println("init db end")
      case Failure(e) => println("init failed "+e.printStackTrace())
    }

    db.run(synonyms.result).onComplete{
      case Success(value) => println(value)
      case Failure(e) => println(e.getMessage)
    }

    Thread.sleep(1000)
  } finally db.close

}



