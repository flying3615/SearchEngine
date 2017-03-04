package search

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by liuyufei on 4/03/17.
  */
object InvertedIndexHelper extends App{

  private val invertedMap = scala.collection.mutable.Map.empty[String,ListBuffer[String]]

  def buildupInvertedMap(files:Seq[String])(implicit f:String=>String) = {
    files.foreach{ filePath =>
      Source.fromFile(filePath)
        .getLines() //get all lines from the file
        .flatMap(_.trim.split("\\W+")) //convert line to word
        .map(f(_)) // do stem
        .foldLeft(invertedMap){
          (map,word) => {
            map += (word -> (map.getOrElse(word,ListBuffer()) += filePath))
          }
        }
    }
  }

  val prefix = "/Users/liuyufei/Documents/Learn/scala/SearchEngine/src/main/resources/"

  val input = List(prefix+"doc1",prefix+"doc2",prefix+"doc3",prefix+"doc4")

  import search.Stemming._

  buildupInvertedMap(input)

  println(invertedMap)

}
