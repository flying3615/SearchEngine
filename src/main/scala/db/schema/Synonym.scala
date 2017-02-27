package db

import slick.ast.ColumnOption.{AutoInc, PrimaryKey, Unique}
import slick.jdbc.H2Profile.api._
import slick.sql.SqlProfile.ColumnOption.Nullable

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

/**
  * Created by liuyufei on 27/02/17.
  */

object schema extends App {

  val synonyms = TableQuery[Synonym]


  // Definition of the Synonym table
  class Synonym(tag: Tag) extends Table[(Int, String, Option[Int])](tag, "Synonym") {
    def id = column[Int]("SYN_ID", AutoInc, PrimaryKey)

    // This is the primary key column
    def name = column[String]("SYN_NAME", Unique)

    def fk_id = column[Option[Int]]("SYN_FK_ID", Nullable)

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, fk_id)

    // A reified foreign key relation that can be navigated to create a join
    def root_word = foreignKey("SYN_FK", fk_id, synonyms)(_.id)
  }

  val db = Database.forConfig("h2mem")

  try {
    val setupTask = db.run( synonyms.schema.create).andThen {
      case Success(value) => println("init db end")
      case Failure(e) => println("init failed " + e.printStackTrace())
    }
    Await.result(setupTask, 1 seconds)
    //auto generate id when id=0
    val insertTask = db.run(synonyms ++= Seq((1, "hotel", Option.empty),
      (2, "motel", Some(1)),
      (3, "inn", Some(1)),
      (0, "test1", None),
      (0, "test2", Some(1))
    ))
    val queryTask = db.run(synonyms.result)
    val queryTask2 = db.run(synonyms.result)
    val lastTwoTasks = insertTask.flatMap(_=>queryTask).flatMap(_=>queryTask2)
    Await.result(lastTwoTasks, 1 seconds) foreach println
  } finally db.close

}



