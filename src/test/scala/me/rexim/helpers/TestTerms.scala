package me.rexim.helpers

import me.rexim.{LambdaFunc, LambdaVar}

trait TestTerms {
  val x = LambdaVar("x")
  val y = LambdaVar("y")
  val z = LambdaVar("z")

  def alphaVar(prefix: String, number: Int) =
    LambdaVar(s"$prefix##$number")

  def I(v : LambdaVar) = LambdaFunc(v, v)
}
