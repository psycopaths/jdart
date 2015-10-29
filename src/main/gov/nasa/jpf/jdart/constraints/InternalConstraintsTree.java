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
import gov.nasa.jpf.constraints.api.ConstraintSolver.Result;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.SolverContext;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.util.ExpressionUtil;
import gov.nasa.jpf.jdart.config.AnalysisConfig;
import gov.nasa.jpf.jdart.config.ConcolicValues;
import gov.nasa.jpf.jdart.exploration.ExplorationStrategy;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.Pair;
import gov.nasa.jpf.vm.Instruction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class InternalConstraintsTree {

  
  public enum BranchEffect {
    NORMAL,
    UNEXPECTED,
    INCONCLUSIVE
  }
    
  private final JPFLogger logger = JPF.getLogger("jdart");
  
  
  private final Node root = new Node(null, -1);
  private Node current = root; // This is the current node in our EXPLORATION
  private Node currentTarget = root; // This is the node the valuation computed by the constraint solver SHOULD reach
  
  private final AnalysisConfig anaConf;
  
  private ArrayList<Integer> expectedPath = new ArrayList<>();
  private boolean diverged = false;
  private final SolverContext solverCtx;
  
  private final ExplorationStrategy explorationStrategy;
  private boolean explore;
  
  private final ConcolicValues preset;
  private boolean replay = false;
  
  private Valuation prev = null;
  
  public InternalConstraintsTree(SolverContext solverCtx, ExplorationStrategy explorationStrategy, AnalysisConfig anaConf) {
    this(solverCtx, explorationStrategy, anaConf, null);   
  }

  public InternalConstraintsTree(SolverContext solverCtx, ExplorationStrategy explorationStrategy, AnalysisConfig anaConf, ConcolicValues preset) {
    this.solverCtx = solverCtx;
    
    this.explorationStrategy = explorationStrategy;
    this.explorationStrategy.initialize(this.solverCtx, this);
    
    this.anaConf = anaConf;
    this.explore = anaConf.isExploreInitially();
    this.preset = preset;
  }
  
  public Node getNode(List<Integer> path) throws IllegalStateException {
    Node curr = root;
    for(int decision : path) {
      NodeData currNodeData = curr.getNodeData();
      if(!(currNodeData instanceof DecisionData)) {
        throw new IllegalStateException("Path leads to node with no decision data. Node data was of type: " + 
            currNodeData.getClass().getName());
      }
      Node nxt = ((DecisionData)currNodeData).getChild(decision);
      curr = nxt;
    }
    return curr;
  }
  
  public void setExplore(boolean explore) {
    this.explore = explore;
  }
  
  public boolean needsDecision() {
    return !current.hasDecisionData();
  }
  
  /**
   * Retrieves the node in the constraints tree that would be reached using
   * the given valuation.
   * 
   * @param valuation the valuation
   * @return the node in the tree that would be reached by the given valuation
   */
  public Node simulate(Valuation valuation) {
    Node curr = root;

    while(curr.decisionData() != null) {
      DecisionData dd = curr.decisionData();
      int branchIdx = -1;
      for(int i = 0; i < dd.getConstraints().length; i++) {
        Expression<Boolean> constraint = dd.getConstraints()[i];
        try {
          if(constraint.evaluate(valuation)) {
            branchIdx = i;
            break;
          }
        }
        catch(RuntimeException ex) {
          // e.g. due to function with undefined semantics
          return null;
        }
      }
      if(branchIdx < 0) {
        throw new IllegalStateException("Non-complete set of constraints at constraints tree node!");
      }
      if(dd.getChildren()[branchIdx] == null) {
        break;
      }
      curr = dd.getChildren()[branchIdx];
    }

    return curr;
  }

  public BranchEffect decision(Instruction insn, int chosenIdx, Expression<Boolean>[] decisions) {
    if(anaConf.maxDepthExceeded(current.getDepth())) {
      //System.err.println("DEPTH EXCEEDED");
      return BranchEffect.NORMAL; // just ignore it
    }

    DecisionData data;
    try {
      if(!current.hasDecisionData())
        data = this.explorationStrategy.buildDecisionData(current, insn, decisions, chosenIdx, explore);
      else {
        data = current.decisionData();
        data.verifyDecision(insn, decisions);
      }
        
      current.setDecision(data);
    } catch(IllegalStateException e) {
      logger.severe(e.getMessage());
      // FIXME: this indicates a bug //
      return BranchEffect.INCONCLUSIVE;
    }
    
    int depth = current.getDepth();
    current = data.getChild(chosenIdx);
    
    if(current.isExhausted() && !replay) { // FALK: check how exhaustion is computed, maybe replay check is not necessary
      diverged = true;
      return BranchEffect.INCONCLUSIVE;
    }

    if(!diverged) {
      if(depth < expectedPath.size()) {
        int expected = expectedPath.get(depth).intValue();
        if(expected != chosenIdx) {
          diverged = true;
          return BranchEffect.UNEXPECTED;
        } 
      }
      else {
        Expression<Boolean> constraint = data.getConstraint(chosenIdx);
        solverCtx.push();
        try {
          solverCtx.add(constraint);
        }
        catch(RuntimeException ex) {
          logger.finer(ex.getMessage()); 
          //ex.printStackTrace();
        }

        expectedPath.add(chosenIdx);
        currentTarget = current;
      }
    }

    return BranchEffect.NORMAL;
  }
  
  public void finish(PathResult result) {
    current.result(result);
  }
  
  
  public void failCurrentTarget() {
    currentTarget.dontKnow();
  }
  
  
  private Node backtrack(Node node, boolean pop) {
    if(node == null)
      return null;
    
    while(!node.isOpen()) {
      boolean exh = node.isExhausted();
      node = node.getParent();
      if(node == null)
        break;
      if(pop) {
        solverCtx.pop();
        expectedPath.remove(expectedPath.size() - 1);
      }
      DecisionData dec = node.decisionData();
      dec.decrementOpen();
      if(exh)
        dec.decrementUnexhausted();
    }
    
    return node;
  }
  
  public Valuation findNext() {
    replay = false;
    
    if(diverged) {
      backtrack(current, false);
      diverged = false;
    }
    current = root;
    while((currentTarget = explorationStrategy.findNextNode(currentTarget, expectedPath)) != null) {
      assert currentTarget.isVirgin();
      //Check that we have not exceeded the depth bound
      int ad = currentTarget.incAltDepth();
      if(anaConf.maxAltDepthExceeded(ad) || anaConf.maxDepthExceeded(currentTarget.getDepth())) {
        //If we have, we look for a new node
        currentTarget.dontKnow();
        continue;
      } else {     
        Valuation val = new Valuation();
        logger.finer("Finding new valuation");
        Result res = solverCtx.solve(val);
        logger.finer("Found: " + res + " : " + val);
        // FIXME: prevent generation of valuation that has been used before.        
        switch(res) {
        case UNSAT:
          currentTarget.unsatisfiable();
          break;
        case DONT_KNOW:
          currentTarget.dontKnow();
          break;
        case SAT:
          Node predictedTarget = simulate(val);
          if(predictedTarget != null && predictedTarget != currentTarget) {
            boolean inconclusive = predictedTarget.isExhausted();
            logger.finer("Predicted ", inconclusive ? "inconclusive " : "", "divergence");
            if(inconclusive) {
              logger.finer("NOT attempting execution");
              currentTarget.dontKnow();
              break;
            }
          }
          if (val.equals(prev)) {
            logger.finer("Wont re-execute with known valuation");
            currentTarget.dontKnow();
            break;
          }
          prev = val;
          return ExpressionUtil.combineValuations(val);
        }
      }
    }
    
    //We fall back on the preset values that might be specified in the
    //jpf config -- this only happens when we cannot find a new target
    //node from exercising the constraints tree
    if (preset != null && preset.hasNext()) {
      current = root;
      currentTarget = root;
      assert this.expectedPath.isEmpty();
      replay = true;
      return preset.next();
    }

    return null;
  }

  public ConstraintsTree toFinalCTree() {
    if (root == null) {
      return null;
    }    
    TrimmedConstraintsTree.Node r = trim();    
    if (r == null) {
      return null;
    } 
    return new ConstraintsTree(TrimmedConstraintsTree.toBinaryCTree(r));
  }
    
  TrimmedConstraintsTree.Node trim() {
    LinkedList<Pair<Integer,TrimmedConstraintsTree.Node[]>> stack = new LinkedList<>();
    Node curr = root;
    Node prev = null;
    TrimmedConstraintsTree.Node done = null;
    
    while (curr != null) {
      // moving down
      if (prev == null || prev == curr.getParent()) {        
        // moving further down
        if (curr.getNodeData() != null && curr.getNodeData().getClass().getName().equals(DecisionData.class.getName())) {
          DecisionData d = (DecisionData)curr.getNodeData();
          int cCount = d.getChildren().length;
          assert cCount > 0;
         
          int idx = 0;
          TrimmedConstraintsTree.Node[] arr = new TrimmedConstraintsTree.Node[cCount];
          for (int i=0; i<cCount; i++) {
            if (d.getChildren()[i] != null) {
              break;
            }
            arr[idx++] = null;
          }
          
          // moving back up 
          if (idx == cCount) {
            done = generateTrimmedNode(d, arr);
            Node tmp = curr; curr = prev; prev = tmp;            
            continue;
          }
          
          // moving further down
          Pair<Integer, TrimmedConstraintsTree.Node[]> p = new Pair<>(idx, arr);
          stack.push(p);
          prev = curr; curr = d.getChildren()[idx];
          continue;
        }

        // moving back up
        if (curr.getNodeData() == null || curr.getNodeData().getClass().getName().equals(UnsatisfiableData.class.getName())) {
          done = null;
        }        
        else if (curr.getNodeData().getClass().getName().equals(AbstractResultData.ResultData.class.getName())) {
          AbstractResultData.ResultData d = (AbstractResultData.ResultData) curr.getNodeData();
          done = new TrimmedConstraintsTree.ResultNode(d.result);
        }
        else if (curr.getNodeData().getClass().getName().equals(AbstractResultData.DontKnowData.class.getName())) {
          done = TrimmedConstraintsTree.DONT_KNOW_NODE;
        }
        Node tmp = curr; curr = prev; prev = tmp;
      } 
      // moving up
      else {
        Pair<Integer, TrimmedConstraintsTree.Node[]> p = stack.pop();
        DecisionData data = (DecisionData) curr.getNodeData();
        p._2[p._1] = done;

        int idx = p._1;
        while (++idx < p._2.length) {
          if (data.getChildren()[idx] != null) {
            break;
          }
        }

        // moving further up
        if (idx == p._2.length) {        
          done = generateTrimmedNode(data, p._2);
          prev = curr; curr = curr.getParent();
          continue;
        }
        // moving down again
        p = new Pair(idx, p._2);
        stack.push(p);
        prev = curr; curr = data.getChildren()[idx];
      }
    }
    return done;
  }


  private TrimmedConstraintsTree.Node generateTrimmedNode(DecisionData d, TrimmedConstraintsTree.Node[] arr) {
    List<TrimmedConstraintsTree.Node> tchildren = new ArrayList<>();
    List<Expression<Boolean>> tconstraints = new ArrayList<>();
    boolean allDontKnow = true;
    for(int i = 0; i < arr.length; i++) {
      TrimmedConstraintsTree.Node tc = arr[i];
      if(tc == null)
        continue;
      tchildren.add(tc);
      tconstraints.add(d.getConstraints()[i]);
      if(tc != TrimmedConstraintsTree.DONT_KNOW_NODE)
        allDontKnow = false;
    }

    if(tchildren.isEmpty())
      return null;
    if(tchildren.size() == 1)
      return tchildren.iterator().next();
    if(allDontKnow)
      return TrimmedConstraintsTree.DONT_KNOW_NODE;
    return new TrimmedConstraintsTree.InnerNode(tchildren, tconstraints);
  }  

  public boolean isExplore() {
    return explore;
  }
}
