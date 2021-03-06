/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm.compilers.opt.lir2mir.ia32;

import static org.jikesrvm.compilers.opt.ir.Operators.*;

import static org.jikesrvm.compilers.opt.ir.Operators.INT_LOAD;
import java.util.Enumeration;

import org.jikesrvm.VM;
import org.jikesrvm.compilers.opt.OptimizingCompilerException;
import org.jikesrvm.compilers.opt.Simplifier;
import org.jikesrvm.compilers.opt.driver.CompilerPhase;
import org.jikesrvm.compilers.opt.ir.CondMove;
import org.jikesrvm.compilers.opt.ir.IR;
import org.jikesrvm.compilers.opt.ir.Instruction;
import org.jikesrvm.ia32.ArchConstants;

/**
 * Reduce the number of ALU operators considered by BURS
 */
public class ConvertALUOperators extends CompilerPhase implements ArchConstants {

  @Override
  public final String getName() {
    return "ConvertALUOps";
  }

  /**
   * Return this instance of this phase. This phase contains no
   * per-compilation instance fields.
   * @param ir not used
   * @return this
   */
  @Override
  public CompilerPhase newExecution(IR ir) {
    return this;
  }

  @Override
  public final void perform(IR ir) {
    // Calling Simplifier.simplify ensures that the instruction is
    // in normalized form. This reduces the number of cases we have to
    // worry about (and does last minute constant folding on the off
    // chance we've missed an opportunity...)
    // BURS assumes that this has been done
    for (Enumeration<Instruction> instrs = ir.forwardInstrEnumerator(); instrs.hasMoreElements();) {
      Instruction s = instrs.nextElement();
      Simplifier.simplify(false, ir.regpool, ir.options, s);
    }

    // Pass over instructions
    for (Enumeration<Instruction> e = ir.forwardInstrEnumerator(); e.hasMoreElements();) {
      Instruction s = e.nextElement();

      // BURS doesn't really care, so consolidate to reduce rule space
      switch (s.getOpcode()) {
      case REF_ADD_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_ADD : LONG_ADD);
        break;
      case REF_SUB_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_SUB : LONG_SUB);
        break;
      case REF_NEG_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_NEG : LONG_NEG);
        break;
      case REF_NOT_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_NOT : LONG_NOT);
        break;
      case REF_AND_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_AND : LONG_AND);
        break;
      case REF_OR_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_OR : LONG_OR);
        break;
      case REF_XOR_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_XOR : LONG_XOR);
        break;
      case REF_SHL_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_SHL : LONG_SHL);
        break;
      case REF_SHR_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_SHR : LONG_SHR);
        break;
      case REF_USHR_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_USHR : LONG_USHR);
        break;
      case REF_LOAD_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_LOAD : LONG_LOAD);
        break;
      case REF_STORE_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_STORE : LONG_STORE);
        break;
      case REF_ALOAD_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_ALOAD : LONG_ALOAD);
        break;
      case REF_ASTORE_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_ASTORE : LONG_ASTORE);
        break;
      case REF_MOVE_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_MOVE : LONG_MOVE);
        break;
      case REF_IFCMP_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_IFCMP : LONG_IFCMP);
        break;
      case BOOLEAN_CMP_ADDR_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? BOOLEAN_CMP_INT : BOOLEAN_CMP_LONG);
        break;
      case ATTEMPT_ADDR_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? ATTEMPT_INT : ATTEMPT_LONG);
        break;
      case PREPARE_ADDR_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_LOAD : LONG_LOAD);
        break;
      case PREPARE_INT_opcode:
        s.changeOperatorTo(INT_LOAD);
        break;
      case PREPARE_LONG_opcode:
        s.changeOperatorTo(LONG_LOAD);
        break;
      case INT_2ADDRSigExt_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_MOVE : INT_2LONG);
        break;
      case INT_2ADDRZerExt_opcode:
        if (VM.BuildFor32Addr) {
          s.changeOperatorTo(INT_MOVE);
        }
        break;
      case ADDR_2INT_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? INT_MOVE : LONG_2INT);
        break;
      case ADDR_2LONG_opcode:
        // Note that a 32-bit ADDR_2LONG cannot be changed to an INT_2LONG because
        // INT_2LONG uses the Java conventions for widening by default. Java widening
        // uses sign extension which is not appropriate for addresses. The unboxed
        // types that Jikes RVM uses define that the toLong() methods use zero-extension.
        // For example, for 0xFFFF FFFF (32 bit addresses) the result is supposed to be
        // 0x0000 0000 FFFF FFFF but with sign extension it would be
        // 0xFFFF FFFF FFFF FFFF.
        // Therefore, let BURS rules convert to ADDR_2LONG. The BURS rules can use
        // methods from BURS_Helpers which provide the possibility to emit code for an
        // INT_2LONG that uses zero-extension.
        if (VM.BuildFor64Addr) {
          s.changeOperatorTo(LONG_MOVE);
        }
        break;
      case LONG_2ADDR_opcode:
        s.changeOperatorTo(VM.BuildFor32Addr ? LONG_2INT : LONG_MOVE);
        break;

      case FLOAT_ADD_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_ADD);
        break;
      case DOUBLE_ADD_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_ADD);
        break;
      case FLOAT_SUB_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_SUB);
        break;
      case DOUBLE_SUB_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_SUB);
        break;
      case FLOAT_MUL_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_MUL);
        break;
      case DOUBLE_MUL_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_MUL);
        break;
      case FLOAT_DIV_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_DIV);
        break;
      case DOUBLE_DIV_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_DIV);
        break;
      case FLOAT_REM_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_REM);
        break;
      case DOUBLE_REM_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_REM);
        break;
      case FLOAT_NEG_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_NEG);
        break;
      case DOUBLE_NEG_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(FP_NEG);
        break;

      case INT_COND_MOVE_opcode:
      case REF_COND_MOVE_opcode:
        s.changeOperatorTo(CondMove.getCond(s).isFLOATINGPOINT() ? FCMP_CMOV : (CondMove.getVal1(s).isLong() ? LCMP_CMOV : CMP_CMOV));
        break;
      case FLOAT_COND_MOVE_opcode:
      case DOUBLE_COND_MOVE_opcode:
        s.changeOperatorTo(CondMove.getCond(s).isFLOATINGPOINT() ? FCMP_FCMOV : CMP_FCMOV);
        break;

      case GUARD_COND_MOVE_opcode:
      case LONG_COND_MOVE_opcode:
        OptimizingCompilerException.TODO("Unimplemented conversion" + s);
        break;

      case INT_2FLOAT_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(INT_2FP);
        break;
      case INT_2DOUBLE_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(INT_2FP);
        break;
      case LONG_2FLOAT_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(LONG_2FP);
        break;
      case LONG_2DOUBLE_opcode:
        if (!SSE2_FULL)
          s.changeOperatorTo(LONG_2FP);
        break;
      }
    }
  }
}
