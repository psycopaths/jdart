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
package gov.nasa.jpf.jdart.constraints;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.util.ExpressionUtil;

import java.io.IOException;

/**
 * program path.
 */
public class Path {
  
  private Expression<Boolean> pathCondition;
  
  private final PathResult pathResult;
  
  public Path(Expression<Boolean> path, PathResult pathResult) {
    this.pathCondition = path;
    this.pathResult = pathResult;
  }

  /**
   * @return the path
   */
  public Expression<Boolean> getPathCondition() {
    return pathCondition;
  }
  
  public PathResult getPathResult() {
    return pathResult;
  }
  
  public PathResult.ValuationResult getValuationResult() {
    if(pathResult instanceof PathResult.ValuationResult)
      return (PathResult.ValuationResult)pathResult;
    return null;
  }
  
  public PathResult.OkResult getOkResult() {
    if(pathResult instanceof PathResult.OkResult)
      return (PathResult.OkResult)pathResult;
    return null;
  }
  
  public PathResult.ErrorResult getErrorResult() {
    if(pathResult instanceof PathResult.ErrorResult)
      return (PathResult.ErrorResult)pathResult;
    return null;
  }

  /**
   * @return the postCondition
   */
  public PostCondition getPostCondition() {
    PathResult.OkResult okr = getOkResult();
    if(okr != null)
      return okr.getPostCondition();
    return null;
  }
  
  public String getExceptionClass() {
    PathResult.ErrorResult errr = getErrorResult();
    if(errr != null)
      return errr.getExceptionClass();
    return null;
  }
  
  public Valuation getValuation() {
    PathResult.ValuationResult valr = getValuationResult();
    if(valr != null)
      return valr.getValuation();
    return null;
  }
  
  /**
   * @return the state
   */
  public PathState getState() {
    return pathResult.getState();
  }
 
  
  public void print(Appendable a) throws IOException {
    pathCondition.print(a);
    a.append(' ');
    pathResult.print(a, true, false);
  }
    
  @Override  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    try {
      print(sb);
      return sb.toString();
    }
    catch(IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public void appendConstraint(Expression<Boolean> constraint) {
    this.pathCondition = ExpressionUtil.and(this.pathCondition, constraint);
  } 
  
  
}
