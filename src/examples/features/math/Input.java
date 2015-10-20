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
package features.math;

public class Input {
	
  public void foo(double d) {
  	double f = Math.ceil(d);
  	if (f == 10)
  		System.out.println("Math.ceil(" + d + ") == 10");
  	else
  		System.out.println("Math.ceil(" + d + ") != 10");
  }
  
  public void bar(double d1, double d2) {
    double s = java.lang.Math.toRadians(2.2);
    if (d1 > d2) {
      if (Math.sqrt(d1)*s >= 0) {
        
      } else {
        assert false;
      }
    }
  }
  
	public static void main(String[] args) {
    System.out.println("-------- In main!");
    Input inst = new Input();
    try {
    	inst.foo(3.14159);
    } catch (Throwable t) {
    	System.out.println("Caught the rascal <" + t.getMessage() + "> redhanded!");
    }
    inst.bar(1,2);
  }
}
