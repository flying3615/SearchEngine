package db

import slick.ast.ColumnOption.{AutoInc, PrimaryKey, Unique}
import slick.jdbc.H2Profile.api._
import slick.sql.SqlProfile.ColumnOption.Nullable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
  * Created by liuyufei on 27/02/17.
  */

object schema extends App {

  val synonyms = TableQuery[Synonym]

  // Definition of the Synonym table
  class Synonym(tag: Tag) extends Table[(Int, String, Option[Int])](tag, "Synonym") {
    def id = column[Int]("SYN_ID", AutoInc, PrimaryKey)

    // This is the primary key column
    def name = column[String]("SYN_NAME",Unique)

    def fk_id = column[Option[Int]]("SYN_FK_ID",Nullable)

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

      //FK can be null?
      synonyms += (1, "hotel", Option.empty),
      synonyms += (2, "motel", Option(1)),
      synonyms += (3, "inn", Option(1))
    )

    db.run(setup).onComplete{
      case Success(value)=>println("init db end")
      case Failure(e) => println("init failed "+e.printStackTrace())
    }

    Thread.sleep(1000)

    db.run(synonyms.result).onComplete{
      case Success(value) => println(value)
      case Failure(e) => println(e.getMessage)
    }

    Thread.sleep(1000)
  } finally db.close

}



