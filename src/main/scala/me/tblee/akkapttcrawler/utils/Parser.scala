package me.tblee.akkapttcrawler.utils

import com.sun.deploy.util.BlackList
import org.jsoup.Connection.Method
import org.jsoup.nodes.Element
import org.jsoup.{Connection, Jsoup}

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by tblee on 2/17/17.
  * A parser object to process ptt page information.
  */

case class PostFormField(tag: String, value: String)
case class LinksAndCookies(links: Option[List[String]], cookies: Option[Map[String, String]])
case class PushContent(pushTag: String, userId: String, content: String, ipDateTime: String)
case class PttArticle(articleId: String, metaData: Map[String, String], content: String, push: List[PushContent])

object Parser {

  val parsePageUtils = ParsePageUtils

  def parsePage(link: String, blackList: Set[String] = Set[String]()): Try[List[PttArticle]] = {

    for {
      articleLinks <- Try(parseTableOfContents(link, blackList))
      pttArticles <- Try{
        articleLinks.links map { links =>
          links.map{ link =>
            parseArticle(link, articleLinks.cookies.getOrElse(Map[String, String]()), blackList)
          }.flatten
        }
      }
    } yield pttArticles.getOrElse(List[PttArticle]())
  }

  private def parseTableOfContents(link: String, blackList: Set[String]): LinksAndCookies = {

    // Ptt has age check. For the first time we access every page of a board, we access through the
    // age checking page then save the cookie for the use of following article crawling.
    val maybeCnResponse = parsePageUtils.accessAgeCheck(link, blackList)

    val cookies: Option[Map[String, String]] =
      maybeCnResponse map { cnResponse => cnResponse.cookies().asScala.toMap }

    val articleLinks: Option[List[String]] =
      maybeCnResponse map { cnResponse =>
        cnResponse.parse.getElementsByClass("r-ent").asScala
          .map(elem => elem.select("a[href]").attr("abs:href"))
          .filter(elems => elems.size > 0)
          .toList
      }

    LinksAndCookies(articleLinks, cookies)
  }

  private def parseArticle(articleLink: String, cookies: Map[String, String], blackList: Set[String]): Option[PttArticle] = {

    val articleId = articleLink.substring(articleLink.lastIndexOf("/") + 1)

    // Main content includes article title, date, author, text and all push messages
    val maybePage: Option[Connection.Response] = parsePageUtils.accessPageWithCookies(articleLink, cookies, blackList)
    maybePage map { page =>
      val mainContent: Element = page.parse.getElementById("main-content")

      // Extract article meta data
      val metaTags = mainContent.getElementsByClass("article-meta-tag").asScala.map(elem => elem.text)
      val metaValues = mainContent.getElementsByClass("article-meta-value").asScala.map(elem => elem.text)
      val metaData = metaTags.zip(metaValues).toMap

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

  def createConnection(link: String, blackList: Set[String] = Set()): Option[Connection] = {
    val connection = Option(link) filter {l => !blackList.contains(l)} map {l => Jsoup.connect(l)}
    connection
  }

  def accessPageWithCookies(link: String, cookies: Map[String, String], blackList: Set[String] = Set[String]()): Option[Connection.Response] = {
    val maybeConnection = createConnection(link, blackList)
    maybeConnection map {
      connection =>
        connection
          .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
          .cookies(cookies.asJava)
          .execute
    }

  }

  def createPostRequest(link: String, data: List[PostFormField], blackList: Set[String] = Set[String]()): Option[Connection.Response] = {

    val addDataToConnection: (Connection, PostFormField) => Connection =
      (con, pd) => con.data(pd.tag, pd.value)

    val maybeConnection = createConnection(link, blackList)

    maybeConnection map {
      connection =>
        data match {
          case Nil =>
            connection.userAgent(userAgent).execute
          case _ =>
            data.foldLeft[Connection](connection)(addDataToConnection).userAgent(userAgent).method(Method.POST).execute
        }
    }
  }

  def accessAgeCheck(link: String, blackList: Set[String] = Set[String]()): Option[Connection.Response] = {

    val ageCheckFrom = PostFormField("from", link.substring(link.indexOf("/bbs")))
    createPostRequest(ageCheckPage, List(ageCheckFrom, ageCheckYes), blackList)
  }
}