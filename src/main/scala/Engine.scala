/*package org.template.regression

import org.apache.predictionio.controller.{EmptyEvaluationInfo, Engine, EngineFactory}

case class Query(vector: Array[Double])
case class PredictedResult(
  prediction: Double
)
case class ActualResult(label: Double)

object RegressionEngine extends EngineFactory {
  type Type = Engine[TrainingData, EmptyEvaluationInfo, PreparedData, Query, PredictedResult, ActualResult]

  def apply(): Type = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map(
        //"sgd" -> classOf[LinearRegressionWithSGD],
        "tree" -> classOf[DecisionTreeRegression]
        //"iso" -> classOf[IsotonicRegressionAlgorithm],
        //"ridge" -> classOf[RidgeRegression],
        //"lasso" -> classOf[LassoRegression]
      ),
      classOf[Serving]
    )
  }
}
*/
package org.apache.predictionio.examples.classification

import org.apache.predictionio.controller.EngineFactory
import org.apache.predictionio.controller.Engine

case class Query(
  attr0 : Double,
  attr1 : Double,
  attr2 : Double
)

case class PredictedResult(
  label: Double
)

case class ActualResult(
  label: Double
)

object ClassificationEngine extends EngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("naive" -> classOf[NaiveBayesAlgorithm],
        "randomforest" -> classOf[RandomForestAlgorithm]), // ADDED
      classOf[Serving])
  }
}
