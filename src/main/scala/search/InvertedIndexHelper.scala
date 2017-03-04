package search

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.io.Source

/**
  * Created by liuyufei on 4/03/17.
  */
object InvertedIndexHelper extends App {

  private val invertedMap = scala.collection.mutable.Map.empty[String, ListBuffer[Map[String, (Int, ArrayBuffer[Int])]]]

  def buildupInvertedMap(files: Seq[String], f: ((String, Int, ArrayBuffer[Int])) => ((String, Int, ArrayBuffer[Int]))) = {
    files.foreach { filePath =>
      Source.fromFile(filePath)
        .getLines() //get all lines from the file
        .flatMap({ line =>
        val ss = scala.collection.mutable.Map.empty[String, (String, Int, ArrayBuffer[Int])]
        line.trim.split("\\W+").foreach { word =>
          ss(word) = multiIndexOf(line, word, ss.getOrElse(word, (word, 0, ArrayBuffer[Int]())))
        }
        ss.values
      })
        .map(f(_)) //convert line to Array(word,index)
        .foldLeft(invertedMap) {
        (map, wordWithIndex) => {
          map += (wordWithIndex._1 -> (map.getOrElse(wordWithIndex._1, ListBuffer()) += Map(filePath -> (wordWithIndex._2, wordWithIndex._3))))
        }
      }
    }
  }


  def multiIndexOf(line: String, word: String, acc: (String, Int, ArrayBuffer[Int])) = {
    if (acc._1 == word) {
      val nextIndex = if (acc._3.isEmpty) line.indexOf(word) else line.indexOf(word, acc._3.last + word.size)
      val fequ = acc._2 + 1
      acc._3.append(nextIndex)
      (word, fequ, acc._3)
    } else {
      acc
    }
  }


  val prefix = "/Users/liuyufei/Documents/Learn/scala/SearchEngine/src/main/resources/"

  val input = List(prefix + "doc1", prefix + "doc2", prefix + "doc3", prefix + "doc4")

  buildupInvertedMap(input, Stemming.doStem _)

  println(invertedMap.mkString("\n"))

}
