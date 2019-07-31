import akka.actor.{ActorRef, ActorSystem, PoisonPill}

object HelloAkka extends App {
  val system: ActorSystem  = ActorSystem.create("hello-system")
  val supervisor: ActorRef = system.actorOf(SupervisorActor.props)

  supervisor ! Hello(5)
  supervisor ! Count()
  supervisor ! KillYourChild()

  Thread.sleep(2000)

  supervisor ! PoisonPill

  system.terminate()
}
