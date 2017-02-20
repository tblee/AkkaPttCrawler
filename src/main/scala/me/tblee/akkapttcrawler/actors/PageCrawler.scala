package me.tblee.akkapttcrawler.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import me.tblee.akkapttcrawler.utils.Messages.{FinishedCrawlingPage, FinishedWriting, StartCrawlingPage, StartWriting}
import me.tblee.akkapttcrawler.utils.{Parser, PttArticle}

/**
  * Created by tblee on 2/17/17.
  * A crawling actor class that is managed by the supervisor actor. The crawler
  * actor takes in a specific board and page then instantiate parsers to parse all
  * the articles in that given page.
  */
class PageCrawler(supervisor: ActorRef) extends Actor with ActorLogging{

  val pttPrefix = "https://www.ptt.cc/bbs/"
  val parser = Parser

  override def receive: Receive = {
    case StartCrawlingPage(board, page, fileWriter) =>
      val link = s"${pttPrefix}${board}/index${page.toString}.html"
      val articles: List[PttArticle] = parser.parsePage(link)
      //println(articles)
      fileWriter ! StartWriting(articles, board, page)
      //sender() ! FinishedCrawlingPage(board, page)

    case FinishedWriting(board, page) =>
      supervisor ! FinishedCrawlingPage(board, page)

    case _ =>
  }
}
