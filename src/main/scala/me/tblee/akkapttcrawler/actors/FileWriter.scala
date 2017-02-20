package me.tblee.akkapttcrawler.actors

import akka.actor.{Actor, ActorLogging}
import me.tblee.akkapttcrawler.utils.Messages.StartWriting

/**
  * Created by tblee on 2/20/17.
  */
class FileWriter extends Actor with ActorLogging {

  // Initialize a file writer


  def receive: Receive = {
    case StartWriting(articles, board, page) =>
      println(articles)
    case _ =>
  }
}
