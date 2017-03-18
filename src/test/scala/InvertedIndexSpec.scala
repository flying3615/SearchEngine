import org.scalatest._
import search.{InvertedIndexHelper, Stemming}


/**
  * Created by liuyufei on 28/02/17.
  */
class InvertedIndexSpec extends FlatSpec with Matchers {

  it should "successfully build a not empty full inverted index map" in {
    val input = List("/doc1.txt","/doc2.txt","/doc3.txt","/doc4.txt").map(getClass.getResource(_).getPath)
    val fullInvertedIndex = InvertedIndexHelper.buildupInvertedMap(input, Stemming.doStem)

    fullInvertedIndex should not be empty
    println(fullInvertedIndex.mkString("\n"))
  }

  it should "has 5 words key in the map" in {
    val input = List(getClass.getResource("/doc4.txt").getPath)
    val fullInvertedIndex = InvertedIndexHelper.buildupInvertedMap(input, Stemming.doStem)
    fullInvertedIndex should have size 5
  }

  it should "has no result in the map if file is empty" in {
    val input = List(getClass.getResource("/emptyFile.txt").getPath)
    val fullInvertedIndex = InvertedIndexHelper.buildupInvertedMap(input, Stemming.doStem)
    fullInvertedIndex should have size 0
  }

  "filepath 2 id map" should "have been built after building up inverted map" in {
    val input = List("/doc1.txt","/doc2.txt","/doc3.txt","/doc4.txt").map(getClass.getResource(_).getPath)
    InvertedIndexHelper.buildupInvertedMap(input, Stemming.doStem)
    val ID2FilePath = InvertedIndexHelper.ID2FilePath
    val filePath2ID = InvertedIndexHelper.filePath2ID

    val file1 = getClass.getResource("/doc1.txt").getPath
    filePath2ID should contain (file1 -> "1")
    ID2FilePath should equal (filePath2ID.map(_.swap))
  }


}
