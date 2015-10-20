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
package gov.nasa.jpf.jdart;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jdart.bytecode.ATHROW;
import gov.nasa.jpf.jdart.bytecode.D2F;
import gov.nasa.jpf.jdart.bytecode.D2I;
import gov.nasa.jpf.jdart.bytecode.D2L;
import gov.nasa.jpf.jdart.bytecode.DADD;
import gov.nasa.jpf.jdart.bytecode.DCMPG;
import gov.nasa.jpf.jdart.bytecode.DCMPL;
import gov.nasa.jpf.jdart.bytecode.DDIV;
import gov.nasa.jpf.jdart.bytecode.DMUL;
import gov.nasa.jpf.jdart.bytecode.DNEG;
import gov.nasa.jpf.jdart.bytecode.DREM;
import gov.nasa.jpf.jdart.bytecode.DSUB;
import gov.nasa.jpf.jdart.bytecode.EXECUTENATIVE;
import gov.nasa.jpf.jdart.bytecode.F2D;
import gov.nasa.jpf.jdart.bytecode.F2I;
import gov.nasa.jpf.jdart.bytecode.F2L;
import gov.nasa.jpf.jdart.bytecode.FADD;
import gov.nasa.jpf.jdart.bytecode.FCMPG;
import gov.nasa.jpf.jdart.bytecode.FCMPL;
import gov.nasa.jpf.jdart.bytecode.FDIV;
import gov.nasa.jpf.jdart.bytecode.FMUL;
import gov.nasa.jpf.jdart.bytecode.FNEG;
import gov.nasa.jpf.jdart.bytecode.FREM;
import gov.nasa.jpf.jdart.bytecode.FSUB;
import gov.nasa.jpf.jdart.bytecode.I2B;
import gov.nasa.jpf.jdart.bytecode.I2C;
import gov.nasa.jpf.jdart.bytecode.I2D;
import gov.nasa.jpf.jdart.bytecode.I2F;
import gov.nasa.jpf.jdart.bytecode.I2L;
import gov.nasa.jpf.jdart.bytecode.I2S;
import gov.nasa.jpf.jdart.bytecode.IADD;
import gov.nasa.jpf.jdart.bytecode.IAND;
import gov.nasa.jpf.jdart.bytecode.IDIV;
import gov.nasa.jpf.jdart.bytecode.IFEQ;
import gov.nasa.jpf.jdart.bytecode.IFGE;
import gov.nasa.jpf.jdart.bytecode.IFGT;
import gov.nasa.jpf.jdart.bytecode.IFLE;
import gov.nasa.jpf.jdart.bytecode.IFLT;
import gov.nasa.jpf.jdart.bytecode.IFNE;
import gov.nasa.jpf.jdart.bytecode.IF_ICMPEQ;
import gov.nasa.jpf.jdart.bytecode.IF_ICMPGE;
import gov.nasa.jpf.jdart.bytecode.IF_ICMPGT;
import gov.nasa.jpf.jdart.bytecode.IF_ICMPLE;
import gov.nasa.jpf.jdart.bytecode.IF_ICMPLT;
import gov.nasa.jpf.jdart.bytecode.IF_ICMPNE;
import gov.nasa.jpf.jdart.bytecode.IINC;
import gov.nasa.jpf.jdart.bytecode.IMUL;
import gov.nasa.jpf.jdart.bytecode.INEG;
import gov.nasa.jpf.jdart.bytecode.IOR;
import gov.nasa.jpf.jdart.bytecode.IREM;
import gov.nasa.jpf.jdart.bytecode.ISHL;
import gov.nasa.jpf.jdart.bytecode.ISHR;
import gov.nasa.jpf.jdart.bytecode.ISUB;
import gov.nasa.jpf.jdart.bytecode.IUSHR;
import gov.nasa.jpf.jdart.bytecode.IXOR;
import gov.nasa.jpf.jdart.bytecode.L2D;
import gov.nasa.jpf.jdart.bytecode.L2F;
import gov.nasa.jpf.jdart.bytecode.L2I;
import gov.nasa.jpf.jdart.bytecode.LADD;
import gov.nasa.jpf.jdart.bytecode.LAND;
import gov.nasa.jpf.jdart.bytecode.LCMP;
import gov.nasa.jpf.jdart.bytecode.LDIV;
import gov.nasa.jpf.jdart.bytecode.LMUL;
import gov.nasa.jpf.jdart.bytecode.LNEG;
import gov.nasa.jpf.jdart.bytecode.LOOKUPSWITCH;
import gov.nasa.jpf.jdart.bytecode.LOR;
import gov.nasa.jpf.jdart.bytecode.LREM;
import gov.nasa.jpf.jdart.bytecode.LSHL;
import gov.nasa.jpf.jdart.bytecode.LSHR;
import gov.nasa.jpf.jdart.bytecode.LSUB;
import gov.nasa.jpf.jdart.bytecode.LUSHR;
import gov.nasa.jpf.jdart.bytecode.LXOR;
import gov.nasa.jpf.jdart.bytecode.NATIVERETURN;
import gov.nasa.jpf.jdart.bytecode.TABLESWITCH;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.NativeMethodInfo;

/**
 * Contains jdart (concrete and symbolic) bytecodes.
 */

/*
 * Refactored version to use the DefaultInstructionFactory -- re-written corina
 */
public class ConcolicInstructionFactory extends gov.nasa.jpf.jvm.bytecode.InstructionFactory {
	
  public static final boolean DEBUG = false;
  public static JPFLogger logger = JPF.getLogger("jdart");
  

  /* (non-Javadoc)
   * @see gov.nasa.jpf.jvm.bytecode.InstructionFactory#executenative(gov.nasa.jpf.vm.NativeMethodInfo)
   */
  @Override
  public Instruction executenative(NativeMethodInfo mi) {
    return new EXECUTENATIVE(mi);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.jvm.bytecode.InstructionFactory#nativereturn()
   */
  @Override
  public Instruction nativereturn() {
    return new NATIVERETURN();
  }
  
  @Override
  public gov.nasa.jpf.jvm.bytecode.ATHROW athrow() {
    return new ATHROW();
  }

	@Override
	public gov.nasa.jpf.jvm.bytecode.IADD iadd() {
	  return new IADD();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IAND iand() {
		return new IAND();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IINC iinc(int localVarIndex, int incConstant) {
		return new IINC(localVarIndex, incConstant);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.ISUB isub() {
		return new ISUB();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IMUL imul() {
		return new IMUL();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.INEG ineg() {
		return new INEG();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IFLE ifle(int targetPc) {
		return new IFLE(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IFLT iflt(int targetPc) {
		return new IFLT(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IFGE ifge(int targetPc) {
		return new IFGE(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IFGT ifgt(int targetPc) {
		return new IFGT(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IFEQ ifeq(int targetPc) {
		return new IFEQ(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IFNE ifne(int targetPc) {
		return new IFNE(targetPc);
	}
	
	@Override
	public gov.nasa.jpf.jvm.bytecode.IF_ICMPGE if_icmpge(int targetPc) {
		return new IF_ICMPGE(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IF_ICMPGT if_icmpgt(int targetPc) {
		return new IF_ICMPGT(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IF_ICMPLE if_icmple(int targetPc) {
		return new IF_ICMPLE(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IF_ICMPLT if_icmplt(int targetPc) {
		return new IF_ICMPLT(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IDIV idiv() {
		return new IDIV();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.ISHL ishl() {
		return new ISHL();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.ISHR ishr() {
		return new ISHR();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IUSHR iushr() {
		return new IUSHR();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IXOR ixor() {
		return new IXOR();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IOR ior() {
		return new IOR();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IREM irem() {
		return new IREM();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IF_ICMPEQ if_icmpeq(int targetPc) {
		return new IF_ICMPEQ(targetPc);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.IF_ICMPNE if_icmpne(int targetPc) {
		return new IF_ICMPNE(targetPc);
	}


	@Override
	public gov.nasa.jpf.jvm.bytecode.FADD fadd() {
		return new FADD();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.FDIV fdiv() {
		return new FDIV();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.FMUL fmul() {
		return new FMUL();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.FNEG fneg() {
		return new FNEG();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.FREM frem() {
		return new FREM();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.FSUB fsub() {
		return new FSUB();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.FCMPG fcmpg() {
		return new FCMPG();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.FCMPL fcmpl() {
		return new FCMPL();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.DADD dadd() {
		return new DADD();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.DCMPG dcmpg() {
		return new DCMPG();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.DCMPL dcmpl() {
		return new DCMPL();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.DDIV ddiv() {
		return new DDIV();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.DMUL dmul() {
		return new DMUL();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.DNEG dneg() {
		return new DNEG();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.DREM drem() {
		return new DREM();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.DSUB dsub() {
		return new DSUB();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LADD ladd() {
		return new LADD();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LAND land() {
		return new LAND();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LCMP lcmp() {
		return new LCMP();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LDIV ldiv() {
		return new LDIV();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LMUL lmul() {
		return new LMUL();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LNEG lneg() {
		return new LNEG();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LOR lor() {
		return new LOR();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LREM lrem() {
		return new LREM();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LSHL lshl() {
		return new LSHL();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LSHR lshr() {
		return new LSHR();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LSUB lsub() {
		return new LSUB();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LUSHR lushr() {
		return new LUSHR();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.LXOR lxor() {
		return new LXOR();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.I2D i2d() {
		return new I2D();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.D2I d2i() {
		return new D2I();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.D2L d2l() {
		return  new D2L();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.I2F i2f() {
		return  new I2F();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.L2D l2d() {
		return  new L2D();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.L2F l2f() {
		return  new L2F();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.F2L f2l() {
		return  new F2L();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.F2I f2i() {
		return  new F2I();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.SwitchInstruction lookupswitch(int defaultTargetPc, int nEntries) {
		return  new LOOKUPSWITCH(defaultTargetPc, nEntries);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.SwitchInstruction tableswitch(int defaultTargetPc, int low, int high) {
		return  new TABLESWITCH(defaultTargetPc, low, high);
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.D2F d2f() {
		return  new D2F();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.F2D f2d() {
		return  new F2D();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.I2B i2b() {
		return  new I2B();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.I2C i2c() {
		return  new I2C();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.I2S i2s() {
		return  new I2S();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.I2L i2l() {
		return  new I2L();
	}

	@Override
	public gov.nasa.jpf.jvm.bytecode.L2I l2i() {
		return  new L2I();
	}
   
}
