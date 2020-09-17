/*
 * Copyright (c) 2020 Alexandru Nedelcu.
 * All rights reserved.
 */

package org.excavator.boot.experiment.monix
// In order to evaluate tasks, we'll need a Scheduler
import java.util.Observable

import jdk.internal.org.jline.utils.ShutdownHooks.Task
import monix.execution.Scheduler.Implicits.global

// A Future type that is also Cancelable
import monix.execution.CancelableFuture

// Task is in monix.eval
import monix.eval.Task
import scala.util.{Success, Failure}

class ParallelProcessing {

  def theNaiveWay(): Unit ={
    val items = 0 until 1000

    val tasks = items.map(i => Task(i * 2))

    val aggregate = Task.parSequence(tasks).map(_.toList)

    aggregate.foreach(println)
  }

  def imposingAParallelismLimit():Unit = {
    val items = 0 until 1000
    val tasks = items.map(i => Task(i * 2))
    val batches = tasks.sliding(10, 10).map(b => Task.parSequence(b)).iterator.to(Iterator)

    val aggregate = Task.sequence(batches).map(_.flatten.toList)

    aggregate.foreach(println)
  }

  def batchedObservables(): Unit = {
    val source = Observable.range(0, 1000).bufferIntrospective(256)

    val batched = source.flatMap{items => 
      val tasks = items.map(i => Task(i * 2))
      val batches = tasks.sliding(10, 10).map(b => Task.parSequence(b)).iterator.to(Iterator)
      val aggregate = Task.sequence(batches).map(_.flatten.toIterator)

      Observable.fromIterator(aggregate)
    }

    batched.toListL.foreach(println)
  }

}
