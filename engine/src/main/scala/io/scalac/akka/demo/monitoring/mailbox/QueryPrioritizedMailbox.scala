package io.scalac.akka.demo.monitoring.mailbox

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import io.scalac.akka.demo.monitoring.DronesRadar

class QueryPrioritizedMailbox(settings: ActorSystem.Settings, cfg: Config) extends UnboundedPriorityMailbox(
	PriorityGenerator {
		case DronesRadar.Message.GetSnapshot => 0
		case _ => 10
	}
)