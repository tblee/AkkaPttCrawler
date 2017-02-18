package me.tblee.akkapttcrawler.actors

import akka.actor.{Actor, ActorLogging}

/**
  * Created by tblee on 2/17/17.
  * A crawling actor class that is managed by the supervisor actor. The crawler
  * actor takes in a specific board and page then instantiate parsers to parse all
  * the articles in that given page.
  */
class PageCrawler extends Actor with ActorLogging{

  override def receive: Receive = {
    case _ =>
  }
}
