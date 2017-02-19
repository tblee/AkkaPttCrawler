package me.tblee.akkapttcrawler.actors

import akka.actor.{Actor, ActorLogging}
import me.tblee.akkapttcrawler.utils.Messages.{FinishedCrawlingPage, StartCrawlingPage}
import me.tblee.akkapttcrawler.utils.Parser

/**
  * Created by tblee on 2/17/17.
  * A crawling actor class that is managed by the supervisor actor. The crawler
  * actor takes in a specific board and page then instantiate parsers to parse all
  * the articles in that given page.
  */
class PageCrawler extends Actor with ActorLogging{

  val pttPrefix = "https://www.ptt.cc/bbs/"
  val parser = Parser

  override def receive: Receive = {
    case StartCrawlingPage(board, page) =>
      val link = s"${pttPrefix}${board}/index${page.toString}.html"
      val articles = parser.parsePage(link)
      println(articles)
      sender() ! FinishedCrawlingPage(board, page)
    case _ =>
  }
}
