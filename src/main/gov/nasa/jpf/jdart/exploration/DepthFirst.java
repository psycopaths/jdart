package gov.nasa.jpf.jdart.exploration;

import java.util.List;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.SolverContext;
import gov.nasa.jpf.jdart.constraints.DecisionData;
import gov.nasa.jpf.jdart.constraints.InternalConstraintsTree;
import gov.nasa.jpf.jdart.constraints.Node;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author luckow 
 */
public class DepthFirst extends ExplorationStrategy {

  private final JPFLogger logger = JPF.getLogger("jdart");
  
  private SolverContext solverCtx;
  
  @Override
  public void initialize(SolverContext solverCtx, InternalConstraintsTree constraintsTree) {
    this.solverCtx = solverCtx;
  }
  
  @Override
  public Node findNextNode(Node currentNode, List<Integer> expectedPath) {
    if(currentNode == null)
      return null;
    
    while(!currentNode.isOpen()) {
      boolean exh = currentNode.isExhausted();
      currentNode = currentNode.getParent();
      if(currentNode == null) //previous node was the root
        return null;
      solverCtx.pop();
      expectedPath.remove(expectedPath.size() - 1);
      DecisionData dec = currentNode.decisionData();
      dec.decrementOpen();
      if(exh)
        dec.decrementUnexhausted();
    }
    
    DecisionData dec = currentNode.decisionData();

    int nextIdx = dec.nextOpenChild();
    assert (nextIdx != -1);
    Expression<Boolean> constraint = dec.getConstraint(nextIdx);
    Node nxtNode = dec.getChild(nextIdx);
    solverCtx.push();
    expectedPath.add(nextIdx);
    try {
      solverCtx.add(constraint);
    }
    catch(Exception ex) {
      logger.finer(ex.getMessage());
      //currentTarget.dontKnow(); // TODO good idea?
    }
    
    return nxtNode;
  }
}
