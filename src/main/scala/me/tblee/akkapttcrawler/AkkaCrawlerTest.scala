package me.tblee.akkapttcrawler

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
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
  }
}
