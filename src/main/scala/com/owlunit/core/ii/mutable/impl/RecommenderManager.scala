package com.owlunit.core.ii.mutable.impl

import actors._
import akka.actor.{Props, ActorSystem}
import com.owlunit.core.ii.mutable.{IiDao, Recommender, Ii}
import com.weiglewilczek.slf4s.Logging
import akka.util.duration._
import akka.dispatch.{Promise, ExecutionContext, Future, Await}
import akka.pattern.ask
import akka.util.{Duration, Timeout}



/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */


class RecommenderManager(dao: IiDao, name: String) extends Recommender with Logging {

  implicit val system = ActorSystem(name)
  val config = Settings(system)
  implicit val timeout: Timeout = Timeout(config.timeout)


  def init(){}

  def shutdown() {
    system.shutdown()
  }


  def recommend(query: Map[Ii, Double], key: String, limit: Int): List[(Ii, Double)] = {

    logger.debug("recommending for query %s" format query)

    val actor = system.actorOf(Props[RecommenderActor])

    val idQuery = query.map{case (ii, w) => (ii.id, w)}.toMap
    val future = actor ? FindSimilar(idQuery, key, limit)
    val idList = Await.result(future, config.timeout).asInstanceOf[Similar]

    system.stop(actor)

    idList.value.map{ case (id, w) => (dao.load(id), w)}

  }

  def compare(a: Ii, b: Ii): Double = {

    val depth = config.indirectDepth
    val loader = system.actorOf(Props(new Loader(dao)))
    val comparator = system.actorOf(Props[Comparator])

    val future = for {
      aIndirect <- (loader ? LoadIndirect(a.id, 1, depth)).mapTo[WeightedMap]
      bIndirect <- (loader ? LoadIndirect(a.id, 1, depth)).mapTo[WeightedMap]
      result <- (comparator ? Maps(aIndirect.map, bIndirect.map)).mapTo[Likeness]
    } yield {
      result
    }

    val value = Await.result(future, config.timeout)

    system.stop(loader)
    system.stop(comparator)

    value.value
  }

  def getSimilar(a: Ii, key: String, limit: Int): List[(Ii, Double)] = null
}
