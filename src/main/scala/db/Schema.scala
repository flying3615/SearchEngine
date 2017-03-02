package db

import slick.ast.ColumnOption.PrimaryKey
import slick.jdbc.H2Profile.api._

/**
  * Created by liuyufei on 2/03/17.
  */
case class Synonym(root_word: String, synonyms: String)

// Definition of the Synonym table, table of Synonym
class Synonyms(tag: Tag) extends Table[Synonym](tag, "Synonym") {
  def root_word = column[String]("root_word", PrimaryKey)

  // This is the primary key column
  def synonyms = column[String]("synonyms")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (root_word, synonyms) <> (Synonym.tupled, Synonym.unapply)
}