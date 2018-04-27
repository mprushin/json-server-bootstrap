import AccountManager.{RequestAccountCreation, RequestAccountOperation, RequestAccountValue}
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
          log.info("Creating account actor for {}", trackMsg.accountId)
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
