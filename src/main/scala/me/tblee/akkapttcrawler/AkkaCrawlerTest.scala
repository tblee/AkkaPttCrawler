package me.tblee.akkapttcrawler

import akka.actor.{Actor, ActorSystem, Props}
import me.tblee.akkapttcrawler.actors.{PageCrawler, Supervisor}
import me.tblee.akkapttcrawler.utils.Messages._

/**
  * Created by tblee on 2/18/17.
  */
object AkkaCrawlerTest {
  def main(args: Array[String]) {

    val system = ActorSystem()

    val supervisor = system.actorOf(Props(new Supervisor(system, "Gossiping")))
    supervisor ! StartCrawling(19920, 19931)

    /*
    val pageCrawler1 = system.actorOf(Props(new PageCrawler))
    val pageCrawler2 = system.actorOf(Props(new PageCrawler))
    val pageCrawler3 = system.actorOf(Props(new PageCrawler))
    val pageCrawler4 = system.actorOf(Props(new PageCrawler))

    pageCrawler1 ! StartCrawlingPage("Gossiping", 19931)
    pageCrawler2 ! StartCrawlingPage("Gossiping", 19930)
    pageCrawler3 ! StartCrawlingPage("Gossiping", 19929)
    pageCrawler4 ! StartCrawlingPage("Gossiping", 19928) */
  }
}
