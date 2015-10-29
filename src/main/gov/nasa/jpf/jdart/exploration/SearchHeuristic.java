package gov.nasa.jpf.jdart.exploration;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.SolverContext;
import gov.nasa.jpf.jdart.bytecode.IDIV;
import gov.nasa.jpf.jdart.bytecode.LDIV;
import gov.nasa.jpf.jdart.bytecode.SwitchHelper.SwitchInstruction;
import gov.nasa.jpf.jdart.constraints.DecisionData;
import gov.nasa.jpf.jdart.constraints.InternalConstraintsTree;
import gov.nasa.jpf.jdart.constraints.Node;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.Predicate;
import gov.nasa.jpf.vm.Instruction;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * @author luckow
 * TODO: Open and exhausted number of choices is currently not maintained
 * in the heuristic-based exploration
 */
public abstract class SearchHeuristic extends ExplorationStrategy {

  private static final class PrefixComparator implements Comparator<Decision> {
    
    private final List<Integer> currentNodePath;
    
    public PrefixComparator(List<Integer> currentNodePath) {
      this.currentNodePath = currentNodePath;
    }
    
    @Override
    public int compare(Decision o1, Decision o2) {
      return getCommonPrefixLength(currentNodePath, o1.getPath()) - 
          getCommonPrefixLength(currentNodePath, o2.getPath()); 
    }
  }
  
  protected static class Decision {
    private final DecisionData decisionData;
    private final int branchIdx;
    private final Instruction tgtInstruction;
    private List<Integer> path;
    
    public Decision(DecisionData decisionData, int branchIdx, Instruction tgtInstruction) {
      this.decisionData = decisionData;
      this.branchIdx = branchIdx;
      this.tgtInstruction = tgtInstruction;
    }

    public List<Integer> getPath() {
      //lazy init. Maybe we don't want to cache the path here because
      //it could be very long
      if(path != null)
        return path;
      //get the path to the decision, and then add the decision
      path = this.decisionData.getNode().getPath();
      path.add(branchIdx);
      return path;
    }
    
    public DecisionData getDecisionData() {
      return decisionData;
    }

    public Node getDecisionNode() {
      return decisionData.getChild(this.branchIdx);
    }
    
    public int getBranchIdx() {
      return branchIdx;
    }

    public Instruction getTargetInstruction() {
      return tgtInstruction;
    }
  }
  
  private final JPFLogger logger = JPF.getLogger("jdart");
  
  private SolverContext solverCtx;
  private PriorityQueue<Decision> queue;
  private Comparator<Decision> comp;
  private InternalConstraintsTree constraintsTree;
  
  private static final int INIT_QUEUE_SIZE = 11;
  
  
  private final static String ORDER_ACC_CLP_CONF = "jdart.exploration.search.clp"; 
  private final boolean ORDER_ACC_CLP_CONF_DEFAULT = false;
  private final boolean longestPrefixOrder;
  
  public SearchHeuristic(Config config) {
    this.longestPrefixOrder = config.getBoolean(ORDER_ACC_CLP_CONF, 
        ORDER_ACC_CLP_CONF_DEFAULT);
  }
  
  @Override
  public void initialize(SolverContext solverCtx, InternalConstraintsTree constraintsTree) {
    this.solverCtx = solverCtx;
    this.constraintsTree = constraintsTree;
    this.comp = getComparator();
    this.queue = new PriorityQueue<>(INIT_QUEUE_SIZE, comp);
  }

  @Override
  public Node findNextNode(Node currentTarget, List<Integer> expectedPath) {
    final List<Integer> currentPath = Collections.unmodifiableList(expectedPath);
    
    Decision bestDec = this.queue.poll();
    if(this.longestPrefixOrder) {
      Set<Decision> cand = new HashSet<>();
      cand.add(bestDec);
      while(this.comp.compare(bestDec, this.queue.peek()) == 0)
        cand.add(this.queue.poll());
      //we spare some cycles: only if multiple candidates
      //were found, will we compute paths and common prefixes
      if(cand.size() > 1) {
        bestDec = Collections.min(cand, new PrefixComparator(currentPath));
        //We must re-establish the state of the queue
        for(Decision d : cand) {
          if(!d.equals(bestDec))
            this.queue.add(d);
        }
      }
    }
    List<Integer> bestDecPath = bestDec.getPath();
    int clp = getCommonPrefixLength(currentPath, bestDecPath);
    
    this.solverCtx.pop(currentPath.size() - clp);
    
    List<Integer> prefix = bestDecPath.subList(0, clp);
    List<Integer> suffix = bestDecPath.subList(clp, bestDecPath.size());
    Node currNode = this.constraintsTree.getNode(prefix);
    for(int decision : suffix) {
      DecisionData dd = (DecisionData)currNode.getNodeData();
      Expression<Boolean> constraint = dd.getConstraint(decision);
      solverCtx.push();
      try {
        solverCtx.add(constraint);
      }
      catch(Exception ex) {
        logger.finer(ex.getMessage());
      }
      currNode = dd.getChild(decision);
    }
    
    //Could optimize this slightly, by keeping the (potential) common prefix in
    //the expected path and only add the suffix
    expectedPath.clear();
    expectedPath.addAll(bestDecPath);
    return currNode;
  }
  
  private static int getCommonPrefixLength(List<Integer> path1, List<Integer> path2) {
    int iterations = Math.min(path1.size(), path2.size());
    int cpl = 0;
    for(; cpl < iterations; cpl++) {
      if(!path1.get(cpl).equals(path2.get(cpl)))
        break;
    }
    return cpl;
  }
  
  @Override
  public DecisionData buildDecisionData(Node node, Instruction branchInsn, 
      Expression<Boolean>[] constraints, int chosenIdx, boolean explore) {
    DecisionData dec = super.buildDecisionData(node, branchInsn, constraints, chosenIdx, explore);

    //chosenIdx is the index (i.e. the child) selected as part of the concrete execution.
    //Therefore, we do not add that child to the queue because it has already been selected.
    //For each conditional statement (except lookupswitch and tableswitch) a single decision
    //will be added to the queue
    if(branchInsn instanceof IfInstruction) {
      IfInstruction ifInstr = (IfInstruction)branchInsn;
      int unexploredBranchIdx = (chosenIdx == 0) ? 1 : 0;
      Instruction tgtInstruction = (unexploredBranchIdx == 0) ? ifInstr.getTarget() : ifInstr.getNext();
      this.queue.add(createDecision(dec, unexploredBranchIdx, 
          constraints[unexploredBranchIdx], tgtInstruction));
      
    } else if(branchInsn instanceof SwitchInstruction) {
      SwitchInstruction swInstruction = (SwitchInstruction)branchInsn;
      for(int idx = 0; idx < swInstruction.getNumTargets(); idx++) {
        //We add all decisions that are not the decision selected as part of the concrete execution
        if(idx != chosenIdx) {
          int pc = swInstruction.getTargetPC(idx);
          Instruction tgtInstr = branchInsn.getMethodInfo().getInstructionAt(pc);
          this.queue.add(createDecision(dec, idx, constraints[idx], tgtInstr));
        }
      }
    } else if(branchInsn instanceof IDIV || branchInsn instanceof LDIV) {
      //TODO: handled almost as ifinstruction atm
      int unexploredBranchIdx = (chosenIdx == 0) ? 1 : 0;
      //TODO: we cannot know the target instruction if an arithmetic exception is thrown. We should probably fix this
      Instruction tgtInstruction = (unexploredBranchIdx == 1) ? null : branchInsn.getNext();
      
      this.queue.add(createDecision(dec, unexploredBranchIdx, constraints[unexploredBranchIdx], tgtInstruction));
    } else
      throw new IllegalStateException("Instruction type: " + branchInsn.getClass().getName() + " not expected at decision point");
    return dec;
  }
  
  protected Decision createDecision(DecisionData decisionData, int branchIdx, 
      Expression<Boolean> constraint, Instruction tgtInstruction) {
    return new Decision(decisionData, branchIdx, tgtInstruction);
  }
  
  /**
   * Returns a comparator used for determining the (heuristically) best decision.
   * Since the constraints tree (and thus the number of possible next decisions to consider)
   * grows exponentially in the program size, the comparator returned here should be efficient.
   * You might for example consider defining a score for each decision, which can then be stored, either
   * in a cache or in a subclass of {@link Decision}. In the latter case you will need to overwrite the 
   * decision factory method {@link #createDecision(DecisionData, int, Expression, Instruction)}.
   * @return Comparator
   */
  protected abstract Comparator<Decision> getComparator();
}
