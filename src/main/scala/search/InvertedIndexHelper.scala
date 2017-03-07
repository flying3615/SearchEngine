package search

import scala.collection.mutable.{ArrayBuffer, ListMap, Map}
import scala.io.Source

/**
  * Created by liuyufei on 4/03/17.
  */
object InvertedIndexHelper extends App {

//  val synonyms = Map("rise"->Array("up","ascend"))
//  val use_synonym = true

  def buildupInvertedMap(files: Seq[String], f: ((String, Int, ArrayBuffer[Int])) => ((String, Int, ArrayBuffer[Int]))):Map[String, Map[String, (Int, ArrayBuffer[Int])]] = {
    // Map of (filePath -> one line of content)
    val fileToContentMap = files.map { filePath =>
      val sourceFile = Source.fromFile(filePath)
      val oneLine = sourceFile.getLines().mkString(" ") //reform to an array containing one line
      sourceFile.close()
      (filePath -> oneLine)
    }

    val unsortedResult = fileToContentMap.map {
      //convert to Iterable of (file -> (word,frequency, array of index))
      case (file, content) => (file -> lineToTuple(content).map(f(_)))
    }.foldLeft(Map.empty[String, Map[String, (Int, ArrayBuffer[Int])]]) {
      (map, file_content) => {
        file_content._2.foreach { word_freq_indexes =>
          //map add an entry which is (word -> entry of (filePath -> (frequency, Array of index)))
          val indexMapValue = (map.getOrElse(word_freq_indexes._1, Map()) += (file_content._1 -> (word_freq_indexes._2, word_freq_indexes._3)))
          map += (word_freq_indexes._1 -> indexMapValue)
        }
        map
      }
    }

    unsortedResult.map {case (word, listMap) =>
        //sort by word frequency
        val newListMap = ListMap(listMap.toSeq.sortBy(_._2._1): _*)
        (word -> newListMap)
      }

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
  val input = List(prefix + "doc1.txt", prefix + "doc2.txt", prefix + "doc3.txt", prefix + "doc4.txt")
  println(buildupInvertedMap(input, Stemming.doStem _).mkString("\n"))

}
