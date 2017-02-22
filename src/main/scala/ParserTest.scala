import me.tblee.akkapttcrawler.utils.Parser

/**
  * Created by tblee on 2/17/17.
  */
object ParserTest {
  def main(args: Array[String]) {
    val articles = Parser.parsePage("https://www.ptt.cc/bbs/Gossiping/index.html")
    //val articles = Parser.parsePage("https://www.ptt.cc/bbs/Baseball/index5025.html")
    println(articles)
  }
}
