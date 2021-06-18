package de.sciss.aleatorium

case class KeyFrame(name: String, pos: ArmPos, dur: Int = 2000) {
  override def toString: String = name
}
