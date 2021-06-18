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

trait Expr[A] extends Model[A] {
  def apply(): A
}

trait Var[A] extends Expr[A] {
  def update(value: A): Unit
}

trait LineVar[A] extends Var[A] {
  def lineTo(value: A, duration: Int): Unit
}
