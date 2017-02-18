package me.tblee.akkapttcrawler

import java.net.URL

import org.jsoup.{Connection, Jsoup}
import org.jsoup.Connection.Method
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.collection.JavaConverters._

/**
  * Created by tblee on 2/17/17.
  * A parser object to process ptt page information.
  */

case class PostFormField(tag: String, value: String)
case class LinksAndCookies(links: List[String], cookies: Map[String, String])

object Parser {

  val userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1"
  val ageCheckPage = "https://www.ptt.cc/ask/over18"
  val ageCheckYes = PostFormField("yes", "yes")

  def accessPage(link: String): Connection.Response = {
    val connection = Jsoup.connect(link)
    connection
      .userAgent(userAgent)
      .execute
  }

  def createPostRequest(link: String, data: List[PostFormField]): Connection.Response = {

    val addDataToConnection: (Connection, PostFormField) => Connection =
      (con, pd) => con.data(pd.tag, pd.value)

    val connection = Jsoup.connect(link)
    val enrichedConnection: Connection = data match {
      case Nil => connection
      case _ => data.foldLeft[Connection](connection)(addDataToConnection)
    }

    enrichedConnection.userAgent(userAgent)
      .method(Method.POST)
      .execute
  }

  def accessAgeCheck(link: String): Connection.Response = {

    val ageCheckFrom = PostFormField("from", link.substring(link.indexOf("/bbs")))
    createPostRequest(ageCheckPage, List(ageCheckFrom, ageCheckYes))
  }

  def parsePage(link: String) = {
    val articleLinks: LinksAndCookies = parseTableOfContents(link)
    articleLinks.links.foreach{ link => parseArticle(link, articleLinks.cookies) }
  }

  def parseTableOfContents(link: String): LinksAndCookies = {

    val tableOfContentsConnection = accessAgeCheck(link)
    val cookies: Map[String, String] = tableOfContentsConnection.cookies().asScala.toMap
    println(tableOfContentsConnection.cookies)

    val articleLinks: List[String] = tableOfContentsConnection.parse.getElementsByClass("r-ent").asScala
      .map(elem => elem.select("a[href]").attr("abs:href"))
      .filter(elems => elems.size > 0)
      .toList

    LinksAndCookies(articleLinks, cookies)
  }

  def parseArticle(articleLink: String, cookies: Map[String, String]) = {
    val connection = Jsoup.connect(articleLink)

    // Main content includes article title, date, author, text and all push messages
    val mainContent: Element = connection
      .ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .cookies(cookies.asJava)
      .execute
      .parse
      .getElementById("main-content")

    // Extract articl meta data
    val metaTags = mainContent.getElementsByClass("article-meta-tag").asScala.map(elem => elem.text)
    val metaValues = mainContent.getElementsByClass("article-meta-value").asScala.map(elem => elem.text)
    val metaData = metaTags.zip(metaValues)

    mainContent.children().remove()
    val cleanedContent = mainContent.text

    println(cleanedContent)
    println(metaData)
  }
}
