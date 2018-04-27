package Actors

import akka.actor.{Actor, ActorLogging, Props}

object Account {
  def props(accountId: Int): Props = Props(new Account(accountId))
}


class Account(accountId: Int) extends Actor with ActorLogging {

  var operations: List[(String, Double)] = Nil

  override def preStart(): Unit = log.info("Actors.Account {} started", accountId)

  override def postStop(): Unit = log.info("Actors.Account {} stopped", accountId)

  override def receive = {
    case AccountManager.RequestAccountCreation(`accountId`) => sender() ! AccountManager.RespondAccountCreated(accountId)

    case AccountManager.RequestAccountCreation(accountId) => log.warning("Ignoring request for {}. This actor is responsible for {}.", accountId, this.accountId)

    case AccountManager.RequestAccountOperation(accountId, operation, value) => {
      log.info("Actors.Account operation {}-{}-{}", accountId, operation, value)
      operations = (operation, value) :: operations
      sender() ! AccountManager.RespondAccountOperation(accountId, operation)
    }

    case AccountManager.RequestAccountValue(accountId) => {
      sender() ! AccountManager.RespondAccountValue(accountId, operations.map(_._2).sum)
    }

  }
}
