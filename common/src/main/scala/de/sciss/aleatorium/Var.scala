/*
 *  Var.scala
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

import de.sciss.model.Model
import de.sciss.model.impl.ModelImpl

trait Expr[A] extends Model[A] {
  def apply(): A
}

object Var {
  def apply[A](init: A): Var[A] = new VarImpl(init)

  private final class VarImpl[A](init: A) extends Var[A] with ModelImpl[A] {
    private var _value = init

    private val sync = new AnyRef

    override def apply(): A = sync.synchronized(_value)

    override def update(value: A): Unit = {
      val change = sync.synchronized {
        (_value != value) && {
          _value = value
          true
        }
      }
      if (change) dispatch(value)
    }
  }
}
trait Var[A] extends Expr[A] {
  def update(value: A): Unit
}

trait LineVar[A] extends Var[A] {
  def lineTo(value: A, duration: Int): Unit
}
