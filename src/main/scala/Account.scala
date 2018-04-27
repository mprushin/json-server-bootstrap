import akka.actor.{Actor, ActorLogging, Props}

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
