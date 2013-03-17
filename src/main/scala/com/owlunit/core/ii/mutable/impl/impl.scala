package com.owlunit.core.ii.mutable

import org.neo4j.helpers.collection.MapUtil
import org.neo4j.graphdb.index.IndexManager
import org.neo4j.graphdb.{RelationshipType, Relationship, Direction, Node}
import org.neo4j.kernel.{Uniqueness, Traversal}
import org.neo4j.graphdb.traversal.Evaluators

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
package object impl {

  private[impl] object RelType extends RelationshipType {
    def name() = "CONNECTED"
  }

  private[impl] val FulltextIndexName = "FULLTEXT_ITEMS_INDEX"
  private[impl] val FulltextIndexParams = MapUtil.stringMap(
    IndexManager.PROVIDER, "lucene",
    "type", "fulltext",
    "to_lower_case", "true"
  )

  private[impl] val ExactIndexName = "EXACT_ITEMS_INDEX"
  private[impl] val ExactIndexParams = MapUtil.stringMap(
    "type", "exact",
    "to_lower_case", "true"
  )

  private[impl] val WeightPropertyName = "WEIGHT"

  private[impl] def getNodes(start: Node, direction: Direction, depth: Int): Map[Node, Double] = {

    val nodes = collection.mutable.Map[Node, Double]()

    if (depth == 1) {

      val relsIterator = start.getRelationships(RelType, direction).iterator()
      while (relsIterator.hasNext) {
        val rel = relsIterator.next()
        val n = rel.getOtherNode(start)
        val w = rel.getProperty(WeightPropertyName).asInstanceOf[Double]

        nodes += (n -> w)
      }

    } else {

      val traverserIterator = Traversal.description()
        .breadthFirst()
        .relationships(RelType, direction)
        .uniqueness(Uniqueness.NODE_PATH)
        .evaluator(Evaluators.excludeStartPosition())
        .evaluator(Evaluators.toDepth(depth))
        .traverse(start)
        .iterator()

      while (traverserIterator.hasNext) {
        val path = traverserIterator.next()

        var weight = 0.0
        var qualifier = 1

        val relIterator = path.relationships().iterator()
        while (relIterator.hasNext) {
          val rel = relIterator.next()
          val w = rel.getProperty(WeightPropertyName).asInstanceOf[Double]
          weight += w / qualifier
          qualifier <<= 1
        }

        val node = path.endNode()
        nodes get node match {
          case Some(x) => nodes(node) = x + weight
          case None => nodes(node) = weight
        }

      }
    }

    nodes.toMap
  }

  private[impl] def getIndirectNodes(node: Node, depth: Int): Map[Node,  Double] = getNodes(node, Direction.OUTGOING, depth)

  private[impl] def getRelation(a: Node, b: Node): Option[Relationship] = {

    val aIter = a.getRelationships(RelType).iterator()
    val bIter = b.getRelationships(RelType).iterator()

    while (aIter.hasNext && bIter.hasNext) {
      val aRel = aIter.next()
      if (aRel.getEndNode == b)
        return Some(aRel)

      val bRel = bIter.next()
      if (bRel.getEndNode == a)
        return Some(bRel)
    }

    None
  }

  implicit def iiToIiImpl(item: Ii): NeoIi = {
    if (item.isInstanceOf[NeoIi])
      item.asInstanceOf[NeoIi]
    else
      throw new IllegalArgumentException("Can't operate with this implementation %s" format item)
  }

}