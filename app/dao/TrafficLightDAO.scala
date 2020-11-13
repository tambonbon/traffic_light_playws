package dao

package dao

import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import models.{Color, TrafficLight, TrafficLightJson}
import models.Color.{Color, Orange, Red}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.util.Try
//import slick.jdbc.JdbcProfile

import scala.concurrent.Future

@ImplementedBy(classOf[TrafficLightDAO])
trait TrafficLightDAOComponent { self : HasDatabaseConfigProvider[JdbcProfile] => // import Jdbc first
  import profile.api._

  implicit lazy val myColorMapper = MappedColumnType.base[Color, String](
    e => e.toString,
    s =>
      Try(Color.withName(s)).getOrElse(
        throw new IllegalArgumentException(s"enumeration $s doesn't exist $Color[${Color.values.mkString(",")}]")
      )
  )

  class TrafficLightsTable(tag: Tag) extends Table[TrafficLight](tag, "TrafficLights") {
    def id = column[Int]("id", O.PrimaryKey)

    def lights = column[Color]("color")

    def * = (id, lights) <> ((TrafficLight.apply _).tupled, TrafficLight.unapply)
  }

//  def all(): Future[Seq[TrafficLight]]
//  def get(id: Int): Future[Option[TrafficLight]]
//  def save(tl: TrafficLight): Future[Unit]
//  def changeToGreenFromRed(id: Int): Future[TrafficLight]
//  def changeToRedFromGreen(id: Int): Future[TrafficLight]
//  def changeToRedFromOrange(id: Int): Future[TrafficLight]


}

@Singleton()
class TrafficLightDAO @Inject() (config: Config,
                                 protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with TrafficLightDAOComponent with TrafficLightJson {
  //  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import profile.api._

  val trafficLights = TableQuery[TrafficLightsTable] // This is the actual query

  /*
  * Populating a database
  * */
  val setup = DBIO.seq(
    // Create the table
    trafficLights.schema.create,
    // Insert some lights
    trafficLights ++= Seq(
      TrafficLight(1, Color.Green),
      TrafficLight(2, Color.Green),
      TrafficLight(3, Color.Red),
      TrafficLight(4, Color.Red),
      TrafficLight(5, Color.Orange),
      TrafficLight(6, Color.Green),
      TrafficLight(7, Color.Orange),

    )
  )
  val setupFuture = dbConfig.db.run(setup)

  /** Insert new lights */
  def save(light: TrafficLight): Future[Unit] = { //update

    dbConfig.db.run(trafficLights += light).map(_ => ())
  }

  /** Show all lights*/
  def all(): Future[Seq[TrafficLight]] = {
    dbConfig.db.run(trafficLights.result)
  }
  def get(id: Int): Future[Option[TrafficLight]] = {
    dbConfig.db.run(trafficLights.filter(tl => tl.id === id).result.headOption)
  }
  def delete(id: Int): Future[Int] = {
    dbConfig.db.run(trafficLights.filter(tl => tl.id === id).delete)
  }
  def getFirst: Future[Option[TrafficLight]] = {
    dbConfig.db.run(trafficLights.filter(tl => tl.id === 1).result.headOption)
  }
  def getFirstRedOrOrangeLight(fromID: Int): Future[Option[TrafficLight]] = {
    val trafficLightWOFromID = trafficLights.filter(tl => tl.id > fromID)
    dbConfig.db.run(
      trafficLightWOFromID.filter(tl => tl.lights === Color.Orange || tl.lights === Red).result.headOption
    )
  }
//  def getPathtoRedOrOrange(fromID: Int) : Future[Seq[TrafficLight]] = {
//    val trafficLightRes = trafficLights.filter(_.id >= fromID)
//    val trafficLightWOFromID = trafficLights.filter(_.id > fromID)
//    val trafficLightStop     = trafficLightWOFromID.filter(tl => tl.lights === Color.Orange || tl.lights === Red).take(1)
//    val stopID = trafficLightStop.map(tl => tl.id)
//    val res = (trafficLightRes.filter(_.id < 2)
//  }

  def stopAtOrangeOrRed(fromID: Int): Future[Option[TrafficLight]] = {
    val currentColor =  (dbConfig.db.run(trafficLights.filter(_.id === fromID).result.map(_.headOption.map(tl => tl.color)) )  )
    if (currentColor == Red || currentColor == Orange) {
      val any =  dbConfig.db.run(trafficLights.filter(_.id === fromID).result.headOption)
      any
    }
    else {
      stopAtOrangeOrRed(fromID+1)
    }

  }

  private var ongoingRequests: Map[Int, Future[TrafficLight]] = Map.empty

 /* def changeToGreenFromRed(id: Int): Future[TrafficLight] = Future {
    // Step 1. Make the light Green
    val greenTrafficLight = TrafficLight(id, Color.Green)
    dbConfig.db.run(trafficLights +=  greenTrafficLight)

    greenTrafficLight
  }

  System.setProperty("traffic-light.duration", "This value comes from a system property")

  def changeToRedFromGreen(id: Int): Future[TrafficLight] = {
    val request = Future {
      // Step 1. Make the light Orange
      val orangeTrafficLight = TrafficLight(id, Color.Orange)
      dbConfig.db.run(trafficLights +=  orangeTrafficLight)

      // Step 2. Wait 15 seconds
      Thread.sleep(config.getInt("traffic-light.duration"))

      // Step 3. Make the light Red
      val redTrafficLight = TrafficLight(id, Color.Red)
      dbConfig.db.run(trafficLights +=  redTrafficLight)

      // Step 4. Finish the request.
      ongoingRequests -= id

      redTrafficLight
    }

    ongoingRequests += id -> request

    request
  }

  def changeToRedFromOrange(id: Int): Future[TrafficLight] = {
    val ongoingRequestOpt = ongoingRequests.get(id)
    ongoingRequestOpt match {
      case Some(ongoingRequest) =>
        ongoingRequest
      case None =>
        Future.successful(TrafficLight(id, Color.Red))
    }
  }*/

  //  def changeToGreenFromRed(id: Int): Future[TrafficLight] = Future {
  //    // Step 1. Make the light Green
  //    val greenTrafficLight = TrafficLight(id, Color.Green)
  //
  //    val q = for { c <- trafficLights if c.id === id } yield c.lights
  //    val updateAction = q.update(Color.Red)
  //    // Get the statement without having to specify an updated value:
  //    //    val sql = q.updateStatement
  //    dbConfig.db.run(updateAction) // this is a side-effect
  //  // use that returning value to return instead of greenTL
  //    greenTrafficLight
  //  }

}
