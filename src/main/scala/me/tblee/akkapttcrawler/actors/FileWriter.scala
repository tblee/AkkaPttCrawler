package me.tblee.akkapttcrawler.actors

import java.io.{File, PrintWriter}

import akka.actor.{Actor, ActorLogging}
import me.tblee.akkapttcrawler.utils.Messages.{FinishedWriting, StartWriting}

/**
  * Created by tblee on 2/20/17.
  * File writer that writes the crawled page information to file.
  */
class FileWriter extends Actor with ActorLogging {

  // Initialize a file writer
  val writer = new PrintWriter(new File("test.txt"))

  def receive: Receive = {
    case StartWriting(articles, board, page) =>
      writer.write(articles.toString)
      sender() ! FinishedWriting(board, page)

    case _ =>
  }

  override def postStop() = {
    super.postStop
    writer.flush
    writer.close
  }
}
