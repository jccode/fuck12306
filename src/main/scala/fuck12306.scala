
import scala.io.Source.{fromInputStream}
import java.net._

import org.json4s._
import org.json4s.native.JsonMethods._

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


object Fuck12306 {

  val interval = 2 * 60 * 1000
  val date = "2015-02-19"
  val url = "https://kyfw.12306.cn/otn/lcxxcx/query?purpose_codes=ADULT&queryDate="+date+"&from_station=IOQ&to_station=CNQ"
  val logger = Logger(LoggerFactory.getLogger("Fuck12306"))

  def main(args:Array[String]) {
    loop(interval, runner)
  }

  def loop(interval: Int, callback: () => Unit) = {
    while(true) {
      callback()
      Thread sleep interval
    }
  }

  def runner(): Unit = {
    val now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
    logger.info(now + " check tickets. ")

    if(getDatas().values == None) return;

    val datas = getDatas().values.asInstanceOf[List[Map[String, String]]]
    val have_tickets = datas.exists(pTicketExist)
    if(!have_tickets) {
      logger.info("No tickets")
    } else {
      val tickets = datas.filter(pTicketExist).map(ticketInfo)
      val content = tickets.mkString("\n")
      val header = "Tickets in " + date + "\n"
      logger.info(header+content)
      sendMail( header+content )
    }
  }

  def getDatas(): JValue = {
    val s = getJsonString()
    val json = parse(s)
    json \ "data" \ "datas"
  }

  def getJsonString(): String = {
    SecurityBypasser.destroyAllSSLSecurityForTheEntireVMForever()
    fromInputStream(new URL(url).openStream()).getLines.mkString("\n")
  }

  private def pTicketExist(el: Map[String, String]): Boolean =
    el("wz_num") != "无" || el("zy_num") != "无" || el("ze_num") != "无"

  private def ticketInfo(el: Map[String, String]): String =
    "[" + el("start_time") + " - " + el("arrive_time") + "]" +
  (if(el("wz_num") != "无") "无座"+el("wz_num")+" " else "") +
  (if(el("zy_num") != "无") "一等座"+el("zy_num")+" " else "") +
  (if(el("ze_num") != "无") "二等座"+el("ze_num")+" " else "")

  private def looptest(): Unit = {
    loop(interval, () => {
      println("hello")
    })
  }

  def sendMail(content: String): Unit = {
    import mail._

    send a new Mail (
      from = "chen.junchang@163.com" -> "Junchang Chen",
      to = "492429624@qq.com", // 492429624@qq.com chen.junchang@163.com
      subject = "Tickes! Tickets!",
      message = content
    )
  }
}
