package com.themillhousegroup.reactivemongo.mocks

// Reactive Mongo imports

import reactivemongo.api.CollectionProducer
import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.api.DefaultDB
import reactivemongo.api.FailoverStrategy
import com.themillhousegroup.reactivemongo.mocks.facets._
import org.specs2.specification.Scope
import reactivemongo.play.json.collection.JSONCollection

//// Reactive Mongo plugin
import reactivemongo.play.json._

import org.specs2.mock.Mockito
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.{ ExecutionContext, Future }

trait MongoMocks extends Mockito with Logging
    with CollectionFind
    with CollectionInsert
    with CollectionRemove
    with CollectionSave
    with CollectionUpdate {
  this: org.specs2.mutable.Specification =>

  lazy val mockDatabaseName = "mockDB"

  val mockDB = mock[DefaultDB]
  mockDB.name answers { _ =>
    logger.debug(s"Returning mocked $mockDatabaseName DB")
    mockDatabaseName
  }

  /** Returns a mocked JSONCollection that can be used with the givenMongo... methods in MongoMocks */
  def mockedCollection(name: String)(implicit mockDB: DefaultDB): JSONCollection = {
    val mockCollection = mock[JSONCollection]
    mockDB
      .collection[JSONCollection](
        org.mockito.Matchers.eq(name),
        any[FailoverStrategy])(
          any[CollectionProducer[JSONCollection]]) answers { _ =>
            logger.debug(s"Returning mocked $name collection")
            mockCollection
          }

    // Add some sensible responses to standard methods:
    mockCollection.name returns s"$name (mock)"
    mockCollection.fullCollectionName returns s"mock.${name}"
    mockCollection.db returns mockDB

    mockCollection
  }

  def mockReactiveMongoApi = {
    val rma = mock[ReactiveMongoApi]
    rma.db returns mockDB
  }

  /**
   * A specs2 Scope that contains and configures an
   * independent ReactiveMongoApi instance, guaranteeing
   * no possible interaction with other mocked instances
   */
  class MongoMockScope extends Scope {
    val reactiveMongoApi = mock[ReactiveMongoApi]
    implicit val scopedMockDB = mock[DefaultDB]
    reactiveMongoApi.db returns scopedMockDB
  }
}
