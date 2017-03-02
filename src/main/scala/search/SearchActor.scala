package search

import java.io.File

import akka.actor.Actor
import akka.actor.Actor.Receive

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by liuyufei on 26/02/17.
  */
class SearchActor extends Actor{

  //todo change words to case class Synonym
  def search(selectedFile: File, words: String) =  {
    println(s"go for search ${selectedFile.getAbsolutePath} with search word ${words}")
    val start = System.currentTimeMillis()
    val paths = listFileNames(selectedFile)
    val end = System.currentTimeMillis()
    val statistic = s"search ${paths.size} matches took ${end-start} ms"
    (paths,statistic)
  }

  def listFileNames(file: File): ArrayBuffer[String] = {
    val paths = new ArrayBuffer[String]()
    file.listFiles().foreach(f => {
      if (f.isDirectory) {
        paths += f.getAbsolutePath
        //if the file is a directory, recurse it
        listFileNames(f)
      } else {
        //if the file is a file, add its path to a list
        paths += f.getAbsolutePath
      }
    })
    paths
  }

  override def receive: Receive = {
    case SearchMessage(selectFiled,synonyms) => sender ! search(selectFiled,synonyms)
    case _ => println("unhandle message")
  }
}

case class SearchMessage(selectFiled:File,synonyms:String)
