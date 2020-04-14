/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2020  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.sosy_lab.java_smt.delegate.synchronize;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.InterpolatingProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverException;

class SynchronizedInterpolatingProverEnvironmentWithContext<T>
    extends SynchronizedBasicProverEnvironmentWithContext<T>
    implements InterpolatingProverEnvironment<T> {

  private final InterpolatingProverEnvironment<T> delegate;

  SynchronizedInterpolatingProverEnvironmentWithContext(
      InterpolatingProverEnvironment<T> pDelegate,
      SolverContext pSync,
      FormulaManager pManager,
      FormulaManager pOtherManager) {
    super(pDelegate, pSync, pManager, pOtherManager);
    delegate = checkNotNull(pDelegate);
  }

  @Override
  public BooleanFormula getInterpolant(Collection<T> pFormulasOfA)
      throws SolverException, InterruptedException {
    return manager.translateFrom(delegate.getInterpolant(pFormulasOfA), otherManager);
  }

  @Override
  public List<BooleanFormula> getSeqInterpolants(List<? extends Collection<T>> pPartitionedFormulas)
      throws SolverException, InterruptedException {
    return translate(delegate.getSeqInterpolants(pPartitionedFormulas), otherManager, manager);
  }

  @Override
  public List<BooleanFormula> getTreeInterpolants(
      List<? extends Collection<T>> pPartitionedFormulas, int[] pStartOfSubTree)
      throws SolverException, InterruptedException {
    return translate(
        delegate.getTreeInterpolants(pPartitionedFormulas, pStartOfSubTree), otherManager, manager);
  }
}
