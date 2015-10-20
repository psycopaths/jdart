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

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.LogicalOperator;
import gov.nasa.jpf.constraints.expressions.PropositionalCompound;
import gov.nasa.jpf.constraints.util.ExpressionUtil;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.Pair;
import java.util.ArrayList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TrimmedConstraintsTree {
  
  static abstract class Node {
    public abstract boolean isInner();
  }
  
  static class ResultNode extends Node {
    private final PathResult result;
    
    public ResultNode(PathResult result) {
      this.result = result;
    }
    
    @Override
    public boolean isInner() {
      return false;
    }
    
    public PathResult getResult() {
      return result;
    }
  }
  
  static class InnerNode extends Node {
    private final Node[] children;
    private final Expression<Boolean>[] constraints;
    
    @SuppressWarnings("unchecked")
    public InnerNode(Collection<Node> children, List<Expression<Boolean>> constraints) {
      this(children.toArray(new Node[children.size()]), constraints.toArray(new Expression[constraints.size()]));
    }
    
    public InnerNode(Node[] children, Expression<Boolean>[] constraints) {
      assert children.length == constraints.length;
      this.children = children;
      this.constraints = constraints;
    }
    
    @Override
    public boolean isInner() {
      return true;
    }
    
    public int getNumChildren() {
      return children.length;
    }
    
    public Node getChild(int idx) {
      return children[idx];
    }
    
    public Expression<Boolean> getConstraint(int idx) {
      return constraints[idx];
    }
  }
  
  static final ResultNode DONT_KNOW_NODE = new ResultNode(PathResult.dontKnow());
  
  private static final JPFLogger logger = JPF.getLogger("jdart");

  
  static ConstraintsTree.Node toBinaryCTree(TrimmedConstraintsTree.Node root) {
    ConstraintsTree.Node done = null;
    Expression<Boolean> pc = null;
    LinkedList<Pair<TrimmedConstraintsTree.Node, ArrayList<ConstraintsTree.Node>>> stack = new LinkedList<>();
    Pair<TrimmedConstraintsTree.Node, ArrayList<ConstraintsTree.Node>> p = null;
    boolean down = true;
    TrimmedConstraintsTree.Node curr = root;
    int depth = 0;
    int maxdepth = 0;
    while (!stack.isEmpty() || down) {
      // moving down
      if (down) {
        // moving further down
        if (curr.isInner()) {
          p = new Pair<>(curr, new ArrayList<ConstraintsTree.Node>());
          stack.push(p);
          depth++;
          pc = (pc == null) ? ((InnerNode)curr).constraints[0] : 
                  new PropositionalCompound(((InnerNode)curr).constraints[0], LogicalOperator.AND, pc);
          curr = ((InnerNode)curr).children[0];
        } 
        // moving back up
        else {
          down = false;
          done = new ConstraintsTree.Node(
                  pc != null ? pc : ExpressionUtil.TRUE, ((ResultNode)curr).result); 
          maxdepth = java.lang.Math.max(depth, maxdepth);
        }
      }
      // moving up
      else {
        p = stack.pop();
        depth--;
        pc = (pc instanceof PropositionalCompound) ? ((PropositionalCompound)pc).getRight() : null;
        p._2.add(done);
        curr = p._1;
        int idx = p._2.size();
        // moving further up        
        if (idx == ((InnerNode)curr).children.length) {          
          done = toBinaryCTree((InnerNode)curr, p._2.toArray(new ConstraintsTree.Node[] {}), 0);
        }
        // moving down again
        else {
          stack.push(p);
          depth++;
          pc = (pc == null) ? ((InnerNode)curr).constraints[idx] :
                  new PropositionalCompound(((InnerNode)curr).constraints[idx], LogicalOperator.AND, pc);
          curr = ((InnerNode)curr).children[idx];
          down = true;
        }
      }
    }
    logger.fine("Constraints Tree had depth " + maxdepth);
    return done;
  }
  
  private static ConstraintsTree.Node toBinaryCTree(TrimmedConstraintsTree.InnerNode node, 
          ConstraintsTree.Node[] children, int index) {

      if(index == children.length)
        return null;
      
      ConstraintsTree.Node succTrue = children[index];
      ConstraintsTree.Node succFalse = toBinaryCTree(node, children, index+1);
      if(succFalse == null)
        return succTrue;
      return new ConstraintsTree.Node(node.constraints[index], succTrue, succFalse);    
  }
  
  public TrimmedConstraintsTree() {
    // TODO Auto-generated constructor stub
  }

}
