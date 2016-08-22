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

import gov.nasa.jpf.constraints.api.Valuation;

import java.io.IOException;

public class PathResult {
  
  public static abstract class ValuationResult extends PathResult {
    private final Valuation valuation;
    
    private ValuationResult(PathState ps, Valuation valuation) {
      super(ps);
      this.valuation = valuation;
    }
    
    public Valuation getValuation() {
      return valuation;
    }
    
    
    public void print(Appendable a, boolean printDetails, boolean printValues) throws IOException {
      super.print(a, printValues, printDetails);
      if(printValues) {
        a.append(": ");
        valuation.print(a);
      }
    }
  }
  
  public static final class OkResult extends ValuationResult {
    private final PostCondition postCondition;
    
    public OkResult(Valuation valuation, PostCondition postCondition) {
      super(PathState.OK, valuation);
      this.postCondition = postCondition;
    }
    
    public PostCondition getPostCondition() {
      return postCondition;
    }
    
    public void print(Appendable a, boolean printPost, boolean printValues) throws IOException {
      super.print(a, printPost, printValues);
      if(printPost) {
        a.append((printValues) ? ", " : ": ");
        postCondition.print(a);
      }
    }
    
  }
  
  public static final class ErrorResult extends ValuationResult {
    private final String exceptionClass;
    private final String stackTrace;
    
    public ErrorResult(Valuation valuation, String exceptionClass, String stackTrace) {
      super(PathState.ERROR, valuation);
      this.exceptionClass = exceptionClass;
      this.stackTrace = stackTrace;
    }
    
    public String getExceptionClass() {
      return exceptionClass;
    }
    
    public String getStackTrace() {
      return stackTrace;
    }
    
    public void print(Appendable a, boolean printExcName, boolean printValues) throws IOException {
      super.print(a, printExcName, printValues);
      if(printExcName) {
        a.append((printValues) ? ", " : ": ");
        a.append(exceptionClass);
      }
    }
  }
  
  public static OkResult ok(Valuation valuation, PostCondition pc) {
    return new OkResult(valuation, pc);
  }
  
  public static ErrorResult error(Valuation valuation, String exceptionClass, String stackTrace) {
    return new ErrorResult(valuation, exceptionClass, stackTrace);
  }
  
  public static PathResult dontKnow() {
    return DONT_KNOW;
  }
  
  
  public static PathResult DONT_KNOW = new PathResult(PathState.DONT_KNOW);
  
  
  
  
  
  private final PathState state;
  
  protected PathResult(PathState state) {
    this.state = state;
  }
  
  public PathState getState() {
    return state;
  }
  
  public void print(Appendable a, boolean printDetails, boolean printValues) throws IOException {
    a.append(state.toString());
  }
  
  @Override
  public String toString() {
    return toString(true, false);
  }
  
  public String toString(boolean printDetails, boolean printValues) {
    StringBuilder sb = new StringBuilder();
    try {
      print(sb, printDetails, printValues);
      return sb.toString();
    }
    catch(IOException ex) {
      throw new IllegalStateException();
    }
  }

}
