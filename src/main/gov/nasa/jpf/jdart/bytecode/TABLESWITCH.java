//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
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
