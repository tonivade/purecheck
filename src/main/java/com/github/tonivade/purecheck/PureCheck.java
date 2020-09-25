/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purecheck;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.data.NonEmptyList;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.data.SequenceOf;
import com.github.tonivade.purefun.data.Sequence_;
import com.github.tonivade.purefun.instances.SequenceInstances;
import com.github.tonivade.purefun.typeclasses.Parallel;

public abstract class PureCheck<F extends Witness, E> {
  
  private final Parallel<F, F> parallel;
  private final String name;
  private final NonEmptyList<TestSuite<F, E>> suites;
  
  public PureCheck(Parallel<F, F> parallel, String name, NonEmptyList<TestSuite<F, E>> suites) {
    this.parallel = checkNonNull(parallel);
    this.name = checkNonEmpty(name);
    this.suites = checkNonNull(suites);
  }
  
  public Kind<F, Report<E>> runK() {
    Kind<F, Kind<Sequence_, TestSuite.Report<E>>> sequence = 
        parallel.parSequence(SequenceInstances.traverse(), suites.map(TestSuite::runK));
    
    Kind<F, Sequence<TestSuite.Report<E>>> results = parallel.monad().map(sequence, SequenceOf::narrowK);
    
    return parallel.monad().map(results, xs -> new PureCheck.Report<>(name, xs));
  }
  
  public abstract Report<E> run();

  public abstract Future<Report<E>> parRun(Executor executor);

  public Future<Report<E>> parRun() {
    return parRun(Future.DEFAULT_EXECUTOR);
  }

  public static class Report<E> {

    private final String name;
    private final Sequence<TestSuite.Report<E>> reports;

    public Report(String name, Sequence<TestSuite.Report<E>> reports) {
      this.name = checkNonEmpty(name);
      this.reports = checkNonNull(reports);
    }

    public void assertion() {
      try {
        reports.forEach(TestSuite.Report::assertion);
      } finally {
        System.out.println(this);
      }
    }

    @Override
    public String toString() {
      return reports.join("\n\n", "# " + name + "\n\n", "\n");
    }
  }
}
