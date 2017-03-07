package com.mercury.model

import java.util.UUID

import com.mercury.core.Site
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

class SitesModel extends CassandraTable[ConcreteSitesModel, Site] {
  override def tableName: String = "sites"

  object id extends TimeUUIDColumn(this) with PartitionKey {
    override lazy val name = "site_id"
  }

  object title extends StringColumn(this)

  object group extends StringColumn(this)

  object url extends StringColumn(this)

  override def fromRow(r: Row): Site = Site(id(r), group(r), url(r), group(r))
}

abstract class ConcreteSitesModel extends SitesModel with RootConnector {
  def getBySiteId(id: UUID): Future[Option[Site]] = {
    select
      .where(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

  def store(site: Site): Future[ResultSet] = {
    insert
      .value(_.id, site.id)
      .value(_.title, site.title)
      .value(_.group, site.group)
      .value(_.url, site.url)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

  def deleteById(id: UUID): Future[ResultSet] = {
    delete
      .where(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }
}

class SitesByGroupModel extends CassandraTable[SitesByGroupModel, Site] {
  override def tableName: String = "sites_by_group"

  object group extends StringColumn(this) with PartitionKey

  object id extends TimeUUIDColumn(this) with ClusteringOrder {
    override lazy val name = "song_id"
  }

  object title extends StringColumn(this)

  object url extends StringColumn(this)

  override def fromRow(r: Row): Site = Site(id(r), title(r), url(r), group(r))
}

abstract class ConcreteSitesByGroupModel extends SitesByGroupModel with RootConnector {
  def getByGroup(group: String): Future[List[Site]] = {
    select
      .where(_.group eqs group)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .fetch()
  }

  def store(site: Site): Future[ResultSet] = {
    insert
      .value(_.id, site.id)
      .value(_.title, site.title)
      .value(_.url, site.url)
      .value(_.group, site.group)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

  def deleteByGroupAndId(group: String, id: UUID): Future[ResultSet] = {
    delete
      .where(_.group eqs group)
      .and(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }
}
