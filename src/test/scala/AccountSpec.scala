import Actors.{Account, AccountManager}
import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class AccountSpec(_system: ActorSystem) extends TestKit(_system)
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("AccountSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  it should "reply to creation requiest with corect accountId" in {
    val accountId = 5
    val probe = TestProbe()
    val accountActor = system.actorOf(Account.props(accountId))
    accountActor.tell(AccountManager.RequestAccountCreation(accountId), probe.ref)
    val response = probe.expectMsgType[AccountManager.RespondAccountCreated]
    response.accountId should === (accountId)
  }

  it should "ignore creation request with incorrect accountId" in {
    val accountId = 5
    val incorectAccountId = 6
    val probe = TestProbe()
    val accountActor = system.actorOf(Account.props(accountId))
    accountActor.tell(AccountManager.RequestAccountCreation(incorectAccountId), probe.ref)
    probe.expectNoMsg(1.second)
  }

  it should "increase value by operation" in {
    val accountId = 1
    val probe = TestProbe()
    val accountActor = system.actorOf(Account.props(accountId))

    accountActor.tell(AccountManager.RequestAccountValue(accountId), probe.ref)
    val response1 = probe.expectMsgType[AccountManager.RespondAccountValue]
    response1.value should === (0)

    accountActor.tell(AccountManager.RequestAccountOperation(accountId, "add 5", 5), probe.ref)
    val response2 = probe.expectMsgType[AccountManager.RespondAccountOperation]
    response2.accountId should === (accountId)

    accountActor.tell(AccountManager.RequestAccountValue(accountId), probe.ref)
    val response3 = probe.expectMsgType[AccountManager.RespondAccountValue]
    response3.value should === (5)
  }

}
