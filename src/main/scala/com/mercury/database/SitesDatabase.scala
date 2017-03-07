package com.mercury.database

import com.mercury.core.Connector._
import com.mercury.model.{ConcreteSitesByGroupModel, ConcreteSitesModel}
import com.outworkers.phantom.dsl._

class SitesDatabase(override val connector: KeySpaceDef) extends Database[SitesDatabase](connector) {
  object sitesModel extends ConcreteSitesModel with connector.Connector
  object sitesByGroupModel extends ConcreteSitesByGroupModel with connector.Connector
}

object ProductionDb extends SitesDatabase(connector)

trait ProductionDatabaseProvider {
  def database: SitesDatabase
}

trait ProductionDatabase extends ProductionDatabaseProvider {
  override val database = ProductionDb
}

object EmbeddedDb extends SitesDatabase(testConnector)

trait EmbeddedDatabaseProvider {
  def database: SitesDatabase
}

trait EmbeddedDatabase extends EmbeddedDatabaseProvider {
  override val database = EmbeddedDb
}

