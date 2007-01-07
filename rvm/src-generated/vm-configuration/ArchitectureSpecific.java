package com.ibm.jikesrvm;

import org.vmmagic.unboxed.WordArray;

import com.ibm.jikesrvm.classloader.VM_Method;
import com.ibm.jikesrvm.opt.OPT_BURS;
import com.ibm.jikesrvm.opt.OPT_DepGraphNode;
import com.ibm.jikesrvm.opt.ir.OPT_IR;

public class ArchitectureSpecific {
  public static class VM_Assembler extends com.ibm.jikesrvm.VM_Assembler {
    public VM_Assembler (int bytecodeSize) {
      super(bytecodeSize, false);
    }
    public VM_Assembler (int bytecodeSize, boolean shouldPrint, VM_Compiler compiler) {
      super(bytecodeSize, shouldPrint, compiler);
    }
    public VM_Assembler (int bytecodeSize, boolean shouldPrint) {
      super(bytecodeSize, shouldPrint);
    }
  }
  public static interface VM_BaselineConstants extends com.ibm.jikesrvm.VM_BaselineConstants {}
  public static final class VM_BaselineExceptionDeliverer extends com.ibm.jikesrvm.VM_BaselineExceptionDeliverer {}
  public static final class VM_BaselineGCMapIterator extends com.ibm.jikesrvm.VM_BaselineGCMapIterator {
    public VM_BaselineGCMapIterator(WordArray registerLocations) {
      super(registerLocations);
    }}
  public static final class VM_CodeArray extends com.ibm.jikesrvm.VM_CodeArray {
    public VM_CodeArray() { super(0);};
    public VM_CodeArray(int size) { super(size);};
    static VM_CodeArray create (int size) { // only intended to be called from VM_CodeArray.factory
      if (VM.runningVM) VM._assert(false);  // should be hijacked
      return new VM_CodeArray(size);
    }
  }
  public static final class VM_Compiler extends com.ibm.jikesrvm.VM_Compiler {
    public VM_Compiler(VM_BaselineCompiledMethod cm) {
      super(cm);
    }}
  public static final class VM_DynamicLinkerHelper extends com.ibm.jikesrvm.VM_DynamicLinkerHelper {}
  public static final class VM_InterfaceMethodConflictResolver extends com.ibm.jikesrvm.VM_InterfaceMethodConflictResolver {}
  public static final class VM_LazyCompilationTrampolineGenerator extends com.ibm.jikesrvm.VM_LazyCompilationTrampolineGenerator {}
  public static final class VM_MachineCode extends com.ibm.jikesrvm.VM_MachineCode {
  //-#if RVM_FOR_IA32
    public VM_MachineCode(ArchitectureSpecific.VM_CodeArray array, int[] bm) {
      super(array, bm);
    }
  //-#endif
  }
  public static final class VM_MachineReflection extends com.ibm.jikesrvm.VM_MachineReflection {}
  public static final class VM_MultianewarrayHelper extends com.ibm.jikesrvm.VM_MultianewarrayHelper {}
  public static final class VM_OutOfLineMachineCode extends com.ibm.jikesrvm.VM_OutOfLineMachineCode {}
  public static final class VM_ProcessorLocalState extends com.ibm.jikesrvm.VM_ProcessorLocalState {}
  public static interface VM_RegisterConstants extends com.ibm.jikesrvm.VM_RegisterConstants {}
  public static final class VM_Registers extends com.ibm.jikesrvm.VM_Registers {}
  public static interface VM_StackframeLayoutConstants extends com.ibm.jikesrvm.VM_StackframeLayoutConstants {}
  public static interface VM_TrapConstants extends com.ibm.jikesrvm.VM_TrapConstants {}
  public static final class VM_JNICompiler extends com.ibm.jikesrvm.jni.VM_JNICompiler {}
  public static final class VM_JNIGCMapIterator extends com.ibm.jikesrvm.jni.VM_JNIGCMapIterator {
    public VM_JNIGCMapIterator(WordArray registerLocations) {
      super(registerLocations);
    }}
  public static final class VM_JNIHelpers extends com.ibm.jikesrvm.jni.VM_JNIHelpers {}
  public static final class OPT_Assembler extends com.ibm.jikesrvm.opt.OPT_Assembler {
  //-#if RVM_FOR_IA32
    public OPT_Assembler(int bcSize, boolean print, OPT_IR ir) {
      super(bcSize, print, ir);
    }
  //-#endif
  }
  public static final class OPT_BURS_Debug extends com.ibm.jikesrvm.opt.OPT_BURS_Debug {}
  public static final class OPT_BURS_STATE extends com.ibm.jikesrvm.opt.OPT_BURS_STATE {
    public OPT_BURS_STATE(OPT_BURS b) {
      super(b);
    }}
  public static class OPT_BURS_TreeNode extends com.ibm.jikesrvm.opt.OPT_BURS_TreeNode {
    public OPT_BURS_TreeNode(OPT_DepGraphNode node) {
      super(node);
    }
    public OPT_BURS_TreeNode(char int_constant_opcode) {
      super(int_constant_opcode);
    }}
  public static final class OPT_CallingConvention extends com.ibm.jikesrvm.opt.OPT_CallingConvention {}
  public static final class OPT_ComplexLIR2MIRExpansion extends com.ibm.jikesrvm.opt.OPT_ComplexLIR2MIRExpansion {}
  public static final class OPT_ConvertALUOperators extends com.ibm.jikesrvm.opt.OPT_ConvertALUOperators {}
  public static final class OPT_FinalMIRExpansion extends com.ibm.jikesrvm.opt.OPT_FinalMIRExpansion {}
  public static final class OPT_MIROptimizationPlanner extends com.ibm.jikesrvm.opt.OPT_MIROptimizationPlanner {}
  public static final class OPT_NormalizeConstants extends com.ibm.jikesrvm.opt.OPT_NormalizeConstants {}
  public static interface OPT_PhysicalRegisterConstants extends com.ibm.jikesrvm.opt.OPT_PhysicalRegisterConstants {}
  public static abstract class OPT_PhysicalRegisterTools extends com.ibm.jikesrvm.opt.OPT_PhysicalRegisterTools {}
  public static final class OPT_RegisterPreferences extends com.ibm.jikesrvm.opt.OPT_RegisterPreferences {}
  public static final class OPT_RegisterRestrictions extends com.ibm.jikesrvm.opt.OPT_RegisterRestrictions {
    public OPT_RegisterRestrictions(OPT_PhysicalRegisterSet phys) {
      super(phys);
    }}
  public static final class OPT_StackManager extends com.ibm.jikesrvm.opt.OPT_StackManager {}
  public static final class VM_OptExceptionDeliverer extends com.ibm.jikesrvm.opt.VM_OptExceptionDeliverer {}
  public static final class VM_OptGCMapIterator extends com.ibm.jikesrvm.opt.VM_OptGCMapIterator {
    public VM_OptGCMapIterator(WordArray registerLocations) {
      super(registerLocations);
    }}
  public static interface VM_OptGCMapIteratorConstants extends com.ibm.jikesrvm.opt.VM_OptGCMapIteratorConstants {}
  public static final class OPT_GenerateMachineSpecificMagic extends com.ibm.jikesrvm.opt.ir.OPT_GenerateMachineSpecificMagic {}
  public static final class OPT_PhysicalDefUse extends com.ibm.jikesrvm.opt.ir.OPT_PhysicalDefUse {}
  public static final class OPT_PhysicalRegisterSet extends com.ibm.jikesrvm.opt.ir.OPT_PhysicalRegisterSet {}
  public static final class OPT_RegisterPool extends com.ibm.jikesrvm.opt.ir.OPT_RegisterPool {
    public OPT_RegisterPool(VM_Method meth) {
      super(meth);
    }}
  public static final class OSR_BaselineExecStateExtractor extends com.ibm.jikesrvm.osr.OSR_BaselineExecStateExtractor {}
  public static final class OSR_CodeInstaller extends com.ibm.jikesrvm.osr.OSR_CodeInstaller {}
  public static final class OSR_OptExecStateExtractor extends com.ibm.jikesrvm.osr.OSR_OptExecStateExtractor {}
  public static final class OSR_PostThreadSwitch extends com.ibm.jikesrvm.osr.OSR_PostThreadSwitch {}
}