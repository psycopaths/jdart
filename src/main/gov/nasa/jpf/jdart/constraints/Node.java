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

import java.util.LinkedList;
import java.util.List;

public final class Node {
  private final Node parent;
  private final int depth;
  private int altDepth;

  private NodeData data;
  private final int selectedIdx;

  public Node(Node parent, int selectedIdx) {
    this.selectedIdx = selectedIdx;
    this.parent = parent;
    this.depth = (parent != null) ? parent.depth + 1 : 0;
    this.altDepth = (parent != null) ? parent.altDepth : 0;
  }

  public int incAltDepth() {
    return ++altDepth;
  }

  public boolean isVirgin() {
    return (data == null);
  }


  public Node getParent() {
    return parent;
  }


  public int getDepth() {
    return depth;
  }

  public NodeData getNodeData() {
    return this.data;
  }
  
  public boolean hasData() {
    return (data != null);
  }

  public DecisionData decisionData() {
    if(data == null || data.getClass() != DecisionData.class)
      return null;
    return (DecisionData)data;
  }

  public boolean isOpen() {
    DecisionData dec = decisionData();
    if(dec == null)
      return (data == null);
    return dec.hasOpen();
  }

  public boolean isExhausted() {
    DecisionData dec = decisionData();
    if(dec == null)
      return (data != null && data.getClass() != AbstractResultData.DontKnowData.class); // Dont know is not exhausted, all other forms of data are

      return !dec.hasUnexhausted();
  }

  public boolean hasDecisionData() {
    if(data == null || data.getClass() == AbstractResultData.DontKnowData.class)
      return false;

    //if(data.getClass() == DecisionData.class)
    return true;      
    //throw new IllegalArgumentException("Querying non-decision node (depth: "+ depth + 
    //        ") about decision data! " + data.getClass());
  }
  
  public void setDecision(DecisionData decision) {
    if(!hasDecisionData()) {
      this.data = decision;
    } else {
      DecisionData dec = (DecisionData)data;
      dec.verifyDecision(decision.getInsn(), decision.getConstraints());
    }
  }

  public AbstractResultData.ResultData result(PathResult result) {
    if(data == null || data.getClass() == AbstractResultData.DontKnowData.class) {
      AbstractResultData.ResultData res = new AbstractResultData.ResultData(result);
      data = res;
      return res;
    }

    //throw new IllegalStateException("Attempting to finish already explored path (data = " + data.getClass().getName() + "!");
    return null;
  }

  public AbstractResultData.DontKnowData dontKnow() {
    if(data == null) {
      AbstractResultData.DontKnowData dk = AbstractResultData.DontKnowData.getInstance();
      data = dk;
      return dk;
    }

    if(data.getClass() != AbstractResultData.DontKnowData.class) {
      //System.err.println("Attempting to fail already explored path!");
      return null;
    }
    return (AbstractResultData.DontKnowData)data;
  }

  public UnsatisfiableData unsatisfiable() {
    if(data == null) {
      UnsatisfiableData dk = UnsatisfiableData.getInstance();
      data = dk;
      return dk;
    }

    //throw new IllegalStateException("Attempting to fail already explored path!");
    return null;
  }

  public List<Integer> getPath() {
    LinkedList<Integer> path = new LinkedList<>();
    Node cur = this;
    int selection;
    while((selection = cur.getSelectedIdx()) != -1) {
      path.addFirst(selection);
      cur = cur.getParent();
    }
    return path;
  }

  public int getSelectedIdx() {
    return selectedIdx;
  }
}