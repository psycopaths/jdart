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
package gov.nasa.jpf.jdart.termination;



/**
 * Executes up to a fixed number of times.
 * 
 */
public class UpToFixedNumber extends TerminationStrategy {

  private int toGo;

  public UpToFixedNumber(int n){
    if (n <= 0)
      throw new IllegalArgumentException("bogus number " + n);
    this.toGo= n;
  }

  @Override
  public boolean isDone() {
    toGo -= 1;
    return toGo <= 0;
  }

  @Override
  public String getReason() {
// FIXME: re-introduce actual test for termination
    if (true)
//    if (ConstraintsTree.done())
      return "Resolved all paths!";
    else
      return "Execution Limit Reached";
  }
}
