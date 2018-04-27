import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class AccountManagerSpec(_system: ActorSystem) extends TestKit(_system)
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {
  //#test-classes

  def this() = this(ActorSystem("AccountManagerSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  it should "reply to account creation" in {
    //#specification-example
    val probe = TestProbe()
    val accountManagerActor = system.actorOf(AccountManager.props())

    accountManagerActor.tell(AccountManager.RequestAccountCreation(1), probe.ref)
    val response = probe.expectMsgType[AccountManager.RespondAccountCreated]
    response.accountId should === (1)
  }


  it should "return the same actor for the same id" in {
    val probe = TestProbe()
    val accountManagerActor = system.actorOf(AccountManager.props())

    accountManagerActor.tell(AccountManager.RequestAccountCreation(1), probe.ref)
    val response = probe.expectMsgType[AccountManager.RespondAccountCreated]
    response.accountId should === (1)

    accountManagerActor.tell(AccountManager.RequestAccountValue(1), probe.ref)
    val actor1 = probe.lastSender

    accountManagerActor.tell(AccountManager.RequestAccountValue(1), probe.ref)
    val actor2 = probe.lastSender

    actor1 should ===(actor2)

  }


}
