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
package org.jikesrvm.compilers.common;

import static org.jikesrvm.SizeConstants.BYTES_IN_ADDRESS;

import org.jikesrvm.ArchitectureSpecific;
import org.jikesrvm.mm.mminterface.GCMapIterator;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.WordArray;

/**
 * Iterator for stack frames inserted by hardware trap handler.
 * Such frames are purely used as markers.
 * They contain no object references or JSR return addresses.
 */
@Uninterruptible
public final class HardwareTrapGCMapIterator extends GCMapIterator {

  public HardwareTrapGCMapIterator(WordArray registerLocations) {
    this.registerLocations = registerLocations;
  }

  @Override
  public void setupIterator(CompiledMethod compiledMethod, Offset instructionOffset, Address framePtr) {
    this.framePtr = framePtr;
  }

  @Override
  public Address getNextReferenceAddress() {
    // update register locations, noting that the trap handler represented by this stackframe
    // saved all registers into the thread's "exceptionRegisters" object
    //
    Address registerLocation = Magic.objectAsAddress(thread.getExceptionRegisters().gprs);
    for (int i = 0; i < ArchitectureSpecific.ArchConstants.NUM_GPRS; ++i) {
      registerLocations.set(i, registerLocation.toWord());
      registerLocation = registerLocation.plus(BYTES_IN_ADDRESS);
    }
    return Address.zero();
  }

  @Override
  public Address getNextReturnAddressAddress() {
    return Address.zero();
  }

  @Override
  public void reset() {}

  @Override
  public void cleanupPointers() {}

  @Override
  public int getType() {
    return CompiledMethod.TRAP;
  }
}
