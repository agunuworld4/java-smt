// This file is part of JavaSMT,
// an API wrapper for a collection of SMT solvers:
// https://github.com/sosy-lab/java-smt
//
// SPDX-FileCopyrightText: 2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.java_smt.api;

/** Possible floating point rounding modes. */
public enum FloatingPointRoundingMode {
  NEAREST_TIES_TO_EVEN,
  NEAREST_TIES_AWAY,
  TOWARD_POSITIVE,
  TOWARD_NEGATIVE,
  TOWARD_ZERO
}
