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
package features.simple;

import java.util.Map;
import java.util.HashMap;
public class Input {

	public static class Data {
		private int x;
		private final int y;

		public Data(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}
	

  private int i64;

  public Input(int a, int b) {

    this.i64 = 64;
  }
  
  public Input(int i) {
    if(i > 10) {
      System.err.println("\n-------- In <Init>(int)! i > 10");
    } else {
      System.err.println("\n-------- In <Init>(int! i <= 10");
    }
    this.i64 = 64;
  }
  
  public int foo(int i) {
    System.err.println("\n-------- In foo! Parameter = " + i);

    assert i64 > 0;
    if (i > i64) {
      System.err.printf("%s\n", "i > 64");
      if ( 5 * i <= 325) {
        System.err.printf("%s\n", "5 * i <= 325");
        if (i != 65) {
          System.err.printf("%s\n", "i != 65");
        } else {
          System.err.printf("%s\n", "i == 65");            
        }
        return i + 3;
      } else if ( i != 66 ) {
        System.err.printf("%s\n", "5 * i > 325 && i != 66");
        return i + 5;
      } else {
        System.err.printf("%s\n", "5 * i > 325 && i == 66");
        assert false : "foo failed!";
      }
    } else if ((i & 7) == 7) {
      System.err.printf("%s\n", "i & 5 == 5");    	
    }
    System.err.printf("%s\n", "i <= 64");

    this.i64 = i;

    return i;
  }
  
  public double bar(double d) {
    System.err.println("\n-------- In bar! Parameter = " + d);

    if (d >= 3.141) {
      System.err.printf("%s\n", "d >= 3.141");
    }
   
    double as = Math.asin(d);
    System.err.println("asin returned " + as);
    if(Double.isNaN(as))
      ;//throw new ArithmeticException("NaN");
    return as;
  }
  
  private static short zoo_sub(short j, float f) {
    if (f + j > 256) {
      System.err.printf("%s\n", "i > 73 && f + j > 256");
      return j;
    }
    System.err.printf("%s\n", "i > 73 && f + j <= 256");
    assert(false);
    return 0;
  }
  
  public static short zoo(int i, short j, float f) {
    System.err.println("\n-------- In zoo! Parameters = " + i + ", " + j + ", " + f);

    Integer boxI = i;
    i = boxI.intValue();
    
    Short boxJ = j;
    j = boxJ.shortValue();
    
    Float boxF = f;
    f = boxF.floatValue();
    
    if (i > 73) {
      zoo_sub(j, f);
    }
	else {
		switch(i) {
		case 12:
			System.err.printf("%s\n", "i = 12");
			break;
		case 42:
			System.err.printf("%s\n", "i = 42");
			break;
		default:
		    System.err.printf("%s\n", "i <= 73");
		}
	}
    return j;
  }

	public void baz(Data d) {
		if(d.getX() < 5) {
			System.err.println("x < 5");
		}
		if(d.getY() > 40) {
			System.err.println("y > 40");
		}
		assert (d.getX() + d.getY() < 43);
	}

   public void xyz(Map<String,Integer> m) {
     if(m.get("a") > m.get("b"));
   }


  public static void main(String[] args) {
    System.out.println("-------- In main!");
    Input inst = new Input(64);
	HashMap<String,Integer> hm = new HashMap<String,Integer>();
    hm.put("a", 1);
    hm.put("b", 2);
    inst.xyz(hm);
	try {
    	inst.foo(-1024);
	} catch(AssertionError ex) {}
	inst.bar(1.732);
    inst.zoo(1, (short)2, (float)1.414);
	inst.baz(new Data(37, 42));
  }
}
