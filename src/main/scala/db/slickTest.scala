package db

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by liuyufei on 26/02/17.
  */
object slickTest extends App{

  val db = Database.forURL("jdbc:h2:mem:test1",
    Map("driver" -> "org.h2.Driver",
        "connectionPool" -> "disabled",
        "keepAliveConnection" -> "true"))
  try {
    println(db.toString)
  } finally db.close
}
