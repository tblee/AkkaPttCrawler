package me.tblee.akkapttcrawler.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import me.tblee.akkapttcrawler.utils.Messages.{FinishedCrawlingPage, StartCrawling, StartCrawlingPage}

/**
  * Created by tblee on 2/18/17.
  */
class Supervisor(system: ActorSystem, board: String) extends Actor with ActorLogging{

  val numCrawlersBasic = 3

  var pagesToCrawl: Set[Int] = Set()
  var pagesCrawling: Set[Int] = Set()
  var pageCrawlers = (1 to numCrawlersBasic) map {id => system.actorOf(Props(new PageCrawler))}

  def assignPageToCrawl(page: Int, crawler: ActorRef) = {
    pagesToCrawl -= page
    pagesCrawling += page
    crawler ! StartCrawlingPage(board, page)
  }

  def receive: Receive = {
    // Setup crawling environment and start crawling
    case StartCrawling(from, to) =>
      pagesToCrawl = (from to to).toSet
      // In this first implementation we assume #pages >= #crawlers
      val firstBatch = pagesToCrawl.take(numCrawlersBasic) zip pageCrawlers
      firstBatch foreach { case (page, crawler) => assignPageToCrawl(page, crawler) }

    // When a PageCrawler reports a crawling task is finished
    case FinishedCrawlingPage(_, page) =>
      if (!pagesToCrawl.isEmpty) assignPageToCrawl(pagesToCrawl.head, sender())

    case _ =>
  }
}
