package hypedex.queryAnalyzer.models

case class LessThan(override val value: Double) extends LogicalExpression (value, false, -1) {
  override def isWithinRange(otherValue: Double): Boolean = this.value > otherValue
}
