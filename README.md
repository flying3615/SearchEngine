# SearchEngine - My first scala project
Small file search engine using [inverted index](https://en.wikipedia.org/wiki/Inverted_index), powered by [Slick](http://slick.lightbend.com/ "Funcitonal Relational Mapping") &amp;[Akka](http://akka.io/ "toolkit to build highly concurrent app")&amp;[Scala-Swing](https://github.com/scala/scala-swing)
Facing to a task that

>Build a basic search tool which allows the user to select a folder. Then for each query (list of
 terms), the search tool returns all the files under the input folder that contains all these terms.
 and Manipulating a synonym database from [init file](https://app.box.com/s/lvpm0aniqk09xucv41ojyde17nvhmcl6)

## Output of full inverted index map list as below

entry of inverted index map format like `word -> Map(doc_id -> (occurrences, index_list)`

* forecast -> Map(1 -> (1,ArrayBuffer(19)))
* sale -> Map(2 -> (6,ArrayBuffer(5, 24, 30, 41, 60, 66)), 4 -> (1,ArrayBuffer(15)), 1 -> (1,ArrayBuffer(9)), 3 -> (1,ArrayBuffer(18)))
* rise -> Map(2 -> (2,ArrayBuffer(11, 47)), 4 -> (1,ArrayBuffer(21)))
* top -> Map(1 -> (1,ArrayBuffer(15)))
* in -> Map(3 -> (2,ArrayBuffer(1, 10)), 2 -> (2,ArrayBuffer(16, 52)))
* juli -> Map(2 -> (2,ArrayBuffer(19, 55)), 4 -> (1,ArrayBuffer(1)), 3 -> (1,ArrayBuffer(27)))
* home -> Map(2 -> (2,ArrayBuffer(0, 36)), 4 -> (1,ArrayBuffer(10)), 1 -> (1,ArrayBuffer(4)), 3 -> (1,ArrayBuffer(13)))
* new -> Map(4 -> (1,ArrayBuffer(6)), 1 -> (1,ArrayBuffer(0)))
* increas -> Map(3 -> (1,ArrayBuffer(1)))
* ascend -> Map(1 -> (3,ArrayBuffer(29, 36, 43)))

## Core code to build full inverted index as below

<pre><code>
object InvertedIndexHelper extends App {

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
    line.trim.split("\\W+").foreach { word =>
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


  val prefix = "fileAbsolutePath"
  val input = List(prefix + "doc1.txt", prefix + "doc2.txt", prefix + "doc3.txt", prefix + "doc4.txt")
  println(buildupInvertedMap(input, Stemming.doStem _).mkString("\n"))

}
</code></pre>

## Unit test covered by 
[scalaTest](http://www.scalatest.org/) & [akka-testkit](http://doc.akka.io/docs/akka/current/scala/testing.html)

<pre><code>
class SearchActorSpec extends TestKit(ActorSystem(
  "SearchActorSpec",
  ConfigFactory.parseString(SearchActorSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll{

  val searchActor = system.actorOf(Props[SearchActor],name="searchActor")
  val searchFile = new File(getClass.getResource("/doc4.txt").getFile)

  val searchActorRef = TestActorRef(new SearchActor)

  override def afterAll(): Unit = shutdown()

  "SearchActor" should {
    "respond to the SearchMessage " in {
      within(500 millis) {
        searchActor ! SearchMessage(searchFile.getParentFile,"forecast")
        expectMsgPF(500 millis){
          case msg @ (a:List[String],b:String) =>
            a should have size 1
            b should not be empty
        }
      }
    }

    "throw exception" in {
      intercept[Exception](searchActorRef.receive("error"))
    }
  }
}


object SearchActorSpec {
  val config =
    """
      |akka {
      |   loglevel = "WARNING"
      |}
    """.stripMargin
}

</code></pre>



## Stemming the words by 
[The Porter Stemming Algorithm](https://tartarus.org/martin/PorterStemmer/)

### Reference to inverted index
[elastic - basic concepts URL](https://www.elastic.co/guide/en/elasticsearch/guide/current/inverted-index.html)
[A first take at building an inverted index](https://learning.wintec.ac.nz/mod/url/view.php?id=552404)
[Full inverted index](https://learning.wintec.ac.nz/mod/url/view.php?id=552405)