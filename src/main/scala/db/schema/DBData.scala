package db.schema

import scala.io.Source

/**
  * Created by liuyufei on 1/03/17.
  */
object DBData extends App{

  val inputData = Source.fromResource("synonym_words.txt").getLines()
  inputData foreach println
}
