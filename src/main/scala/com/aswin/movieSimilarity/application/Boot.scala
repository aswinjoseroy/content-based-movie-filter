package com.aswin.movieSimilarity.application

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer, VectorAssembler}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by aswin on 18/10/16.
  */
object Boot {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("MovieSimilarity")
    val sc: SparkContext = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("../Downloads/movies.csv")

    val movie = tfIdf("movie","1")
    val actor = tfIdf("actor","2")

    val assembler = new VectorAssembler()
      .setInputCols(Array("feature1", "feature2"))
      .setOutputCol("features")

    val pipeline = new Pipeline()().setStages(Array(movie._1,movie._2,movie._3,actor._1,actor._2,actor._3,assembler))

    val model = pipeline.fit(df)
    val predictions = model.transform(df)

    val vectorRdd = predictions.map(x => x.getAs[Vector]("features"))

    val mat = new RowMatrix(vectorRdd)

    //changes to be made
    val simsPerfect = mat.columnSimilarities()
    val simsEstimate = mat.columnSimilarities(0.8)

    println("Pairwise similarities are: " + simsPerfect.entries.collect.mkString(", "))

    println("Estimated pairwise similarities are: " +     simsEstimate.entries.collect.mkString(", "))

  }

  def tfIdf(col: String, featureNumber:String): (Tokenizer, HashingTF, IDF) = {
    val tokenizer = new Tokenizer().setInputCol(col).setOutputCol(col + "_tokenized")
    val hash = new HashingTF().setNumFeatures(1000).setInputCol(tokenizer.getOutputCol)
      .setOutputCol(col + "_hashed")
    val idf = new IDF().setInputCol(hash.getOutputCol).setOutputCol("feature"+featureNumber.toString)
    (tokenizer, hash, idf)
  }
}
