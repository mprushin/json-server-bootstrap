import AccountManager.RequestAccountValue
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object AccountManager {
  def props(): Props = Props(new AccountManager)

  final case class RequestAccountCreation(accountId: Int)

  final case class RespondAccountCreated(accountId: Int)

  final case class RequestAccountValue(accountId: Int)

  final case class RespondAccountValue(accountId: Int, value: Double)

  final case class RequestAccountOperation(accountId: Int, operation: String, value: Double)

  final case class RespondAccountOperation(accountId: Int, operation: String)

}

class AccountManager extends Actor with ActorLogging {
  var idToAccount = Map.empty[Int, ActorRef]
  var accountToId = Map.empty[ActorRef, Int]

  override def preStart(): Unit = log.info("AccountManager started")

  override def postStop(): Unit = log.info("AccountManager stopped")

  override def receive: Receive = {
    case trackMsg@RequestAccountValue(accountId) =>
      idToAccount.get(accountId) match {
        case Some(ref) => ref forward trackMsg
        case None => log.info("there is no such account with id {}", accountId)
      }
    case trackMsg@RequestAccountCreation(accountId) =>
      idToAccount.get(accountId) match {
        case Some(ref) => ref forward trackMsg
        case None => {
          log.info("Creating device actor for {}", trackMsg.accountId)
          val accountActor = context.actorOf(Account.props(accountId), s"account-${trackMsg.accountId}")
          idToAccount += trackMsg.accountId -> accountActor
          accountActor forward trackMsg
        }
      }
    case trackMsg@RequestAccountOperation(accountId, operation, value) =>
      idToAccount.get(accountId) match {
        case Some(ref) => ref forward trackMsg
        case None => log.info("there is no such account with id {}", accountId)
      }
  }
}

object Account {
  def props(accountId: Int): Props = Props(new Account(accountId))
}


class Account(accountId: Int) extends Actor with ActorLogging {
  var accountValue: Double = 0

  override def preStart(): Unit = log.info("Account {} started", accountId)

  override def postStop(): Unit = log.info("Account {} stopped", accountId)

  override def receive = {
    case AccountManager.RequestAccountCreation(`accountId`) => sender() ! AccountManager.RespondAccountCreated(accountId)

    case AccountManager.RequestAccountCreation(accountId) => log.warning("Ignoring request for {}. This actor is responsible for {}.", accountId, this.accountId)

    case AccountManager.RequestAccountOperation(accountId, operation, value) => {
      log.info("Account operation {}-{}-{}", accountId, operation, value)
      accountValue += value
      sender() ! AccountManager.RespondAccountOperation(accountId, operation)
    }

    case AccountManager.RequestAccountValue(accountId) => {
      sender() ! AccountManager.RespondAccountValue(accountId, accountValue)
    }
  }

}
