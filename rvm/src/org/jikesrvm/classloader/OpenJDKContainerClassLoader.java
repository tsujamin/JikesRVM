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
  
  private static int containerCount = 0;
  
  private final ImmutableEntryHashSetRVM<Atom> checkedReplacementClasses = new ImmutableEntryHashSetRVM<Atom>();
  private final String myName;
  
  public OpenJDKContainerClassLoader() {
    this(CommandLineArgs.getOpenJDKClasses());
  }
  
  public OpenJDKContainerClassLoader(String containerClassPath) {
    if (VM.VerifyAssertions) VM._assert(Options.OpenJDKContainer);
    
    // set name of classloader based on count of intantiated loaders
    myName = "OpenJDKContainerClassLoader-" + Integer.toString(containerCount);
    containerCount++;

    if(containerClassPath == null) VM.sysFail("No OpenJDKContainer classpath provided");
    bootstrapClasspath = containerClassPath;

    if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: created " + myName + " with classpath \"" + bootstrapClasspath + "\"");
  }
  
  @Override
  public synchronized Class<?> loadClass(String className, boolean resolveClass) throws ClassNotFoundException {
    final Atom classNameAtom, replacementClassNameAtom;
    Class<?> loadedClass = null;
    
    // target class must be in correct format for bootstrap class check
    if (className.startsWith("L") && className.endsWith(";"))
      classNameAtom = Atom.findOrCreateAsciiAtom(className.replace('.', '/'));
    else
      classNameAtom = Atom.findOrCreateAsciiAtom('L' + className.replace('.', '/') + ';');
    
    if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: " + classNameAtom + " is a bootstrap class descriptor? " + classNameAtom.isBootstrapClassDescriptor());
    if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: " + classNameAtom + " has been checked for replacement? " + checkedReplacementClasses.contains(classNameAtom));
    
    // load the target class
    loadedClass = super.loadClass(className, false);
    
    try {      
      // Check if there has been an attempt to load the replacement JDK class of this name
      if (classNameAtom.isBootstrapClassDescriptor() && !checkedReplacementClasses.contains(classNameAtom)) {
        checkedReplacementClasses.add(classNameAtom);
        replacementClassNameAtom = toReplacementClassAtom(classNameAtom);
        
        if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: Checking for replacement class for " + classNameAtom + " named " + replacementClassNameAtom);
        
        // Load, resolve and instantiate the replacement class if it exists.
        RVMType replacementClassType = TypeReference.findOrCreate(findClass(replacementClassNameAtom.toString())).resolve();
        replacementClassType.asClass().resolve();
        replacementClassType.asClass().instantiate();
        
        if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: Replacement class " + replacementClassNameAtom + " loaded for " + classNameAtom);
        
      }
    } catch (ClassNotFoundException e) {
      if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: No replacement class for " + classNameAtom);
    }
    
    // Resolve the target class if required
    if (resolveClass)
      resolveClass(loadedClass);
    
    return loadedClass;
  }
  
  private static Atom toReplacementClassAtom(Atom classNameAtom) {
    String className = classNameAtom.toString();
    
    if(className.startsWith("L") && className.endsWith(";"))
      className = className.substring(1, className.length() - 1);
    
    //TODO Adopt better nomenclature for replacement classes
    return Atom.findOrCreateAsciiAtom("LOpenJDK_" + className.replace('.', '_').replace('/', '_') + ';');
  }
  
  @Override
  public String toString() {
    return myName;
  }  
}
