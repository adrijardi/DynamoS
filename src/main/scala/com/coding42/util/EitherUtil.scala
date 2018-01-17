package com.coding42.util

import com.coding42.dynamos.DynamosParsingError

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

object EitherUtil {

  def sequence[L, R, M[X] <: TraversableOnce[X]](seq: M[Either[L, R]])
                                                (implicit cbf: CanBuildFrom[Nothing, R, M[R]]): Either[L, M[R]] = {
    Right {
      seq.foldRight(List.empty[R]) { case (a, res) =>
        a match {
          case Left(l) => return Left[L, M[R]](l)
          case Right(r) => r :: res
        }
      }
        .to[M]
    }
  }

  def map[L, K, R](seq: Map[K, Either[L, R]], leftCombine: (K, L) => L): Either[L, Map[K, R]] = {
    Right {
      seq.foldLeft(Map.empty[K, R]) { case (res, a) =>
        a match {
          case (k, Left(l)) => return Left[L, Map[K, R]](leftCombine(k, l))
          case (k, Right(r)) => res + (k -> r)
        }
      }
    }
  }

  def map[K, R](seq: Map[K, Either[DynamosParsingError, R]]): Either[DynamosParsingError, Map[K, R]] = {
    map[DynamosParsingError, K, R](seq, (k, l) => DynamosParsingError(s"$k -> ${l.field}"))
  }

}
