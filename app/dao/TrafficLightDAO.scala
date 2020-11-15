package dao

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import models.Color.{Color, Red}
import models.{Color, TrafficLight, TrafficLightJson}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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

}

@Singleton()
class TrafficLightDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
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
  /** Get a single light */
  def get(id: Int): Future[Option[TrafficLight]] = {
    dbConfig.db.run(trafficLights.filter(tl => tl.id === id).result.headOption)
  }
  /** Delete a light*/
  def delete(id: Int): Future[Int] = {
    dbConfig.db.run(trafficLights.filter(tl => tl.id === id).delete)
  }
  /** the name says it all */
  def getFirstRedOrOrangeLight(fromID: Int): Future[Option[TrafficLight]] = {
    val trafficLightWOFromID = trafficLights.filter(tl => tl.id > fromID)
    dbConfig.db.run(
      trafficLightWOFromID.filter(tl => tl.lights === Color.Orange || tl.lights === Red).result.headOption
    )
  }

}
