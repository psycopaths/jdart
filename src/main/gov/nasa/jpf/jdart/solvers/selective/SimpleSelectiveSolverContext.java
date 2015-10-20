/*
 * Copyright (C) 2015, United States Government, as represented by the 
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The PSYCO: A Predicate-based Symbolic Compositional Reasoning environment 
 * platform is licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0. 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 */
package gov.nasa.jpf.jdart.solvers.selective;

import gov.nasa.jpf.constraints.api.ConstraintSolver.Result;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.SolverContext;
import gov.nasa.jpf.constraints.api.Valuation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SimpleSelectiveSolverContext extends SolverContext {

  private static class StackElement {    
    final ArrayList<Expression<Boolean>> exprsn = new ArrayList<>();
  }
  
  private final SolverContext ctx;

  private final ExpressionFilter filter;

  private final ArrayList<StackElement> dkStack = new ArrayList<>();
  
  private StackElement current;
  
  private final boolean evaluate;

  public SimpleSelectiveSolverContext(SolverContext ctx, ExpressionFilter filter, boolean evaluate) {
    this.ctx = ctx;
    this.filter = filter;
    this.evaluate = evaluate;
  }
    
  @Override
  public void push() {
    ctx.push();
    current = new StackElement();
    dkStack.add(current);
  }

  @Override
  public void pop(int n) {
    for (int i=0; i<n; i++) {
      current = dkStack.remove(dkStack.size() -1);
    }
    ctx.pop(n);
  }

  @Override
  public Result solve(Valuation vltn) {
    Result res = ctx.solve(vltn);
    if (res != Result.SAT) {
      return res;
    }
    try {
      if (!hasDK() || (evaluate && evaluate(vltn))) {
        return Result.SAT;
      }
    } catch (UnsupportedOperationException e) {
      System.out.println(e.getMessage());
    }
    
    return Result.DONT_KNOW;
  }

  @Override
  public void add(List<Expression<Boolean>> list) { 
    for (Expression<Boolean> e : list) {
      if (filter.submitToSolver(e)) {
        ctx.add(e);
      } else {
        current.exprsn.add(e);
        //System.out.println(e);
      }
    }
  }

  @Override
  public void dispose() {
    ctx.dispose();
  }
  
  private boolean hasDK() {
    for (StackElement e : dkStack) {
      if (!e.exprsn.isEmpty()) {
        return true;
      }
    }
    return false;
  }  
  
  private boolean evaluate(Valuation vltn) {
    for (StackElement se : dkStack) {
      for (Expression<Boolean> expr : se.exprsn) {
        if (!expr.evaluate(vltn)) {
          return false;
        }
      }
    }
    return true;
  }  
  
}
