package me.tblee.akkapttcrawler.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import me.tblee.akkapttcrawler.utils.Messages.{FinishedCrawlingPage, StartCrawling, StartCrawlingPage}

/**
  * Created by tblee on 2/18/17.
  */
class Supervisor(system: ActorSystem, board: String) extends Actor with ActorLogging{

  val numCrawlersBasic = 3

  var pagesToCrawl: Set[Int] = Set()
  var pagesCrawling: Set[Int] = Set()

  // Fire up PageCrawler actors
  var pageCrawlers = (1 to numCrawlersBasic) map {id => system.actorOf(Props(new PageCrawler(self)))}

  // Fire up FileWriter actor
  val fileWriter = system.actorOf(Props(new FileWriter))

  def assignPageToCrawl(page: Int, crawler: ActorRef) = {
    pagesToCrawl -= page
    pagesCrawling += page
    crawler ! StartCrawlingPage(board, page, fileWriter)
  }

  def registerFinishedPage(page: Int) = {
    pagesCrawling -= page
    log.info(s"Finished crawling page --${page} of board --${board}")
  }

  override def receive: Receive = {
    // Setup crawling environment and start crawling
    case StartCrawling(from, to) =>
      pagesToCrawl = (from to to).toSet
      // In this first implementation we assume #pages >= #crawlers
      val firstBatch = pagesToCrawl.take(numCrawlersBasic) zip pageCrawlers
      firstBatch foreach { case (page, crawler) => assignPageToCrawl(page, crawler) }

    // When a PageCrawler reports a crawling task is finished
    case FinishedCrawlingPage(_, page) =>
      registerFinishedPage(page)
      if (!pagesToCrawl.isEmpty) assignPageToCrawl(pagesToCrawl.head, sender())
      else if (pagesCrawling.isEmpty) shutDown()

    case _ =>
  }

  private def shutDown() = {
    self ! PoisonPill
    system.terminate
  }
}
