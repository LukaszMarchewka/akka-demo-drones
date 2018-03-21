package io.scalac.akka.demo.monitoring.mailbox

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

class QueryPrioritizedMailbox(settings: ActorSystem.Settings, cfg: Config) extends UnboundedPriorityMailbox(
	PriorityGenerator {
		case _: QueryPrioritizedMailbox.QueryMessage => 0
		case _ => 10
	}
)

object QueryPrioritizedMailbox {

	trait QueryMessage

}