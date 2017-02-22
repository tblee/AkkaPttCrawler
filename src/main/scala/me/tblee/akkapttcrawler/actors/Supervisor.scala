package me.tblee.akkapttcrawler.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import me.tblee.akkapttcrawler.utils.Messages._
import org.jsoup.HttpStatusException

/**
  * Created by tblee on 2/18/17.
  */
class Supervisor(system: ActorSystem, board: String) extends Actor with ActorLogging{

  val numCrawlersBasic = 15

  var pagesToCrawl: Set[Int] = Set()
  var pagesCrawling: Set[Int] = Set()

  // Fire up PageCrawler actors
  var pageCrawlers = (1 to numCrawlersBasic) map {id => system.actorOf(Props(new PageCrawler(self)))}

  // Fire up FileWriter actor
  val fileWriter = system.actorOf(Props(new FileWriter(new File(s"${board}_crawled.json"))))

  def assignPageToCrawl(page: Int, crawler: ActorRef) = {
    pagesToCrawl -= page
    pagesCrawling += page
    crawler ! StartCrawlingPage(board, page, fileWriter)
  }

  def registerFinishedPage(page: Int) = {
    pagesCrawling -= page
    log.info(s"Finished crawling page --${page} of board --${board}")
  }

  def registerFailedPage(page: Int) = {
    pagesCrawling -= page
    pagesToCrawl += page
    log.info(s"Failed crawling page --${page} of board --${board}, will try again...")
  }

  override def receive: Receive = {
    // Setup crawling environment and start crawling
    case StartCrawling(from, to) =>
      pagesToCrawl = (from to to).toSet
      log.info(s"Start crawling...")
      // This works even when #crawlers > #pages
      val firstBatch = pagesToCrawl.take(numCrawlersBasic) zip pageCrawlers
      firstBatch foreach { case (page, crawler) => assignPageToCrawl(page, crawler) }

    // When a PageCrawler reports a crawling task is finished
    case FinishedCrawlingPage(_, page) =>
      registerFinishedPage(page)
      if (!pagesToCrawl.isEmpty) assignPageToCrawl(pagesToCrawl.head, sender())
      else if (pagesCrawling.isEmpty) shutDown()

    // When a Page Crawler reports failure in crawling a page
    case FailedCrawlingPage(_, page, err) =>
      // Handle error message, if some URL has HttpStatusException, add it to blacklist
      err match {
        case error: HttpStatusException =>
          val badURL = error.getUrl
          log.info(s"The URL --${badURL} of page --${page} of board --${board} is not reachable, add to black list...")
          pageCrawlers foreach {pageCrawler => pageCrawler ! UpdateBlackList(badURL)}
        case _ =>
      }
      registerFailedPage(page)
      assignPageToCrawl(pagesToCrawl.head, sender())

    case _ =>
  }

  private def shutDown() = {
    self ! PoisonPill
    system.terminate
  }
}
