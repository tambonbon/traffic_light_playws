package controllers

import dao.dao.TrafficLightDAO
import javax.inject._
import models.TrafficLightJson
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(ws: WSClient,
                               trafficLight: TrafficLightDAO,
                               controllerComponents: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(controllerComponents) with TrafficLightJson {

  val url: String = "http://localhost:9000"

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }


  def all = Action.async {
    trafficLight.all().map(tl => Ok(Json.toJson(tl)))
  }
  def get(ID: Int) = Action.async {
    trafficLight.get(ID).map{
      case Some(tl) => Ok(Json.toJson(tl))
      case None     => NotFound(Json.obj("message" -> "No traffic light found"))
    }
  }
  def goTillRed(fromID: Int) = Action.async {
    trafficLight.getFirstRedOrOrangeLight(fromID).map{
      case Some(tl) => Ok(Json.toJson(tl))
      case None     => NotFound(Json.obj("message" -> "No traffic light found"))
    }
  }


//  def goTillRed = Action {
    //    val request: WSRequest = ws.url(url)
    val futureResponse: Future[WSResponse] = for {
      responseOne <- ws.url("http://localhost:9000/go-to-red").get()
      responseTwo <- ws.url(responseOne.body).get()
    } yield responseTwo
    futureResponse.recover {
      case e: Exception =>
        val exceptionData = Map("error" -> Seq(e.getMessage))
        ws.url("http://localhost:9000/").post(exceptionData)
    }
//  }
}
