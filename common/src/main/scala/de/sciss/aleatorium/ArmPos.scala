/*
 *  ArmPos.scala
 *  (Aleatorium)
 *
 *  Copyright (c) 2021 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Affero General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.aleatorium

object ArmPos {
  val Unknown: ArmPos = ArmPos(
    base     = 90,
    lowArm   = 90,
    highArm  = 90,
    ankle    = 90,
    gripRota = 90,
    gripOpen = 90,
  )
}
case class ArmPos(
  base    : Int,
  lowArm  : Int,
  highArm : Int,
  ankle   : Int,
  gripRota: Int,
  gripOpen: Int,
) {
  def seq: Seq[Int] = Seq(
    base, lowArm, highArm, ankle, gripRota, gripOpen
  )
}
