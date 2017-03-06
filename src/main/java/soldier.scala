import scala.annotation.tailrec

/**
  * Created by liuyufei on 5/03/17.
  */

case class soldier(var num:Int, var next:soldier)
class SoldierList {

  var header:soldier = _

  def initList(rootNum:Int) = {
    header = soldier(rootNum,header)
    header.next = header
  }

  def deleteList(num:Int) = {
    var tmp = header
    while(tmp.next!=header){
      if(tmp.next.num==num){
        tmp.next = tmp.next.next
      }else{
        tmp = tmp.next
      }
    }
    this
  }

  def insertList(num:Int) = {
    if(header.next==header){
      header.next = soldier(num,header)
    }else{
      var tmp_soldier = header
      while(tmp_soldier.next!=header){
        tmp_soldier = tmp_soldier.next
      }
      tmp_soldier.next = soldier(num,header)
    }
    this
  }

  def size = {
    var tmp = header
    var size:Int = 1
    while(tmp.next!=header){
      size+=1
      tmp = tmp.next
    }
    size
  }

  def getSoldier(num:Int):Option[soldier] ={
    var tmp = header
    while(tmp.next!=header){
      if(tmp.num==num) Option(tmp)
      else{
        tmp = tmp.next
      }
    }
    return None
  }

  def printList() = {
    var tmp_soldier = header
    println(tmp_soldier.num)
    while (tmp_soldier.next!=header){
      tmp_soldier = tmp_soldier.next
      print(s"${tmp_soldier.num},")
    }
  }
}


object test extends App{

  val soldierList = new SoldierList
  soldierList.initList(1)

  for(i<-2 to 41){
    soldierList.insertList(i)
  }


  def kill(soldierList: SoldierList):SoldierList = {

    val toBeRemoved = soldierList.header.next

    @tailrec
    def innerKill(soldierList: SoldierList,toBeRemoved:soldier):SoldierList ={
      if(soldierList.size==1) return soldierList
      else{
        println("kill "+toBeRemoved.num)
        if(toBeRemoved.num==soldierList.header.num) soldierList.header = soldierList.header.next
        return innerKill(soldierList.deleteList(toBeRemoved.num),toBeRemoved.next.next)
      }
    }
    innerKill(soldierList,toBeRemoved)
  }


  kill(soldierList)
  soldierList.printList()

}
