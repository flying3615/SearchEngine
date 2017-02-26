package ui

import java.awt.Color
import java.io.File
import javax.swing.border.Border

import scala.collection.mutable.ArrayBuffer
import scala.swing.{BoxPanel, Button, FileChooser, FlowPanel, Label, MainFrame, Orientation, ScrollPane, Swing, TextArea, TextField}

/**
  * Created by liuyufei on 18/02/17.
  *
  Write a GUI application which enables the user to select a folder. The application outputs the list of
  all files under this folder (and sub folders).
  */
class UI extends MainFrame{

  title = "File selector"

  object searchWords extends TextField {columns = 30}

  val fileChooser:FileChooser = new FileChooser()

  fileChooser.fileSelectionMode = FileChooser.SelectionMode.FilesAndDirectories

  fileChooser.descriptionFor(new File(System.getProperty("user.home")))

  val textArea = new TextArea(10,50){
    editable = false
  }


  val buttonPanel = new FlowPanel{
    contents += Button("Close"){
      System.exit(0)
    }
    Swing.HStrut(20)
    contents += Button("Select") {
      selectFile()
    }
  }

  val searchPanel = new BoxPanel(Orientation.Horizontal){
    contents += new Label("Search Words")
    contents += Swing.HStrut(5)
    contents += searchWords
    contents += Button("GO") {
      goSearch()
    }
    def goSearch(): Unit = {
      println("go for search")
    }
  }


  val statusPanel = new BoxPanel(Orientation.Horizontal){
    val statusBar = new Label("status")
    statusBar.yLayoutAlignment =0.0
    contents +=statusBar
  }

//  statusPanel.border = javax.swing.BorderFactory.createEtchedBorder()

  //build main panel
  val mainPanel = new BoxPanel(Orientation.Vertical) {
    contents += searchPanel
    contents += Swing.VStrut(10)
    contents += Swing.Glue
    contents += new Label("File Path")
    contents += Swing.VStrut(5)
    contents += new ScrollPane(textArea)
    contents += Swing.VStrut(5)
    contents += buttonPanel
    contents += statusPanel

    for (e <- contents)
    e.xLayoutAlignment = 0.0
    border = Swing.EmptyBorder(30, 10, 10, 10)

  }

  contents = mainPanel

  def selectFile(): Unit = {
    fileChooser.showOpenDialog(mainPanel) match {
      case FileChooser.Result.Approve =>
        path.clear()
        listFileNames(fileChooser.selectedFile)
        textArea.text = path.mkString("\n")
        repaint()
      case FileChooser.Result.Cancel => println("user close filechooser dialog")
      case _ => println("other action, maybe a error")
    }
  }

  val path:ArrayBuffer[String] = new ArrayBuffer()

  final def listFileNames(file:File):Unit = {
    file.listFiles().foreach(f=>{
      if(f.isDirectory){
        path+=f.getAbsolutePath
        //if the file is a directory, recurse it
        listFileNames(f)
      }else{
        //if the file is a file, add its path to a list
        path+=f.getAbsolutePath
      }
    })
  }

}

object RunIt extends App{
  val selector = new UI
  selector.visible = true
}
