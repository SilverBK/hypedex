package hypedex

import java.util.UUID

import hypedex.models.payloads.HypedexPayload
import hypedex.models.{Metadata, TreeNode}
import hypedex.partitionConstructor.{CalculationWrapper, KDNode, PartitionNode}
import hypedex.services.KDTreeBuilder
import hypedex.storage.{PartitionStore, MetadataRepository}
import org.apache.spark.sql.{Encoder, Row, SparkSession}

import scala.collection.mutable

class DataCommandService[T <: HypedexPayload](
  session: SparkSession,
  partitionRepository: PartitionStore[T],
  metadataStore: MetadataRepository[T, Metadata[T]],
  kdTreeBuilder: KDTreeBuilder[T],
  mapper: Row => T
) {
  def createPartitions(
    originalDataDir: String,
    originalFilePattern: String,
    targetDataDir: String,
    distanceFunction: (T, T) => Double,
    depth: Int =  3
  )(implicit enc: Encoder[T], calcWrapperEncoder: Encoder[CalculationWrapper]): Metadata[T] = {
    val ds = session.read.option("header", "true")
      .parquet(s"${originalDataDir}/${originalFilePattern}")
      .map(mapper)

    ds.persist()
    val kdTree = kdTreeBuilder.buildTree(ds, depth)(calcWrapperEncoder)
    persistParquets(kdTree, targetDataDir)
    ds.unpersist()
    val metadata = Metadata(UUID.randomUUID().toString, distanceFunction, kdTree, targetDataDir)
    this.metadataStore.save(metadata)

    metadata
  }

  def persistParquets(kdTree: TreeNode, targetDir: String) = {
    val queue = mutable.Queue(kdTree)

    while(queue.isEmpty == false) {
      val currentNode = queue.dequeue()

      currentNode match {
        case partition: PartitionNode[T] => this.partitionRepository.save(partition, targetDir)
        case node: KDNode[T] => queue.enqueue(node.left, node.right)
      }
    }
  }

}
