package com.owlunit.core.ii.mutable.impl.actors

import akka.actor.{ActorLogging, Actor}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Merge incoming maps
 *         When countdown is reached, reports result to parent
 */
class MapsMerger(totalWeight: Double, amount: Int) extends Actor with ActorLogging {

  val result = collection.mutable.Map[Long, Double]()
  var countdown = amount

  if (countdown == 0) {
    context.parent ! Merged(Map())
  }

  protected def receive = {

    case LoadedMap(_, mapWeight, data) => {
      for ((id, itemWeight) <- data) {
        if (!result.contains(id))
          result(id) = 0
        result(id) += mapWeight / totalWeight * itemWeight
      }

      countdown -= 1
      if (countdown == 0) {
        context.parent ! Merged(result.toMap)
      }

    }

  }

}
