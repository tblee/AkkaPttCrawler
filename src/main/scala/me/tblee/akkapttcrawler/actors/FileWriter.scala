package me.tblee.akkapttcrawler.actors

import java.io.{File, PrintWriter}
import io.circe.generic.auto._
import io.circe.syntax._

import akka.actor.{Actor, ActorLogging}
import me.tblee.akkapttcrawler.utils.Messages.{FinishedWriting, StartWriting}

/**
  * Created by tblee on 2/20/17.
  * File writer that writes the crawled page information to file.
  */
class FileWriter(file: File) extends Actor with ActorLogging {

  // Initialize a file writer
  val writer = new PrintWriter(file)
  writer.write("[")
  var firstWrite = true

  def receive: Receive = {
    case StartWriting(articles, board, page) =>
      // Results are written as JSON array. Comma is not needed for the every first item written
      // to file.
      if (firstWrite) {
        articles match {
          case head::tail =>
            writer.write(head.asJson.noSpaces + "\n")
            tail foreach { article => writer.write("," + article.asJson.noSpaces + "\n") }
          case _ =>
        }
        firstWrite = false
      } else {
        articles foreach { article => writer.write("," + article.asJson.noSpaces + "\n") }
      }
      sender() ! FinishedWriting(board, page)

    case _ =>
  }

  override def postStop() = {
    super.postStop
    writer.write("]")
    writer.flush
    writer.close
  }
}
