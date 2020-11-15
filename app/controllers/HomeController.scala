package controllers

import dao.TrafficLightDAO
import javax.inject._
import models.TrafficLightJson
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(trafficLight: TrafficLightDAO,
                               controllerComponents: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(controllerComponents) with TrafficLightJson {

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
}
