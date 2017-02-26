package ui

import java.io.File

import search.SearchActor
import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.{BoxPanel, Button, Dialog, FileChooser, FlowPanel, Label, MainFrame, Orientation, ScrollPane, Swing, TextArea, TextField}
import scala.util.{Failure, Success}

/**
  * Created by liuyufei on 18/02/17.
  *
  * Write a GUI application which enables the user to select a folder. The application outputs the list of
  * all files under this folder (and sub folders).
  */
object UI extends MainFrame {

  title = "File selector"

  //search input text
  object searchWords extends TextField(columns = 30)

  val fileChooser: FileChooser = new FileChooser()

  fileChooser.fileSelectionMode = FileChooser.SelectionMode.FilesAndDirectories

  fileChooser.descriptionFor(new File(System.getProperty("user.home")))

  val textArea = new TextArea(10, 50) {
    editable = false
  }

  val buttonPanel = new FlowPanel {
    contents += Button("Close") {
      System.exit(0)
    }
    Swing.HStrut(20)
    contents += Button("Select") {
      selectFile()
    }
  }

  var selectedFile: File = _

  val searchPanel = new BoxPanel(Orientation.Horizontal) {
    contents += new Label("Search Words")
    contents += Swing.HStrut(5)
    contents += searchWords
    contents += Button("GO") {
      goSearch()
    }

    def goSearch(): Unit = {
      if (searchWords.text.isEmpty) {
        //open a dialog
        Dialog.showMessage(contents.head, "Search Word Cannot be Empty", title = "ERROR")
        return
      }

      if (selectedFile.isDirectory) {
        //do future
        SearchActor.search(selectedFile, searchWords.text).onComplete {
          case Success(result) =>
            textArea.text = result._1.mkString("\n")
            statusLabel.text = result._2
          case Failure(ex) => Dialog.showMessage(contents.head, ex.getMessage, title = "ERROR")
        }
      } else {
        // re-select
        statusLabel.text = selectedFile.getAbsolutePath + " is not a directory,please re-select"
      }
    }
  }


  val statusLabel = new Label
  val statusPanel = new BoxPanel(Orientation.Horizontal) {
    statusLabel.yLayoutAlignment = 0.0
    contents += statusLabel
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

    contents foreach {_.xLayoutAlignment = 0.0}
    border = Swing.EmptyBorder(30, 10, 10, 10)

  }

  contents = mainPanel

  def selectFile(): Unit = {
    fileChooser.showOpenDialog(mainPanel) match {
      case FileChooser.Result.Approve =>
        selectedFile = fileChooser.selectedFile
        statusLabel.text = selectedFile.getAbsolutePath
      //        path.clear()
      //        listFileNames(fileChooser.selectedFile)
      //        textArea.text = path.mkString("\n")
      //        repaint()
      case FileChooser.Result.Cancel => println("user close filechooser dialog")
      case _ => println("other action, maybe a error")
    }
  }



}

object RunIt extends App {
  UI.visible = true
}
