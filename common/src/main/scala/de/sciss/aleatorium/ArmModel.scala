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

import de.sciss.model.impl.ModelImpl

object ArmModel {
  def apply(init: ArmPos): ArmModel = new Impl(init)

  private final class VarImpl(init: Int) extends Var[Int] with ModelImpl[Int] {
    private val sync = new AnyRef

    private var _value = init

    override def apply(): Int = sync.synchronized(_value)

    override def update(value: Int): Unit = {
      val change = sync.synchronized {
        (_value != value) && {
          _value = value
          true
        }
      }
      if (change) dispatch(_value)
    }
  }

  private final class Impl(pos0: ArmPos) extends ArmModel {
    override val base     : Var[Int] = new VarImpl(pos0.base    )
    override val lowArm   : Var[Int] = new VarImpl(pos0.lowArm  )
    override val highArm  : Var[Int] = new VarImpl(pos0.highArm )
    override val ankle    : Var[Int] = new VarImpl(pos0.ankle   )
    override val gripRota : Var[Int] = new VarImpl(pos0.gripRota)
    override val gripOpen : Var[Int] = new VarImpl(pos0.gripOpen)

    def variables: Seq[Var[Int]] = Seq(
      base,
      lowArm,
      highArm,
      ankle,
      gripRota,
      gripOpen,
    )

    override def current: ArmPos = ArmPos(
      base      = base(),
      lowArm    = lowArm(),
      highArm   = highArm(),
      ankle     = ankle(),
      gripRota  = gripRota(),
      gripOpen  = gripOpen(),
    )
  }
}
trait ArmModel {
  def current: ArmPos

  def variables: Seq[Var[Int]]

  def base    : Var[Int]
  def lowArm  : Var[Int]
  def highArm : Var[Int]
  def ankle   : Var[Int]
  def gripRota: Var[Int]
  def gripOpen: Var[Int]


}
