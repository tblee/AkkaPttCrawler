package me.tblee.akkapttcrawler

import akka.actor.{ActorSystem, Props}
import me.tblee.akkapttcrawler.actors.Supervisor
import me.tblee.akkapttcrawler.utils.Messages._

/**
  * Created by tblee on 2/18/17.
  */
object StartAkkaCrawler {
  def main(args: Array[String]) {

    val system = ActorSystem()

    val board = args(0)
    val startPage = args(1).toInt
    val endPage = args(2).toInt

    val supervisor = system.actorOf(Props(new Supervisor(system, board)))
    supervisor ! StartCrawling(startPage, endPage)
  }
}
