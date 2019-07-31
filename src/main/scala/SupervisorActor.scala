import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Count()
case class CountResponse(count: Int)
case class CrashRequest(how: String)
case class KillYourChild()

class SupervisorActor extends Actor {

  val greeter: ActorRef = context.watch(context.actorOf(GreetingsActor.props))

  override def supervisorStrategy: SupervisorStrategy = one

  override def receive: Receive = {
    case x: Hello =>
      greeter ! x

    case response: HelloResponse =>
      println(response.textMsg)

    case c: Count =>
      implicit val timeout: Timeout = new Timeout(1 seconds)
      val future                    = (greeter ? c).mapTo[CountResponse]
      future pipeTo self

    case cr: CountResponse =>
      println(s"response is ${cr.count}")

    case l: Language =>
      greeter ! l

    case crash: CrashRequest =>
      greeter ! crash

    case kill: KillYourChild =>
      greeter ! PoisonPill

    case e =>
      println(s"other message $e")
      unhandled()
  }

  def one: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 10 seconds) {
      case _: ArithmeticException      => Resume
      case _: NullPointerException     => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception                => Escalate
    }
  }
}

object SupervisorActor {
  def props = Props(classOf[SupervisorActor])
}
