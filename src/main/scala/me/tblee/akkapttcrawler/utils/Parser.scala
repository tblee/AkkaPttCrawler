package me.tblee.akkapttcrawler.utils

import org.jsoup.Connection.Method
import org.jsoup.nodes.Element
import org.jsoup.{Connection, Jsoup}

import scala.collection.JavaConverters._

/**
  * Created by tblee on 2/17/17.
  * A parser object to process ptt page information.
  */

case class PostFormField(tag: String, value: String)
case class LinksAndCookies(links: List[String], cookies: Map[String, String])
case class PushContent(pushTag: String, userId: String, content: String, ipDateTime: String)
case class PttArticle(articleId: String, metaData: List[(String, String)], content: String, push: List[PushContent])

object Parser {

  val parsePageUtils = ParsePageUtils

  def parsePage(link: String): List[PttArticle] = {
    val articleLinks: LinksAndCookies = parseTableOfContents(link)
    articleLinks.links.map{ link => parseArticle(link, articleLinks.cookies) }
  }

  def parseTableOfContents(link: String): LinksAndCookies = {

    // Ptt has age check. For the first time we access every page of a board, we access through the
    // age checking page then save the cookie for the use of following article crawling.
    val tableOfContentsConnection = parsePageUtils.accessAgeCheck(link)
    val cookies: Map[String, String] = tableOfContentsConnection.cookies().asScala.toMap

    val articleLinks: List[String] = tableOfContentsConnection.parse.getElementsByClass("r-ent").asScala
      .map(elem => elem.select("a[href]").attr("abs:href"))
      .filter(elems => elems.size > 0)
      .toList

    LinksAndCookies(articleLinks, cookies)
  }

  def parseArticle(articleLink: String, cookies: Map[String, String]): PttArticle = {

    val articleId = articleLink.substring(articleLink.lastIndexOf("/") + 1)

    // Main content includes article title, date, author, text and all push messages
    val mainContent: Element = parsePageUtils.accessPageWithCookies(articleLink, cookies).parse.getElementById("main-content")

    // Extract article meta data
    val metaTags = mainContent.getElementsByClass("article-meta-tag").asScala.map(elem => elem.text)
    val metaValues = mainContent.getElementsByClass("article-meta-value").asScala.map(elem => elem.text)
    val metaData = metaTags.zip(metaValues).toList

    // Extract push data
    val pushData: List[PushContent] =
      mainContent.getElementsByClass("push").asScala.map(elem => parseSinglePushContent(elem)).toList

    // Extract article text
    mainContent.children().remove()
    val cleanedContent = mainContent.text

    PttArticle(
      articleId = articleId,
      metaData = metaData,
      content = cleanedContent,
      push = pushData)
  }

  def parseSinglePushContent(push: Element): PushContent = {

    val eliminatePush = ": "

    val pushTag = push.getElementsByClass("push-tag").text
    val pushId = push.getElementsByClass("push-userid").text
    val pushIpDateTime = push.getElementsByClass("push-ipdatetime").text
    val pushContent = push.getElementsByClass("push-content").text
    val modifiedPushContent =
      if (pushContent.indexOf(eliminatePush) > -1) pushContent.substring(pushContent.indexOf(eliminatePush) + eliminatePush.size)
      else pushContent

    PushContent(pushTag, pushId, modifiedPushContent, pushIpDateTime)
  }
}

object ParsePageUtils {

  val userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1"
  val ageCheckPage = "https://www.ptt.cc/ask/over18"
  val ageCheckYes = PostFormField("yes", "yes")

  def accessPageWithCookies(link: String, cookies: Map[String, String]): Connection.Response = {
    val connection = Jsoup.connect(link)
    connection
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .cookies(cookies.asJava)
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
}
