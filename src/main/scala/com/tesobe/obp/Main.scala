package com.tesobe.obp

import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by slavisa on 12/27/16.
  */
object Main extends App with StrictLogging with KafkaConfig {

  val systemName = config.getString("system-name")

  val decider: Supervision.Decider = {
    case e: Throwable =>
      logger.error("Exception occurred, stopping...")
      Supervision.Restart
    case _ =>
      logger.error("Unknown problem, stopping...")
      Supervision.Restart
  }

  implicit val system = ActorSystem(s"$systemName-module")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))
  implicit val executionContext = system.dispatcher

  val actorOrchestration = system.actorOf(Props(new ActorOrchestration()), ActorOrchestration.name)

  actorOrchestration ! ProcessorFactory.getProcessor

  Await.ready(system.whenTerminated, Duration.Inf)
}
