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

package summaries.abp;

import gov.nasa.jpf.jdart.Symbolic;

/** This example is a modified version from:
 *  Automata Learning with Automated Alphabet Abstraction Refinement
 *  Falk Howar, Bernhard Steffen, and Maik Merten
 *  VMCAI 2011
 */

public class Protocol {
 
  @Symbolic("true")  
  private int buffer_empty = 1;
  
  // pdu p, ack; // pdus
  @Symbolic("true")  
  public int expect = 0; // next expected seq. nr

  public void msg(int sequence, int content) {
    if (sequence < 0) return;
  	
    System.out.println("expect = " + expect);
    int prevExpect = expect;
    if (expect > 0)
      prevExpect--;
  	
    if ((buffer_empty==1) && ((sequence + 7) % 7 == (prevExpect + 2) % 2)) {  // this is as expected
      expect++;
      buffer_empty = 0;
      // OK message will be passed to upper layer
    } else {
      assert false;
      // message is discarded
    }
  } 
  
  public void recv_ack(int value) {
    if (buffer_empty==1) {
      assert false;
    } else {
      if (value == (((expect-1) + 2) % 2)) {
         // ack is enabled, message is consumed
        buffer_empty = 1-buffer_empty;
      } else {
        // not the right sequence
        assert false;
      }
    }        
  }
    
  public void reset() {
    
//    if (buffer_empty == 1) assert false;
    
    buffer_empty = 1;
    expect = 0;
  }
  
  public static void main(String[] args) {
    System.out.println("-------- In main!");
    Protocol p = new Protocol();
    p.msg(0, 6);
    p.recv_ack(0);
	p.reset();
  }
}

