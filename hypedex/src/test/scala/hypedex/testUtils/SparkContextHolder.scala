package hypedex.testUtils

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object SparkContextHolder {
  val conf: SparkConf = new SparkConf()
    .setAppName("HypedexTests")
    .setMaster("local[12]")
      //.set("spark.sql.files.maxPartitionBytes", "20000000")
    .set("spark.driver.maxResultSize", "3g")
    .set("spark.executor.memory", "10g")
    .set("spark.driver.memory", "5g")

  val sparkContext = new SparkContext(conf)

  def getSession(): SparkSession = SparkSession.builder().config(conf).getOrCreate()
}
