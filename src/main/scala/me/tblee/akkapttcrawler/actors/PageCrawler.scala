package me.tblee.akkapttcrawler.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import me.tblee.akkapttcrawler.utils.Messages._
import me.tblee.akkapttcrawler.utils.{Parser, PttArticle}

import scala.util.{Failure, Success, Try}

/**
  * Created by tblee on 2/17/17.
  * A crawling actor class that is managed by the supervisor actor. The crawler
  * actor takes in a specific board and page then instantiate parsers to parse all
  * the articles in that given page.
  */
class PageCrawler(supervisor: ActorRef) extends Actor with ActorLogging{

  val pttPrefix = "https://www.ptt.cc/bbs/"
  val parser = Parser
  var blackList: Set[String] = Set[String]()

  override def receive: Receive = {
    case StartCrawlingPage(board, page, fileWriter) =>
      val link = s"${pttPrefix}${board}/index${page.toString}.html"
      val maybeArticles: Try[List[PttArticle]] = parser.parsePage(link, blackList)
      maybeArticles match {
        case Success(articles) =>
          fileWriter ! StartWriting(articles, board, page)
        case Failure(err) =>
          log.error(s"Got ${err.toString} when crawling page --${page} of board --${board}")
          supervisor ! FailedCrawlingPage(board, page, err)
      }

    case FinishedWriting(board, page) =>
      supervisor ! FinishedCrawlingPage(board, page)

    case UpdateBlackList(badUrl) =>
      blackList += badUrl

    case _ =>
  }
}
