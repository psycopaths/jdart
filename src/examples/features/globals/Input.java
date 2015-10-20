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
package features.globals;

import gov.nasa.jpf.jdart.Symbolic;


public class Input {
	
  @Symbolic("true")
  boolean b = false;
  @Symbolic("true")
	int[] k = {1, 2, 3};
  @Symbolic("true")
	static double[] d = {1.0, 2.0, 3.0, 4.0};
  @Symbolic("true")
	int state = 0;
	
  public void foo(int i) {
    if (i > 200000) {
      if (b == true) {
        if (k[0] == k[1])
        	if (d[1] != d[3])
        		d[0] = d[1] + d[3];
        	else
        		d[1] = d[0] + d[3];
      } else {
        ;
      }
      state = k[0];
    } else
    	state = k[1];
  }
  
  public static void main(String[] args) {
    System.out.println("-------- In main!");
    Input inst = new Input();
    inst.foo(-1024);
  }
}
