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

import java.util.Optional;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.OptimizationProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverException;

class SynchronizedOptimizationProverEnvironment extends SynchronizedBasicProverEnvironment<Void>
    implements OptimizationProverEnvironment {

  private final OptimizationProverEnvironment delegate;

  SynchronizedOptimizationProverEnvironment(
      OptimizationProverEnvironment pDelegate, SolverContext pSync) {
    super(pDelegate, pSync);
    delegate = pDelegate;
  }

  @Override
  public int maximize(Formula pObjective) {
    synchronized (sync) {
      return delegate.maximize(pObjective);
    }
  }

  @Override
  public int minimize(Formula pObjective) {
    synchronized (sync) {
      return delegate.minimize(pObjective);
    }
  }

  @Override
  public OptStatus check() throws InterruptedException, SolverException {
    synchronized (sync) {
      return delegate.check();
    }
  }

  @Override
  public Optional<Rational> upper(int pHandle, Rational pEpsilon) {
    synchronized (sync) {
      return delegate.upper(pHandle, pEpsilon);
    }
  }

  @Override
  public Optional<Rational> lower(int pHandle, Rational pEpsilon) {
    synchronized (sync) {
      return delegate.lower(pHandle, pEpsilon);
    }
  }
}