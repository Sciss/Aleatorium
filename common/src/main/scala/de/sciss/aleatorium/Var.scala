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

trait Var[A] extends Model[A] {
  def apply(): A

  def update(value: A): Unit

//  def lineTo(value: A, duration: Int): Unit
}
