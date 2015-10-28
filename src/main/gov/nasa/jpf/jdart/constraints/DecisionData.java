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
import gov.nasa.jpf.vm.Instruction;

public class DecisionData implements NodeData {
  private final Node node;
  private final Instruction branchInsn;
  private final Expression<Boolean>[] constraints;
  private final Node[] children;
  private int numOpen;
  private int numUnexhausted;

  public DecisionData(Node node, Instruction branchInsn, Expression<Boolean>[] constraints, boolean explore) {
    this.node = node;
    this.branchInsn = branchInsn;
    this.constraints = constraints;
    this.children = new Node[constraints.length];
    this.numUnexhausted = constraints.length;

    if(!explore) {
      for(int i = 0; i < constraints.length; i++) {
        this.children[i] = new Node(node, i);
        this.children[i].dontKnow();
      }
      this.numOpen = 0;
    }
    else {
      this.numOpen = constraints.length;
    }
  }
  
  public Expression<Boolean>[] getConstraints() {
    return constraints;
  }
  
  public Expression<Boolean> getConstraint(int idx) {
    return constraints[idx];
  }

  public Node[] getChildren() {
    return this.children;
  }
  
  public Node getChild(int idx) {
    Node c = children[idx];
    if(c == null) {
      c = new Node(node, idx);
      children[idx] = c;
    }
    return c;
  }

  public boolean hasUnexhausted() {
    return (numUnexhausted > 0);
  }

  public boolean hasOpen() {
    return (numOpen > 0);
  }

  public int nextOpenChild() {
    if(numOpen == 0)
      return -1;

    for(int i = 0; i < constraints.length; i++) {
      Node n = children[i];
      if(n == null || n.isOpen())
        return i;
    }

    return -1;
  }

  public void decrementOpen() {
    numOpen--;
  }

  public void decrementUnexhausted() {
    numUnexhausted--;
  }

  public void verifyDecision(Instruction branchInsn, Expression<Boolean>[] constraints) {
    if(branchInsn != this.branchInsn)
      throw new IllegalStateException("Same decision, but different branching instruction!");
    if(constraints != null && constraints.length != this.constraints.length)
      throw new IllegalStateException("Same decision, but different number of constraints!");
  }

  public Instruction getInsn() {
    return branchInsn;
  }

  public Node getNode() {
    return node;
  }

}