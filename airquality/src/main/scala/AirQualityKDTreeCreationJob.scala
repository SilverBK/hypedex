import hypedex.DataCommandService
import hypedex.models.Metadata
import hypedex.services.KDTreeBuilder
import hypedex.storage.{BasicMetadataRepository, ParquetPartitionStore}
import hypedex.testUtils.AirQuality
import org.apache.spark.sql.SparkSession

object AirQualityKDTreeCreationJob {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .master("local[8]")
      .config("spark.driver.memory", "5g")
      .config("spark.executor.memory", "5g")
      .appName("Spark AirQuality")
      .getOrCreate()

    import spark.implicits._

    val baseDir = "D:\\source\\datasets\\air-tests"
    val targetDir = "D:\\source\\datasets\\air-tests\\kd-tree-four-level"
    val metadataDir = s"D:\\source\\datasets\\air-tests\\metadata-four-level"

    val partitionStore = ParquetPartitionStore[AirQuality](spark)
    val metadataStore = new BasicMetadataRepository[AirQuality, Metadata[AirQuality]](metadataDir)

    val depth = 5

    val dimensArray = Array("P1", "P2")
    val treeBuilder = new KDTreeBuilder[AirQuality](spark.sqlContext, dimensArray, 0.10)
    val dataManipulationService = new DataCommandService[AirQuality](spark, partitionStore,
      metadataStore, treeBuilder, AirQuality.namedMappingFunction)

    dataManipulationService.createPartitions(
      baseDir,
      "all-parquet",
      targetDir,
      null,
      depth
    )
  }
}