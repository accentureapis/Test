package org.template.regression

import grizzled.slf4j.Logger
import org.apache.predictionio.controller.{EmptyEvaluationInfo, PDataSource, Params, EmptyParams}
import org.apache.predictionio.data.storage.PropertyMap
import org.apache.predictionio.data.store.PEventStore
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.json4s._

case class DataSourceParams() extends Params

class DataSource(ep: EmptyParams)
  extends PDataSource[TrainingData, EmptyEvaluationInfo, Query, ActualResult] {

  @transient lazy val logger: Logger = Logger[this.type]

  override def readTraining(sc: SparkContext): TrainingData = {
    TrainingData(selectEvents(sc))
  }

  override def readEval(sc: SparkContext): Seq[(TrainingData, EmptyEvaluationInfo, RDD[(Query, ActualResult)])] = {
    val events = selectEvents(sc)
    val rdd = events.map {
      case (_, properties) =>
        Query(properties.get[Array[Double]]("vector")) -> ActualResult(properties.get[Double]("label"))
    }
    val eval = (TrainingData(events), new EmptyEvaluationInfo(), rdd)
    Seq(eval)
  }

  def selectEvents(sc: SparkContext): RDD[(String, PropertyMap)] = {
    val grades = PEventStore.aggregateProperties(
      appName = sys.env("PIO_EVENTSERVER_APP_NAME"),
      entityType = "cokevendor",
      required = Some(List("vendor","item","day","month","quantity"))
    )(sc)

    val events = grades.map {
      case (entityId, properties) =>
        val fields = Map(
          "vector" -> JArray(List(
             JDouble(properties.get[Double]("vendor")),JDouble(properties.get[Double]("item")),JDouble(properties.get[Double]("month")),JDouble(properties.get[Double]("day"))
           )),
		   "label" -> JDouble(properties.get[Double]("quantity"))
        )

        val propertyMap = PropertyMap(fields, properties.firstUpdated, properties.lastUpdated)

        entityId -> propertyMap
    }

    events.cache
  }
}

case class TrainingData(values: RDD[(String, PropertyMap)]) {
  override def toString: String = {
    s"values: [${values.count()}] (${values.take(2).toList}...)"
  }
}
