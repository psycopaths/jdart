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
import gov.nasa.jpf.constraints.solvers.ConstraintSolverFactory;
import gov.nasa.jpf.constraints.solvers.ConstraintSolverProvider;
import gov.nasa.jpf.jdart.solvers.selective.CombinationFilter;
import gov.nasa.jpf.jdart.solvers.selective.ExpressionFilter;
import gov.nasa.jpf.jdart.solvers.selective.FloatConstantFilter;
import gov.nasa.jpf.jdart.solvers.selective.FunctionFilter;
import java.util.Properties;

/**
 *
 */
public class FunctionSolverProvider implements ConstraintSolverProvider {

  @Override
  public String[] getNames() {
    return new String[]{"functions"};
  }

  @Override
  public ConstraintSolver createSolver(Properties config) {
    String dp = "dontknow";
    if (config.containsKey("functions.dp")) {
      dp = config.getProperty("functions.dp");
    }

    boolean dbounds = true;
    if (config.containsKey("functions.use_domain_bounds")) {
      dbounds = Boolean.parseBoolean(config.getProperty("functions.use_domain_bounds"));
    }    

    boolean rbounds = true;
    if (config.containsKey("functions.use_range_bounds")) {
      rbounds = Boolean.parseBoolean(config.getProperty("functions.use_range_bounds"));
    }      
    
    boolean defs = true;
    if (config.containsKey("functions.use_fct_defs")) {
      defs = Boolean.parseBoolean(config.getProperty("functions.use_fct_defs"));
    }    
    
    boolean inst = true;
    if (config.containsKey("functions.instantiate")) {
      inst = Boolean.parseBoolean(config.getProperty("functions.instantiate"));
    }   
    
    ConstraintSolver solver = ConstraintSolverFactory.getRootFactory().createSolver(dp, config);
    return new FunctionSolver(solver, dbounds, rbounds, defs, inst);
  }

}
