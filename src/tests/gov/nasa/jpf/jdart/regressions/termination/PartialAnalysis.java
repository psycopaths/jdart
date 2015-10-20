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
package gov.nasa.jpf.jdart.regressions.termination;

import static gov.nasa.jpf.jdart.regressions.termination.InfiniteLoop.concolic;

/**
 *
 */
public class PartialAnalysis {
  
  public static void test(int i) {
    if (i != 1)
      if (i != 2)
        if (i != 3)
          if (i != 4)
            if (i != 5)
              if (i != 6)
                assert false;
  }

  public static void main(String[] args) throws Throwable {
    test(0);
  }  
}
