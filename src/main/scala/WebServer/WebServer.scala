package WebServer

import Actors.AccountManager
import Actors.AccountManager.{RequestAccountValue, RespondAccountValue}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.io.StdIn
import scala.concurrent.duration._

object WebServer {
  implicit val respondAccountValueFormat = jsonFormat2(RespondAccountValue)

  def main(args: Array[String]) {

    implicit val system = ActorSystem("json-server")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val accountManager = system.actorOf(AccountManager.props())
    accountManager ! AccountManager.RequestAccountCreation(1)

    val route =
      path("increase-by-one") {
        get {
          accountManager ! AccountManager.RequestAccountOperation(1, "increase by one", 1)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Increased</h1>"))
        }
      } ~
        path("get-value") {
          get {
            implicit val timeout: Timeout = 5.seconds
            val value : Future[RespondAccountValue] = (accountManager ? RequestAccountValue(1)).mapTo[RespondAccountValue]
            complete(value)
          }
        }


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
