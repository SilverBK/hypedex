package hypedex.storage

import hypedex.models.Metadata
import hypedex.models.payloads.HypedexPayload

/**
  * Responsible for storing and retrieving the metadata
  * @tparam T Metadata or its subclass
  */
trait MetadataRepository[T <: Metadata] {
  def save(newMetadata: T): String
  def getMetadataById(id: String): T
  def deleteMetadataById(id: String): Boolean
}