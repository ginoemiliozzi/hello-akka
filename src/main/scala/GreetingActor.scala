import akka.actor.{Actor, Props}

case class Hello(times: Int = 1)
case class HelloResponse(textMsg: String)
case class Language(lang: String)

class GreetingsActor extends Actor {
  import context._

  var count: Int = 0

  override def preStart(): Unit = println(s"actor starting...")

  override def preRestart(
      reason: Throwable,
      message: Option[Any]
    ): Unit = {
    super.preRestart(reason, message)
    println(s"actor restarting...")
  }

  override def postRestart(reason: Throwable): Unit = println(s"actor restarted...")

  override def postStop(): Unit = println(s"actor stopping...")

  override def receive: Receive = english

  def english: Receive = {
    case Hello(times) =>
      count += 1
      sender ! HelloResponse(s"Hello " * times)
    case Count() =>
      sender ! CountResponse(count)
    case Language("spanish") =>
      become(spanish)
    case CrashRequest(how) =>
      crash(how)
    case _ =>
      unhandled()
  }

  def spanish: Receive = {
    case Hello(times) =>
      count += 1
      sender ! HelloResponse("Hola " * times)
    case Count() =>
      sender ! CountResponse(count)
    case Language("english") =>
      become(english)
    case CrashRequest(how) =>
      crash(how)
    case _ =>
      unhandled()
  }

  def crash(how: String): Unit = {
    how match {
      case "arithmetic" => throw new ArithmeticException
      case "null"       => throw new NullPointerException
      case "illegal"    => throw new IllegalArgumentException
      case _            => throw new Exception
    }
  }
}

object GreetingsActor {
  def props = Props(classOf[GreetingsActor])
}
