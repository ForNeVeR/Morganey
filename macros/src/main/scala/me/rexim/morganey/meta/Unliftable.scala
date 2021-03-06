package me.rexim.morganey.meta

import me.rexim.morganey.ast.LambdaTerm
import me.rexim.morganey.church.ChurchNumberConverter.{decodeNumber, decodeChar}
import me.rexim.morganey.church.ChurchPairConverter.{decodeList, decodePair}
import me.rexim.morganey.util.sequence

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

trait Unliftable[T] {
  def unapply(t: LambdaTerm): Option[T]
}

trait DefaultUnliftableInstances {

  implicit val unliftInt = Unliftable[Int](decodeNumber)
  implicit val unliftChar = Unliftable[Char](decodeChar)

  implicit val unliftString = Unliftable[String] { s =>
    val unlift = implicitly[Unliftable[Seq[Char]]]
    unlift.unapply(s).map(_.mkString)
  }

  implicit def unliftPair[A, B](implicit una: Unliftable[A], unb: Unliftable[B]): Unliftable[(A, B)] =
    Unliftable[(A, B)] { t =>
      for {
        (x, y) <- decodePair(t)
        a      <- una.unapply(x)
        b      <- unb.unapply(y)
      } yield (a, b)
    }

  implicit def unliftColl[X, CC[X] <: Traversable[X], A]
                         (implicit unlift: Unliftable[A], cbf: CanBuildFrom[List[A], A, CC[A]]): Unliftable[CC[A]] =
    Unliftable[CC[A]] { t =>
      decodeList(t).flatMap { xs =>
        val decodeResults = xs.map(unlift.unapply)

        sequence(decodeResults) map { xs =>
          val bl = cbf()
          xs.foreach(bl += _)
          bl.result()
        }
      }
    }

  implicit val unliftId = Unliftable[LambdaTerm](Some(_))

}

object Unliftable {

  def apply[T](f: LambdaTerm => Option[T]): Unliftable[T] =
    new Unliftable[T] {
      def unapply(t: LambdaTerm): Option[T] = f(t)
    }

  def using[T](f: PartialFunction[LambdaTerm, T]): Unliftable[T] =
    new Unliftable[T] {
      def unapply(t: LambdaTerm): Option[T] = f.lift(t)
    }

}
