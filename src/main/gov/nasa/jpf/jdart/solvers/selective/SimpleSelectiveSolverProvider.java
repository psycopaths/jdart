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
import gov.nasa.jpf.constraints.solvers.ConstraintSolverFactory;
import gov.nasa.jpf.constraints.solvers.ConstraintSolverProvider;
import java.util.Properties;

/**
 *
 */
public class SimpleSelectiveSolverProvider implements ConstraintSolverProvider {

  @Override
  public String[] getNames() {
    return new String[]{"selective"};
  }

  @Override
  public ConstraintSolver createSolver(Properties config) {
    String dp = "dontknow";
    if (config.containsKey("selective.dp")) {
      dp = config.getProperty("selective.dp");
    }

    boolean eval = true;
    if (config.containsKey("selective.eval")) {
      eval = Boolean.parseBoolean(config.getProperty("selective.eval"));
    }    
    
    ExpressionFilter filter = new CombinationFilter(new ExpressionFilter[] {
      new FloatConstantFilter(), new FunctionFilter()
    });
    
    ConstraintSolver solver = ConstraintSolverFactory.getRootFactory().createSolver(dp, config);
    return new SimpleSelectiveSolver(solver, filter, eval);
  }

}
