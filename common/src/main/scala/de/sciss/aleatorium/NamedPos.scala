package de.sciss.aleatorium

case class NamedPos(name: String, pos: ArmPos) {
  override def toString: String = name
}
