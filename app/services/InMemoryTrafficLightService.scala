package services
import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import models.{Color, TrafficLight}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

@ImplementedBy(classOf[InMemoryTrafficLightService])
trait TrafficLightService { // this is the trait to be extended
  def all: Seq[TrafficLight]
  def get(id: Int): Option[TrafficLight]
  def getFuture(id: Int): Future[Option[TrafficLight]]
  def save(tl: TrafficLight): Unit
  def changeToGreenFromRed(id: Int): Future[TrafficLight]
  def changeToRedFromGreen(id: Int): Future[TrafficLight]
  def changeToRedFromOrange(id: Int): Future[TrafficLight]

}
@Singleton
class InMemoryTrafficLightService @Inject() (config: Config) extends TrafficLightService {

  private var ongoingRequests: Map[Int, Future[TrafficLight]] = Map.empty

  private var db = Map( // this is the in-memory map
    1 -> TrafficLight(1, Color.Green),
    2 -> TrafficLight(2, Color.Orange),
    3 -> TrafficLight(3, Color.Red)
  )

  override def all: Seq[TrafficLight] = {
    db.values.toSeq.sortBy(_.id)
  }

  override def get(id: Int): Option[TrafficLight] = {
    db.get(id)
  }

  override def getFuture(id: Int): Future[Option[TrafficLight]] = Future {
    db.get(id)
  }

  override def save(tl: TrafficLight): Unit = {
    db += (tl.id -> tl)
  }

  def trafficLightsList: List[TrafficLight] =
    db.values.toList

  override def changeToGreenFromRed(id: Int): Future[TrafficLight] = Future {
    // Step 1. Make the light Green
    val greenTrafficLight = TrafficLight(id, Color.Green)
    db += id -> greenTrafficLight

    greenTrafficLight
  }

  System.setProperty("traffic-light.duration", "This value comes from a system property")

  override def changeToRedFromGreen(id: Int): Future[TrafficLight] = {
    val request = Future {
      // Step 1. Make the light Orange
      val orangeTrafficLight = TrafficLight(id, Color.Orange)
      db += id -> orangeTrafficLight

      // Step 2. Wait 15 seconds
      Thread.sleep(5000)

      // Step 3. Make the light Red
      val redTrafficLight = TrafficLight(id, Color.Red)
      db += id -> redTrafficLight

      // Step 4. Finish the request.
      ongoingRequests -= id

      redTrafficLight
    }

    ongoingRequests += id -> request

    request
  }

  override def changeToRedFromOrange(id: Int): Future[TrafficLight] = {
    val ongoingRequestOpt = ongoingRequests.get(id)
    ongoingRequestOpt match {
      case Some(ongoingRequest) =>
        ongoingRequest
      case None =>
        Future.successful(TrafficLight(id, Color.Red))
    }
  }
}
