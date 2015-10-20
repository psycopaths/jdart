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
package features.bools;

public class Input {
	
	public class TotallyPsyco extends java.lang.AssertionError {
    private static final long serialVersionUID = 1L;

		TotallyPsyco(String msg) {
			super(msg);
		}
	}

  public void foo(int i, boolean b) {
  	//assert(i == 1);
    if (i > 200000) {
      if (b == false) {
        assert false;
      } else {
        ;
      }
    }
//    throw new TotallyPsyco("Odd Psyco");
  }
  
	public static void main(String[] args) {
    System.out.println("-------- In main!");
    Input inst = new Input();
    try {
    	inst.foo(-1024, false);
    	//inst.foo(1, true);
    } catch (Throwable t) {
    	System.out.println("Caught the rascal <" + t.getMessage() + "> redhanded!");
    }
  }
}
