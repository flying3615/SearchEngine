package search

import java.io.File

import akka.actor.Actor

import scala.collection.mutable.{ArrayBuffer, Map}
/**
  * Created by liuyufei on 26/02/17.
  */
class SearchActor extends Actor{

  //alias for invertedIndexMap
  type invertedIndexMap = Map[String, Map[String, (Int, ArrayBuffer[Int])]]
  val invertedIndexHelperCache = Map[String,invertedIndexMap]()

  def search(selectedFile: File, words: String) =  {
    println(s"go for search ${selectedFile.getAbsolutePath} with search word ${words}")
    val start = System.currentTimeMillis()
    val paths = listFileNames(selectedFile,new ArrayBuffer[String]())

    val invertedMap:invertedIndexMap = invertedIndexHelperCache
                                           .getOrElseUpdate(selectedFile.getAbsolutePath,InvertedIndexHelper.buildupInvertedMap(paths,Stemming.doStem))

    println(s"inverted index map = ${invertedMap.mkString("\n")}")
    //get path List(ArrayBuffer) and flat it
    val pathMap = words.split(",").flatMap{word=>
//        Option(Map(String->(freq,indexes)))
        invertedMap.get(word).map(optionMap=>optionMap.map(_._1))
    }.flatten

    val end = System.currentTimeMillis()
    val statistic = s"search ${pathMap.size} matches took ${end-start} ms"

    (pathMap.toList, statistic)
  }


  def listFileNames(file: File,paths:ArrayBuffer[String], fileType:String ="txt"): ArrayBuffer[String] = {
    file.listFiles().foreach(f => {
      if (f.isDirectory) {
        listFileNames(f,paths)
      } else {
        //only support txt file
        if(f.getName.contains(fileType)) paths += f.getAbsolutePath
      }
    })
    paths
  }

  override def receive: Receive = {
    case SearchMessage(selectFiled,synonyms) => sender ! search(selectFiled,synonyms)
    case _ => throw new Exception("unhandled message")
  }
}

case class SearchMessage(selectFiled:File,synonyms:String)
