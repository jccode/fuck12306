
import scala.io.Source.{fromInputStream}
import java.net._
import java.util.Calendar

import org.json4s._
import org.json4s.native.JsonMethods._

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


object Fuck12306 {

  val interval = 2 * 60 * 1000
  // val date = "2015-02-17"
  // val url = "https://kyfw.12306.cn/otn/lcxxcx/query?purpose_codes=ADULT&queryDate="+date+"&from_station=IOQ&to_station=CNQ"
  val logger = Logger(LoggerFactory.getLogger("Fuck12306"))

  def main(args:Array[String]) {
    loop(interval, runner)
  }

  def loop(interval: Int, callback: () => Unit) = {
    while(true) {
      if(isInValidTime()) 
        callback()
      Thread sleep interval
    }
  }

  def runner(): Unit = {
    checkTickets("2015-02-17", "IOQ", "CNQ", "492429624@qq.com") // shenzhen -> chaoyan

    checkTickets("2015-02-15", "GZQ", "FAQ", "120687689@qq.com") // guangzhou -> shidong
    checkTickets("2015-02-16", "GZQ", "FAQ", "120687689@qq.com")
    checkTickets("2015-02-17", "GZQ", "FAQ", "120687689@qq.com")
  }

  def checkTickets(
    date: String,
    fromStation: String,
    toStation: String,
    email: String
  ): Unit = {
    val now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
    logger.info(now + " check tickets for " + email + " at "+date+" from " + fromStation + " to " + toStation)
    try {
      val jData = getDatas(date, fromStation, toStation).values
      if(jData == None) return;
      val datas = jData.asInstanceOf[List[Map[String, String]]]
      val have_tickets = datas.exists(pTicketExist)
      if(!have_tickets) {
        logger.info("No tickets")
      } else {
        val tickets = datas.filter(pTicketExist).map(ticketInfo)
        val content = tickets.mkString("\n")
        val header = "Tickets in " + date + " from " + fromStation + " to " + toStation + "\n"
        logger.info(header+content)
        sendMail( email, header+content )
      }
    } catch {
      case e: Exception => {
        logger.error("Some unexpected errors occured.", e);
        return;
      }
    }


  }

  def getDatas(date: String, fromStation: String, toStation: String): JValue = {
    val url = "https://kyfw.12306.cn/otn/lcxxcx/query?purpose_codes=ADULT&queryDate="+date+"&from_station="+fromStation+"&to_station="+toStation
    val s = getJsonString(url)
    val json = parse(s)
    json \ "data" \ "datas"
  }

  def getJsonString(url :String): String = {
    SecurityBypasser.destroyAllSSLSecurityForTheEntireVMForever()
    fromInputStream(new URL(url).openStream()).getLines.mkString("\n")
  }

  private def pTicketExist(el: Map[String, String]): Boolean = {
    val keys = List("wz_num", "zy_num", "ze_num")
    val emptyValues = List("无", "*", "--")
    val t = keys.map(k => emptyValues.map(v => el(k) == v).exists(_ == true))
    t.exists(_ == false)
  }

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

  def sendMail(email: String, content: String): Unit = {
    import mail._

    try {
      send a new Mail (
        from = "chen.junchang@163.com" -> "Junchang Chen",
        to = email, // 492429624@qq.com chen.junchang@163.com
        subject = "Tickes! Tickets!",
        message = content
      )
    } catch {
      case e: Exception => logger.error("send mail error. " + e)
    }
  }

  /**
    * valid time is [7am, 11pm]
    */
  def isInValidTime(): Boolean = {
    val now = Calendar.getInstance()
    val am7 = now.clone().asInstanceOf[Calendar]
    am7.set(Calendar.HOUR, 7)
    am7.set(Calendar.MINUTE, 0)
    am7.set(Calendar.SECOND, 0)

    val pm11 = now.clone().asInstanceOf[Calendar]
    pm11.set(Calendar.HOUR, 23)
    pm11.set(Calendar.MINUTE, 0)
    pm11.set(Calendar.SECOND, 0)

    return now.after(am7) && now.before(pm11)
  }
}
