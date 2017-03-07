package com.mercury.service

import com.mercury.core.Site
import com.mercury.database.ProductionDatabase
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

trait SitesService extends ProductionDatabase {
  def getSiteById(id: UUID): Future[Option[Site]] = {
    database.sitesModel.getBySiteId(id)
  }

  def getSitesByGroup(group: String): Future[List[Site]] = {
    database.sitesByGroupModel.getByGroup(group)
  }

  def saveOrUpdate(site: Site): Future[ResultSet] = {
    for {
      byId <- database.sitesModel.store(site)
      byGroup <- database.sitesByGroupModel.store(site)
    } yield byGroup
  }

  def delete(site: Site): Future[ResultSet] = {
    for {
      byID <- database.sitesModel.deleteById(site.id)
      byGroup <- database.sitesByGroupModel.deleteByGroupAndId(site.group, site.id)
    } yield byGroup
  }
}

object SitesService extends SitesService with ProductionDatabase
