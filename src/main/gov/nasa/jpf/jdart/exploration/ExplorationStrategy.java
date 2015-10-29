package gov.nasa.jpf.jdart.exploration;

import java.util.List;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.SolverContext;
import gov.nasa.jpf.jdart.constraints.DecisionData;
import gov.nasa.jpf.jdart.constraints.InternalConstraintsTree;
import gov.nasa.jpf.jdart.constraints.Node;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.vm.Instruction;

/**
 * @author luckow 
 */
public abstract class ExplorationStrategy {
  
  public abstract void initialize(SolverContext solverCtx, InternalConstraintsTree constraintsTree);
  public abstract Node findNextNode(Node currentTarget, List<Integer> expectedPath);
  
  public DecisionData buildDecisionData(Node node, Instruction branchInsn, 
      Expression<Boolean>[] constraints, int chosenIdx, boolean explore) {
    return new DecisionData(node, branchInsn, constraints, explore);
  }
}
