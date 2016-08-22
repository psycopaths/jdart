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

import com.google.gson.Gson;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.expressions.LogicalOperator;
import gov.nasa.jpf.constraints.expressions.Negation;
import gov.nasa.jpf.constraints.expressions.PropositionalCompound;
import gov.nasa.jpf.constraints.util.ExpressionUtil;
import gov.nasa.jpf.constraints.util.MixedParamsException;
import gov.nasa.jpf.util.JPFLogger;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;

/**
 * constraints tree implementation. 
 */
public class ConstraintsTree {

  /* **************************************************************************
   * 
   * nodes
   * 
   */
  
  public static class Node {
  
    private Path path = null;
    
    private Expression<Boolean> decision = null;
    
    private Node parent;
    
    private Node succTrue = null;
    
    private Node succFalse = null;

    public Node(Expression<Boolean> decision, Node succTrue, Node succFalse) {
      this.decision = decision;
      this.succTrue = succTrue;
      succTrue.parent = this;
      this.succFalse = succFalse;
      succFalse.parent = this;
    }
    
    public Node(Expression<Boolean> cond, PathResult result) {
      this.path = new Path(cond, result);
    }
    
    public boolean isLeaf() { return decision == null; }
    
    protected void appendConstraints(Expression<Boolean> constraint) {
      if(decision == null)
        path.appendConstraint(constraint);
      else {
        succTrue.appendConstraints(constraint);
        succFalse.appendConstraints(constraint);
      }
    }
    
    @Override
    public String toString() {
      return toString(false, true);
    }
    
    public String toString(boolean values, boolean postconditions) {
      return toString("", values, postconditions);      
    }
    
    private String toString(final String prefix, boolean values, boolean postconditions) {
      if (isLeaf()) {
        String ret = "_/" + path.getPathResult().toString(postconditions, values);
        
        ret += "\n";
        return ret;
      }
      // add top
      String ret = "-" + this.decision + "\n";
      // add true branch
      String ppTrue = prefix + ((prefix.length() < 1) ? "  |" : "      |");
      ret += ppTrue + "-[+]" + this.succTrue.toString(ppTrue, values, postconditions);
      // add false branch
      String ppFalse = prefix + ((prefix.length() < 1) ? "  " : "      ");
      ret += ppFalse + "+-[-]" + this.succFalse.toString(ppFalse, values, postconditions);
                    
      return ret;
    }

    public void toJson(String filename) {
      JsonNode root = toJson(false, true);
      String tree = new Gson().toJson(root);

      try(PrintWriter out = new PrintWriter(filename)) {
        out.println(tree);
      } catch (Exception ex) {
        logger.severe("Could not write to json file: ", ex);
      }
    }

    private JsonNode toJson(boolean values, boolean postconditions) {
      JsonNode node = new JsonNode();
      if(isLeaf()) {
        node.setResult(path.getPathResult().toString(postconditions, values));
        return node;
      }
      node.setDecison(this.decision);
      node.setChildren(Arrays.asList(this.succTrue.toJson(values, postconditions), 
        this.succFalse.toJson(values, postconditions)));
      return node;
    }

    private class JsonNode {
      private String decision;
      private String result;
      private List<JsonNode> children;

      public void setDecison(Expression<Boolean> decision) {
        this.decision = decision.toString();
      }

      public void setResult(String result) {
        this.result = result;
      }

      public void setChildren(List<JsonNode> children) {
        this.children = children;
      }
    }
    
    public Node strip(Predicate<? super PathResult> pred) {
      if(isLeaf()) {
        if(pred.apply(path.getPathResult()))
          return null;
        return new Node(path.getPathCondition(), path.getPathResult());
      }
      
      Node trueStripped = succTrue.strip(pred);
      Node falseStripped = succFalse.strip(pred);
      if(trueStripped == null)
        return falseStripped;
      if(falseStripped == null)
        return trueStripped;
      return new Node(decision, trueStripped, falseStripped);
    }
    
    /**
     * compute constraints on path from node to root.
     * @return 
     */
    public Expression<Boolean> getConstraint() {
      return getConstraint(null);
    }
    
    private Expression<Boolean> getConstraint(Node child) {

      Expression<Boolean> myDec = this.decision;
      if (myDec != null && child != null && child == succFalse) {
        myDec = new Negation(this.decision);
      }
      
      Expression<Boolean> pExpr = (parent == null) ? null : parent.getConstraint(this);
      if (pExpr == null) {
        return myDec;
      }
      
      if (this.decision == null) {
        return pExpr;
      }
      
      return new PropositionalCompound(pExpr, LogicalOperator.AND, myDec);
    }        
  }

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static JPFLogger logger = JPF.getLogger("jdart");

  /**
   * root of the tree
   */
  private final Node root;
 
  public ConstraintsTree(Node root) {
    this.root = root;
  }
  
  public String toString(boolean values, boolean postconditions) {
    return root.toString(values, postconditions);
  }

  public void toJson(String filename) {
    root.toJson(filename);
  }
  
  /**
   * pretty print
   * 
   * @return 
   */
  @Override
  public String toString() {
    return root.toString();
  }
  
  
  /* **************************************************************************
   * 
   * data retrieval
   * 
   */
  
  public Collection<Path> getCoveredPaths() {
    return getPathsLeadingTo(PathState.OK);
  }
 
  public Collection<Path> getErrorPaths() {
    return getPathsLeadingTo(PathState.ERROR);
  }
  
  public Collection<Path> getDontKnowPaths() {
    return getPathsLeadingTo(PathState.DONT_KNOW);
  }

  public Collection<Path> getAllPaths() {
    ArrayList<Path> ret = new ArrayList<>();    
    for (Node n : getDoneLeaves()) {
      ret.add(n.path);
    }
    return ret;
  }
  
  @Deprecated
  public boolean isEmpty() {
    return this.root.succFalse == null && this.root.succTrue == null;
  }
  
  @Deprecated
  public boolean inError() {
    return this.root.path.getState() == PathState.ERROR;
  }
  
  public Set<Variable<?>> getVariables() {
    Set<Variable<?>> vars = new HashSet<Variable<?>>();
    Collection<Node> nodes = getDoneLeaves();
    for (Node n : nodes) {
      n.path.getPathCondition().collectFreeVariables(vars);
    }
    return vars;
  }
  
  public Expression<Boolean> getConstraint(Predicate<? super PathResult> pred, Predicate<? super Variable<?>> restrict) {
    return getConstraintInSubtree(pred, restrict, root);
  }
  
  
  public Expression<Boolean> getCoveredConstraint() {
//    return getConstraintForStateInSubtree(PathState.OK, root);
    return getConstraintsFor(PathState.OK);
  }
  
  public Expression<Boolean> getErrorConstraint() {
//    return getConstraintForStateInSubtree(PathState.ERROR, root);
    return getConstraintsFor(PathState.ERROR);
  }
  
  public Expression<Boolean> getDontKnowConstraint() {
//    return getConstraintForStateInSubtree(PathState.DONT_KNOW, root);
    return getConstraintsFor(PathState.DONT_KNOW);
  }

  public Expression<Boolean> getCoveredConstraintRestricted(Set<?> restrict) {
    return getConstraintForStateInSubtree(PathState.OK, root, restrict);
  }
  
  public Expression<Boolean> getErrorConstraintRestricted(Set<?> restrict) {
    return getConstraintForStateInSubtree(PathState.ERROR, root, restrict);
  }
  
  public Expression<Boolean> getDontKnowConstraintRestricted(Set<?> restrict) {
    return getConstraintForStateInSubtree(PathState.DONT_KNOW, root,restrict);
  }  
  
  public ConstraintsTree strip(Predicate<? super PathResult> resultPred) {
    Node newRoot = root.strip(resultPred);
    return (newRoot != null) ? new ConstraintsTree(newRoot) : null;
  }
  
  
  
  /* **************************************************************************
   * 
   * private helpers
   * 
   */
  
  private Expression<Boolean> getConstraintInSubtree(Predicate<? super PathResult> pred,
      Predicate<? super Variable<?>> restrict,
      Node subtreeRoot) {
    if(subtreeRoot.isLeaf()) {
      return ExpressionUtil.boolConst(pred.apply(subtreeRoot.path.getPathResult()));
    }
    
    Set<Variable<?>> vars = ExpressionUtil.freeVariables(subtreeRoot.decision);
    boolean relevant = true;
    boolean someRelevant = false;
    for(Variable<?> v : vars) {
      if(!restrict.apply(v))
        relevant = false;
      else
        someRelevant = true;
    }
    
    if(!relevant && someRelevant)
      throw new MixedParamsException();
    
    Expression<Boolean> sf = getConstraintInSubtree(pred, restrict, subtreeRoot.succFalse);
    Expression<Boolean> st = getConstraintInSubtree(pred, restrict, subtreeRoot.succTrue);
    
    
    if(sf instanceof Constant) {
      boolean bf = ((Constant<Boolean>)sf).getValue();
      
      
      if(st instanceof Constant) {
        boolean bt = ((Constant<Boolean>)st).getValue();
        if(bf) {
          if(bt || !relevant)
            return ExpressionUtil.TRUE;
          return new Negation(subtreeRoot.decision);
        }
        if(bt) { // && !bf
          if(!relevant)
            return ExpressionUtil.TRUE;
          return subtreeRoot.decision;
        }
        // !bf && !bt
        return ExpressionUtil.FALSE;
      }
      
      if(bf)
        sf = relevant ? new Negation(subtreeRoot.decision) : ExpressionUtil.TRUE;
      else
        sf = null;
    }
    else {
      if(relevant)
        sf = new PropositionalCompound(new Negation(subtreeRoot.decision), LogicalOperator.AND, sf);
    }
    
    if(st instanceof Constant) {
      boolean bt = ((Constant<Boolean>)st).getValue();
      
      if(bt)
        st = relevant ? subtreeRoot.decision : ExpressionUtil.TRUE;
      else
        st = null;
    }
    else {
      if(relevant)
        st = new PropositionalCompound(subtreeRoot.decision, LogicalOperator.AND, st);
    }
    
    if(sf == null)
      return st;
    if(st == null)
      return sf;
    return new PropositionalCompound(sf, LogicalOperator.OR, st);
  }

  private Expression<Boolean> getConstraintForStateInSubtree(PathState state,
      Node subtreeRoot, Set<?> restrict) {

    if (subtreeRoot.isLeaf()) {

      return subtreeRoot.path.getState().equals(state) ? ExpressionUtil.TRUE
          : ExpressionUtil.FALSE;
    }

    Set<Variable<?>> vars = new HashSet<Variable<?>>();
    subtreeRoot.decision.collectFreeVariables(vars);
    boolean relevant = true;
    if (!restrict.containsAll(vars)) {
      relevant = false;
    }

    // check on children ...
    Expression<Boolean> sf = getConstraintForStateInSubtree(state,
        subtreeRoot.succFalse, restrict);
    Expression<Boolean> st = getConstraintForStateInSubtree(state,
        subtreeRoot.succTrue, restrict);

    // can we simplify?
    if (sf instanceof Constant && st instanceof Constant) {
      boolean bf = ((Constant<Boolean>) sf).getValue();
      boolean bt = ((Constant<Boolean>) st).getValue();

      if (bf && bt) {
        return ExpressionUtil.TRUE;
      }

      if (!bf && !bt) {
        return ExpressionUtil.FALSE;
      }

      if (bt) {
        return relevant ? subtreeRoot.decision : ExpressionUtil.TRUE;
      } else {
        return relevant ? new Negation(subtreeRoot.decision)
            : ExpressionUtil.TRUE;
      }
    }

    // simplify independenty
    if (sf instanceof Constant) {
      boolean bf = ((Constant<Boolean>) sf).getValue();
      if (bf) {
        sf = relevant ? new Negation(subtreeRoot.decision)
            : ExpressionUtil.TRUE;
      } else {
        sf = null;
      }
    } else {
      if (relevant)
        sf = new PropositionalCompound(new Negation(subtreeRoot.decision),
            LogicalOperator.AND, sf);
    }

    // simplify independenty
    if (st instanceof Constant) {
      boolean bt = ((Constant<Boolean>) st).getValue();
      if (bt) {
        st = relevant ? subtreeRoot.decision : ExpressionUtil.TRUE;
      } else {
        st = null;
      }
    } else {
      if (relevant)
        st = new PropositionalCompound(subtreeRoot.decision,
            LogicalOperator.AND, st);
    }

    if (sf == null || st == null) {
      return (sf != null) ? sf : st;
    }

    return new PropositionalCompound(sf, LogicalOperator.OR, st);
  }
    
  private Expression<Boolean> getConstraintsFor(PathState state) {
    LinkedList<Node> nodes = new LinkedList<Node>(getNodesMatchingState(state));
    if (nodes.isEmpty()) {
      return ExpressionUtil.FALSE;
    }
    Expression<Boolean> ret = nodes.pollLast().path.getPathCondition();
    while (!nodes.isEmpty()) {
      ret = new PropositionalCompound(ret, LogicalOperator.OR,
              nodes.pollLast().path.getPathCondition());
    }
    return ret;
  }
  
  private Collection<Path> getPathsLeadingTo(PathState state) {
    Collection<Node> nodes = getNodesMatchingState(state);
    Collection<Path> result = new ArrayList<Path>();
    for (Node n : nodes) {
      result.add(n.path);
    }
    return result;
  }
  
  private Collection<Node> getNodesMatchingState(PathState state) {
    Collection<Node> result = new ArrayList<Node>();
    getNodesMatchingState(state, root, result);
    return result;
  }
  
  private void getNodesMatchingState(PathState state, Node n, Collection<Node> matching) {
    if (n == null) {
      return;
    }
    
    if (n.isLeaf() && n.path.getState().equals(state)) {
      matching.add(n);      
    }
    
    getNodesMatchingState(state, n.succFalse, matching);
    getNodesMatchingState(state, n.succTrue, matching);
  }
  
  private Collection<Node> getDoneLeaves() {
    Collection<Node> result = new ArrayList<>();
    Node curr = root;
    Node prev = null;
    
    while (curr != null) {
      if (curr.isLeaf()) {
        result.add(curr);
        prev = curr; curr = curr.parent;
      }
      // down
      else if (prev == null || prev == curr.parent) {
        prev = curr; curr = curr.succTrue;
      }
      // from true to false
      else if (prev == curr.succTrue) {
        prev = curr; curr = curr.succFalse;
      }
      // up
      else {
        prev = curr; curr = curr.parent;
      }
    }
    
    return result;    
  }
  
  private void getDoneLeaves(Node n, Collection<Node> leaves) {
    if (n == null) {
      return;
    }
    
    if (n.isLeaf()) {
      leaves.add(n);
    }
    
    getDoneLeaves(n.succFalse, leaves);
    getDoneLeaves(n.succTrue, leaves);
  }

}
