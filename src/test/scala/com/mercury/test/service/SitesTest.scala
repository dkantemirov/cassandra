package com.mercury.test.service

import com.mercury.core.{Connector, Site}
import com.datastax.driver.core.utils.UUIDs
import com.mercury.database.EmbeddedDatabase
import com.outworkers.phantom.dsl.ResultSet
import com.websudos.util.testing.{Sample, _}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class SitesTest extends FlatSpec
  with Matchers
  with Inspectors
  with ScalaFutures
  with OptionValues
  with BeforeAndAfterAll
  with EmbeddedDatabase
  with Connector.testConnector.Connector {

  override def beforeAll(): Unit = {
    database.create(5.seconds)
  }

  implicit object SiteGenerator extends Sample[Site] {
    override def sample: Site =
      Site(UUIDs.timeBased(), gen[ShortString].value, "Online store", "http://ebay.com")
  }

  "A Site" should "be inserted into cassandra" in {
    val sample = gen[Site]
    val future = this.store(sample)

    whenReady(future) { result =>
      result isExhausted() shouldBe true
      result wasApplied() shouldBe true
      this.drop(sample)
    }
  }

  it should "find a site by id" in {
    val sample = gen[Site]

    val chain = for {
      store <- this.store(sample)
      get <- database.sitesModel.getBySiteId(sample.id)
      delete <- this.drop(sample)
    } yield get

    whenReady(chain) { res =>
      res shouldBe defined
      this.drop(sample)
    }
  }

  it should "find sites by group" in {
    val sample = gen[Site]
    val sample2 = gen[Site]
    val sample3 = gen[Site]

    val future = for {
      f1 <- this.store(sample.copy(title = "Store a"))
      f2 <- this.store(sample2.copy(title = "Store b"))
      f3 <- this.store(sample3.copy(title = "Store c"))
    } yield (f1, f2, f3)

    whenReady(future) { insert =>
      val songsByGroup = database.sitesByGroupModel.getByGroup("Online store")
      whenReady(songsByGroup) { searchResult =>
        searchResult shouldBe a[List[_]]
        searchResult should have length 3
        this.drop(sample)
        this.drop(sample2)
        this.drop(sample3)
      }
    }
  }

  it should "be updated into cassandra" in {
    val sample =  Site(
      UUIDs.timeBased(),
      gen[ShortString].value,
      "Offline store",
      "http://dixy.com"
    )
    val updatedURL = gen[String]

    val chain = for {
      _ <- this.store(sample)
      _ <- this.store(sample.copy(url = updatedURL))
      modified <- database.sitesModel.getBySiteId(sample.id)
    } yield modified

    whenReady(chain) {
      modified =>
        modified shouldBe defined
        this.drop(modified.get)
    }
  }

  private def store(site: Site): Future[ResultSet] = {
    for {
      byId <- database.sitesModel.store(site)
      byGroup <- database.sitesByGroupModel.store(site)
    } yield byGroup
  }

  private def drop(site: Site) = {
    for {
      byID <- database.sitesModel.deleteById(site.id)
      byGroup <- database.sitesByGroupModel.deleteByGroupAndId(site.group, site.id)
    } yield byGroup
  }
}

