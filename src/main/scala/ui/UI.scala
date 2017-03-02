package ui

import java.io.File

import akka.actor.ActorRef
import akka.pattern.Patterns
import db.{DisableDBMessage, EnableDBMessage, GetSynonyms}
import search.SearchActor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.swing.event.{ButtonClicked, WindowClosing}
import scala.swing.{BoxPanel, Button, Dialog, FileChooser, FlowPanel, Label, MainFrame, Orientation, ScrollPane, Swing, TextArea, TextField, ToggleButton}
import scala.util.{Failure, Success}

/**
  * Created by liuyufei on 18/02/17.
  *
  * Write a GUI application which enables the user to select a folder. The application outputs the list of
  * all files under this folder (and sub folders).
  */
class UI(dbActor: ActorRef) extends MainFrame {

  title = "File selector"

  //search input text
  object searchWords extends TextField(columns = 30)

  val fileChooser: FileChooser = new FileChooser()

  fileChooser.fileSelectionMode = FileChooser.SelectionMode.FilesAndDirectories

  fileChooser.descriptionFor(new File(System.getProperty("user.home")))

  val textArea = new TextArea(10, 50) {
    editable = false
  }

  val enableDBButton = new ToggleButton("DB Off")

  listenTo(enableDBButton)

  //handle enable/disable DB event
  reactions += {
    case ButtonClicked(src) => {
      DBEnable = src.selected
      if (src.selected) {
        DBEnable = true
        enableDBButton.text = "DB On"
        dbActor ! EnableDBMessage
      } else {
        DBEnable = false
        enableDBButton.text = "DB Off"
        dbActor ! DisableDBMessage
      }
    }

    case WindowClosing(src) =>
      dbActor ! DisableDBMessage
  }

  //build select/close button panel
  val buttonPanel = new FlowPanel {
    contents += enableDBButton
    contents += Button("Close") {
      dbActor ! DisableDBMessage
      System.exit(0)
    }
    Swing.HStrut(20)
    contents += Button("Select") {
      selectFile()
    }
  }

  var selectedFile: File = _
  var DBEnable: Boolean = false

  //build select file panel
  val searchPanel = new BoxPanel(Orientation.Horizontal) {
    contents += new Label("Search Words")
    contents += Swing.HStrut(5)
    contents += searchWords
    contents += Button("GO") {
      goSearch()
    }
  }


  //build status panel
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

    contents foreach {
      _.xLayoutAlignment = 0.0
    }
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
      case FileChooser.Result.Cancel => println("user close file chooser dialog")
      case _ => println("other action, maybe a error")
    }
  }


  def goSearch(): Unit = {
    if (searchWords.text.isEmpty) {
      //open a dialog
      Dialog.showMessage(contents.head, "Search Word Cannot be Empty", title = "ERROR")
      return
    }

    if (selectedFile == null) {
      //open a dialog
      Dialog.showMessage(contents.head, "Please select a search directory", title = "ERROR")
      return
    }

    if (selectedFile.isDirectory) {
      //do future
      val searchResultFuture = if (DBEnable) {
        //do search synonyms in db
        val synonymFuture = Patterns.ask(dbActor, GetSynonyms(searchWords.text), 5 seconds)
        synonymFuture.flatMap { synonyms =>
          //todo import synonym case
          SearchActor.search(selectedFile, synonyms.toString)
        }
      } else {
        // do search user input
        //todo import synonym case
        SearchActor.search(selectedFile, searchWords.text)
      }

      FutureHelper.withSuccess(searchResultFuture) { result =>
        textArea.text = result._1.mkString("\n")
        statusLabel.text = result._2
      }
    } else {
      // re-select
      statusLabel.text = selectedFile.getAbsolutePath + " is not a directory,please re-select"
    }
  }
}


object FutureHelper {
  def withSuccess[T](future: Future[T])(f: T => Unit) = {
    future.onComplete {
      case Success(value) => f(value)
      case Failure(e) => e.printStackTrace()
    }
  }
}
