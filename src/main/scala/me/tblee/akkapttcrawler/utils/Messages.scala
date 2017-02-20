package me.tblee.akkapttcrawler.utils

import akka.actor.ActorRef

/**
  * Created by tblee on 2/17/17.
  */
object Messages {
  trait ActorMessage

  // Messages to and from Supervisor
  case class StartCrawling(from: Int, to: Int)
  case class FinishedCrawling(board: String)

  // Messages to and from PageCrawler
  case class StartCrawlingPage(board: String, page: Int, fileWriter: ActorRef)
  case class FinishedCrawlingPage(board: String, page: Int)

  // Messages to and from FileWriter
  case class StartWriting(articles: List[PttArticle], board: String, page: Int)
  case class FinishedWriting(board: String, page: Int)

}