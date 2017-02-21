package me.tblee.akkapttcrawler

import java.io.File

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import me.tblee.akkapttcrawler.actors.{PageCrawler, Supervisor}
import me.tblee.akkapttcrawler.utils.Messages._

/**
  * Created by tblee on 2/18/17.
  */
object AkkaCrawlerTest {
  def main(args: Array[String]) {

    val system = ActorSystem()

    val board = "Gossiping"

    val supervisor = system.actorOf(Props(new Supervisor(system, board)))
    supervisor ! StartCrawling(8810, 18809)
  }
}
