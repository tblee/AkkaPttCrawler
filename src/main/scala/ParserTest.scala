import me.tblee.akkapttcrawler.Parser

/**
  * Created by tblee on 2/17/17.
  */
object ParserTest {
  def main(args: Array[String]) {
    Parser.parsePage("https://www.ptt.cc/bbs/Gossiping/index.html")
    //Parser.parsePage("https://www.ptt.cc/bbs/Baseball/index5025.html")
  }
}
