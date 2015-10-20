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
package summaries.cev;

import gov.nasa.jpf.jdart.Symbolic;


/**
 *
 */
public class Reset {

  @Symbolic("true")
  public int P1;

  @Symbolic("true")
  public int P2;

  @Symbolic("true")
  public int P3;

  @Symbolic("true")
  public int P4;
  
  public void sequence() {
    CEV cev = new CEV();

    cev.reset(P1);
    cev.reset(P2);
    cev.reset(P3);
    cev.reset(P4);    
  }
  
  
  public static void main(String[] args) {
    System.out.println("-------- In main!");
    Reset r = new Reset();
    r.sequence();
  }
  
}
