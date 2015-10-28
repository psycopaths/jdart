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
package gov.nasa.jpf.jdart.solvers.functions;

import gov.nasa.jpf.constraints.api.ConstraintSolver;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.SolverContext;
import gov.nasa.jpf.constraints.api.Valuation;

public class FunctionSolver extends ConstraintSolver {

  private final ConstraintSolver solver;
  
  private final boolean useDomainBounds;
  private final boolean useRangeBounds;  
  private final boolean useDefinitions;
  private final boolean instantiate;

  public FunctionSolver(ConstraintSolver solver, boolean useDomainBounds, 
          boolean useRangeBounds, boolean useDefinitions, boolean instantiate) {
    this.solver = solver;
    this.useDomainBounds = useDomainBounds;
    this.useRangeBounds = useRangeBounds;
    this.useDefinitions = useDefinitions;
    this.instantiate = instantiate;
  }

      
  @Override
  public ConstraintSolver.Result solve(Expression<Boolean> exprsn, Valuation vltn) {
    SolverContext ctx = createContext();
    ctx.add(exprsn);
    return ctx.solve(vltn);    
  }
  
  @Override
  public FunctionSolverContext createContext() {  
    SolverContext ctx = solver.createContext();
    return new FunctionSolverContext(ctx, useDomainBounds, 
            useRangeBounds, useDefinitions, instantiate);
  }  
}
