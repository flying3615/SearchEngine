package search

import scala.collection.mutable.{ArrayBuffer, ListMap, Map}
import scala.io.Source

/**
  * Created by liuyufei on 4/03/17.
  */
object InvertedIndexHelper {

  var filePath2ID = Map[String,String]()
  var ID2FilePath = Map[String,String]()

  type word_occ_indexes = (String, Int, ArrayBuffer[Int])
  type invertedIndexMap = Map[String, Map[String, (Int, ArrayBuffer[Int])]]

  def buildupInvertedMap(files: Seq[String], f: (word_occ_indexes) => (word_occ_indexes)):invertedIndexMap = {
    // Map of (filePath -> one line of content)
    var fileID = 0
    val fileToContentMap = files.map { filePath =>
      val sourceFile = Source.fromFile(filePath)
      //reformat file content to one line, which makes easier to count word indexes
      val oneLine = sourceFile.getLines().mkString(" ")
      sourceFile.close()
      //generate filePath -> id
      fileID+=1
      filePath2ID += (filePath -> fileID.toString)
      (fileID.toString -> oneLine)
    }
    //invert the map
    ID2FilePath = filePath2ID.map(_.swap)

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
    line.trim.split("\\W+").filter(_!="").foreach { word =>
      ss(word) = multiIndexOf(line, word, ss.getOrElse(word, (word, 0, ArrayBuffer[Int]())))
    }
    ss.values
  }

  private def multiIndexOf(line: String, word: String, acc: (String, Int, ArrayBuffer[Int])) = {
    if (acc._1 == word) {
      val nextIndex = if (acc._3.isEmpty) line.indexOf(word) else line.indexOf(word, acc._3.last + word.size)
      val freq = acc._2 + 1
      acc._3.append(nextIndex)
      (word, freq, acc._3)
    } else {
      acc
    }
  }


}
