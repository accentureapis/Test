package org.template.regression

import grizzled.slf4j.Logger
import org.apache.predictionio.controller.P2LAlgorithm
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint}
import org.apache.spark.rdd.RDD
import org.apache.predictionio.controller.Params
import org.apache.predictionio.data.storage.PropertyMap

import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils
import play.api.libs.json.Json

case class DecisionTreeParams(impurity: String, maxDepth: Int, maxBins: Int) extends Params

val inputJson=
"""
{
  "eventId": "e85395d3d0974a89aa9bdf898c9d62de",
  "event": "$set",
  "entityType": "cokevendor",
  "entityId": 1,
  "properties": {
    "quantity": 90,
    "vendor": "5",
    "day": 3,
    "month": 5,
    "item": "95"
  },
  "eventTime": "2017-12-04T06:00:35.700Z",
  "creationTime": "2017-12-04T06:00:35.700Z"
}
"""
val jsonValue = scala.util.parsing.json.JSON.parseFull(inputJson)

jsonValue match{
  case Some(m: Map[String, Any]) => m("vendor") match {
    case s: String => s
  }
  case Some(p: Map[String, Any]) => p("item") match {
    case s1: String => s1
  }
}
println(s)

class DecisionTreeRegression(val ap: DecisionTreeParams)
  extends P2LAlgorithm[PreparedData, DecisionTreeModel, Query, PredictedResult] {

  @transient lazy val logger: Logger = Logger[this.type]

  override def train(sc: SparkContext, data: PreparedData): DecisionTreeModel = {
    def toLabelPoint(item: (String, PropertyMap)): LabeledPoint = item match {
      case (_, properties) =>
        val label = properties.get[Double]("label")
        val vectors = Vectors.dense(properties.get[Array[Double]]("vector"))
        LabeledPoint(label, vectors)
    }
    val labeledPoints: RDD[LabeledPoint] = data.values.map(toLabelPoint).cache
    val categoricalFeaturesInfo = Map[Int, Int]()
    DecisionTree.trainRegressor(labeledPoints, categoricalFeaturesInfo, ap.impurity, ap.maxDepth, ap.maxBins)
  }

  override def predict(model: DecisionTreeModel, query: Query): PredictedResult = {
    val features = Vectors.dense(query.vector)
    val prediction = model.predict(features)
    PredictedResult(prediction)
  }
}
