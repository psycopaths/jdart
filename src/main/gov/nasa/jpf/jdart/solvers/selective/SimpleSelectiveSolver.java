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

import gov.nasa.jpf.constraints.api.ConstraintSolver;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.SolverContext;
import gov.nasa.jpf.constraints.api.Valuation;

/**
 *
 */
public class SimpleSelectiveSolver extends ConstraintSolver {

  private final ConstraintSolver solver;
  
  private final ExpressionFilter filter;
  
  private final boolean evaluate;
    
  public SimpleSelectiveSolver(ConstraintSolver solver, ExpressionFilter filter, boolean evaluate) {
    this.solver = solver;
    this.filter = filter;
    this.evaluate = evaluate;
  }
    
  @Override
  public Result solve(Expression<Boolean> exprsn, Valuation vltn) {
    if (!filter.submitToSolver(exprsn)) {
      return Result.DONT_KNOW;
    }
    return solver.solve(exprsn, vltn);
  }
  
  @Override
  public SimpleSelectiveSolverContext createContext() {  
    SolverContext ctx = solver.createContext();
    return new SimpleSelectiveSolverContext(ctx, filter, evaluate);
  }
}
