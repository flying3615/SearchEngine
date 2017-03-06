package search

import scala.collection.mutable.{ArrayBuffer, ListBuffer, Map}
import scala.io.Source

/**
  * Created by liuyufei on 4/03/17.
  */
object InvertedIndexHelper extends App {

  def buildupInvertedMap(files: Seq[String], f: ((String, Int, ArrayBuffer[Int])) => ((String, Int, ArrayBuffer[Int]))) = {
    // Map of (filePath -> one line of content)
    val fileToContentMap = files.map { filePath =>
      val sourceFile = Source.fromFile(filePath)
      val oneLine = sourceFile.getLines().mkString(" ") //reform to an array containing one line
      sourceFile.close()
      (filePath -> oneLine)
    }

    val invertedIndex = fileToContentMap.map{
      //convert to Iterable of (file -> (word,frequency, array of index))
      case (file,content) => (file->lineToTuple(content).map(f(_)))
    }.foldLeft(Map.empty[String, ListBuffer[Map[String, (Int, ArrayBuffer[Int])]]]){
      (map,listOfWord) => {
        listOfWord._2.foreach{ tuple3 =>
          //map add an entry which is (word -> List of (filePath -> (frequency, Array of index)
          map += (tuple3._1 -> (map.getOrElse(tuple3._1,ListBuffer()) += Map(listOfWord._1->(tuple3._2,tuple3._3))))
        }
        //return modified map
        map
      }
    }

    println(invertedIndex.mkString("\n"))

  }


  private def lineToTuple(line: String): Iterable[(String, Int, ArrayBuffer[Int])] = {
    val ss = Map.empty[String, (String, Int, ArrayBuffer[Int])]
    line.trim.split("\\W+").foreach { word =>
      ss(word) = multiIndexOf(line, word, ss.getOrElse(word, (word, 0, ArrayBuffer[Int]())))
    }
    ss.values
  }

  private def multiIndexOf(line: String, word: String, acc: (String, Int, ArrayBuffer[Int])) = {
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

}
