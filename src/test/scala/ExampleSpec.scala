import org.scalatest._
import search.{InvertedIndexHelper, Stemming}

import scala.collection.mutable


/**
  * Created by liuyufei on 28/02/17.
  */
class ExampleSpec extends FlatSpec with Matchers {

//  "A Stack" should "pop values in last-in first-out order" in {
//    val stack = new mutable.Stack[Int]
//    stack.push(1)
//    stack.push(2)
//    stack.pop should be (2)
//    stack.pop should be (1)
//  }
//
//  it should "throw NoSuchElementException if an empty stack is popped " in {
//    val emptyStack = new mutable.Stack[Int]
//    a [NoSuchElementException] should be thrownBy{
//      emptyStack.pop
//    }
//  }

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


}
