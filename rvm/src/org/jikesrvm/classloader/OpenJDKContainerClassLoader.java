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

package org.jikesrvm.classloader;

import org.jikesrvm.CommandLineArgs;
import org.jikesrvm.Options;
import org.jikesrvm.VM;
import org.jikesrvm.util.ImmutableEntryHashSetRVM;

public class OpenJDKContainerClassLoader extends BootstrapClassLoader {
  
  private final ImmutableEntryHashSetRVM<Atom> loadedRepacementClasses = new ImmutableEntryHashSetRVM<Atom>();
  
  public OpenJDKContainerClassLoader() {
    if (VM.VerifyAssertions) VM._assert(Options.OpenJDKContainer);
    
    setBootstrapRepositories(CommandLineArgs.getOpenJDKClasses());
  }
  
  public OpenJDKContainerClassLoader(String openJDKClasspath) {
    this();
    setBootstrapRepositories(openJDKClasspath);
  }
  
  @Override
  public synchronized Class<?> loadClass(String className, boolean resolveClass) throws ClassNotFoundException {
    Class<?> loadedClass = super.loadClass(className, false);
    
    if (className.startsWith("L") && className.endsWith(";")) {
      className = className.substring(1, className.length() - 2);
    } 
    
    
    try {
      //TODO Adopt better nomenclature for replacement classes
      Atom replacementClassName = Atom.findAsciiAtom("OpenJDK_" + className.replace('.', '_'));
      
      //Check if there has been an attempt to load the replacement JDK class of this name
      if (!loadedRepacementClasses.contains(replacementClassName) && replacementClassName.isBootstrapClassDescriptor()) {
        if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: Checking for replacement class for " + className);

        loadedRepacementClasses.add(replacementClassName);

        // Load, resolve and instantiate the replacement class if it exists.
        RVMType replacementClassType = TypeReference.findOrCreate(findClass(replacementClassName.toString())).resolve();
        replacementClassType.asClass().resolve();
        replacementClassType.asClass().instantiate();
        
        if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: Replacement class " + replacementClassName + " loaded for " + className);
        
      }
    } catch (ClassNotFoundException e) {
      if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: No replacement class for " + className);
    }
    
    // Resolve the target class if required
    if (resolveClass)
      resolveClass(loadedClass);
    
    return loadedClass;
  }
  
}
