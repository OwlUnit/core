package com.owlunit.core.ii.mutable.utils

import java.util.UUID
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Actor, Props, ActorSystem}
import org.specs2.mutable.After
import com.owlunit.core.ii.mutable.{Ii, IiDao}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait IiHelpers {

  def dao: IiDao

  def getRandomIi: Ii = dao.load(dao.create.save.id)
  def createIi(name: String): Ii = dao.load(dao.create.setMeta("name", name).save.id)

  def randomString = UUID.randomUUID().toString
  def randomKeyValue: (String, String) = ("key-%s" format randomString, "value-%s" format randomString)

}