/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
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

package org.sosy_lab.java_smt.domain_optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.FunctionDeclarationKind;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.QuantifiedFormulaManager.Quantifier;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaTransformationVisitor;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

public class DomainOptimizerDecider {

  private final DomainOptimizer opt;
  private final DomainOptimizerSolverContext delegate;
  private final ProverEnvironment wrapped;

  public DomainOptimizerDecider(DomainOptimizer pOpt, DomainOptimizerSolverContext pDelegate) {
    opt = pOpt;
    delegate = pDelegate;
    this.wrapped = opt.getWrapped();
  }

  public Formula performSubstitutions(Formula pFormula) {
    FormulaManager fmgr = delegate.getFormulaManager();
    List<Map<Formula, Formula>> substitutions = new ArrayList<>();
    FormulaVisitor<TraversalProcess> replacer =
        new FormulaVisitor<>() {
          final FunctionDeclarationKind[] dec = new FunctionDeclarationKind[1];
          @Override
          public TraversalProcess visitFreeVariable(Formula f, String name) {
            IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
            Interval domain = opt.getInterval(f);
            IntegerFormula substitute = (IntegerFormula) f;
            switch (dec[0]) {
              case GTE:
                if (domain.isUpperBoundSet()) {
                  substitute = imgr.makeNumber(domain.getUpperBound());
                }
                break;
              case GT:
                if (domain.isUpperBoundSet()) {
                  substitute = imgr.makeNumber(domain.getUpperBound() - 1);
                }
                break;
              case LTE:
                if (domain.isLowerBoundSet()) {
                  substitute = imgr.makeNumber(domain.getLowerBound());
                }
                break;
              case LT:
                if (domain.isLowerBoundSet()) {
                  substitute = imgr.makeNumber(domain.getLowerBound() + 1);
                }
                break;
              default:
                throw new IllegalStateException("Unexpected value: " + dec[0]);
            }
            Map<Formula, Formula> substitution = new HashMap<>();
            substitution.put(f, substitute);
            substitutions.add(substitution);
            return TraversalProcess.CONTINUE;
          }
          @Override
          public TraversalProcess visitBoundVariable(Formula f, int deBruijnIdx) {
            return null;
          }

          @Override
          public TraversalProcess visitConstant(Formula f, Object value) {
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitFunction(
              Formula f, List<Formula> args, FunctionDeclaration<?> functionDeclaration) {
            FunctionDeclarationKind declaration = functionDeclaration.getKind();
            if (declaration == FunctionDeclarationKind.LTE
                || declaration == FunctionDeclarationKind.LT
                || declaration == FunctionDeclarationKind.GTE
                || declaration == FunctionDeclarationKind.GT) {
              dec[0] = declaration;
            }
            return TraversalProcess.CONTINUE;
          }

            @Override
            public TraversalProcess visitQuantifier(
                BooleanFormula f,
                Quantifier quantifier,
                List<Formula> boundVariables,
                BooleanFormula body) {
            return TraversalProcess.CONTINUE;
            }
            };
    fmgr.visitRecursively(pFormula, replacer);
    for (int i = 0; i < substitutions.size() - 1; i++) {
      Map<Formula, Formula> substitution = substitutions.get(i);
      pFormula = fmgr.substitute(pFormula, substitution);
    }
    return pFormula;
  }


  public BooleanFormula pruneTree(Formula pFormula) throws InterruptedException, SolverException {
    pFormula = performSubstitutions(pFormula);
    FormulaManager fmgr = delegate.getFormulaManager();
    ProverEnvironment wrapped = this.wrapped;
    BooleanFormulaManager bmgr = fmgr.getBooleanFormulaManager();
    List<BooleanFormula> operands = new ArrayList<>();
    List<Map<Formula, Formula>> substitutions = new ArrayList<>();
    BooleanFormulaTransformationVisitor visitor = new BooleanFormulaTransformationVisitor(fmgr) {
      @Override
      public BooleanFormula visitAnd(List<BooleanFormula> processedOperands) {
        operands.addAll(processedOperands);
          return super.visitAnd(processedOperands);
      }
      @Override
      public BooleanFormula visitOr(List<BooleanFormula> processedOperands) {
        operands.addAll(processedOperands);
        return super.visitOr(processedOperands);
      }
    };
    bmgr.visit((BooleanFormula) pFormula, visitor);
    for (BooleanFormula toProcess : operands) {
      wrapped.push();
      wrapped.addConstraint(toProcess);
      if (wrapped.isUnsat()) {
          Map<Formula, Formula> substitution = new HashMap<>();
          substitution.put(toProcess, bmgr.makeFalse());
          substitutions.add(substitution);
      }
      wrapped.pop();
    }
    for (Map<Formula, Formula> substitution : substitutions) {
      pFormula = fmgr.substitute(pFormula, substitution);
    }

    return (BooleanFormula) pFormula;
  }


}