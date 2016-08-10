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
package gov.nasa.jpf.jdart.bytecode;

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Access jump table by index and jump
 *   ..., index  ...
 */
public class TABLESWITCH extends gov.nasa.jpf.jvm.bytecode.TABLESWITCH implements SwitchHelper.SwitchInstruction {

  private int min;

  public TABLESWITCH(int defaultTarget, int min, int max){
    super(defaultTarget, min, max);   
    this.min = min;
  }

  @Override
  public Instruction executeConcrete(ThreadInfo ti) {
    return super.execute(ti);
  }

  @Override
  public int chooseTarget(int concreteValue) {
    int i = concreteValue - min;
    if(i < 0 || i >= targets.length)
      return (lastIdx = DEFAULT);
    return (lastIdx = i);
  }

  @Override
  public int getTargetValue(int targetIdx) {
    return targetIdx + min;
  }

  @Override
  public int getTargetPC(int targetIdx) {
    if (targetIdx == DEFAULT) {
      return target;
    }
    return targets[targetIdx];
  }

  @Override
  public int getNumTargets() {
    return targets.length;
  }

  @Override
  public Instruction execute(ThreadInfo ti) {
    return SwitchHelper.execute(this, ti);
  }

}
