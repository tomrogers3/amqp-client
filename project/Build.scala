import sbt._
object MyBuild extends Build {
  val root = Project(id = "amqp-client", base = file("."))
}

