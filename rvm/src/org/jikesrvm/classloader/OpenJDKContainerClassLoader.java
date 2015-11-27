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

/**
 * Implements a bootstrap classloader that can be used to create a container within Jikes that uses OpenJDK
 * 
 * When bootstrap classes are loaded, such as java.lang.Object, the classloader searches for replacement classes
 * that implement missing functionality (such as native methods implemented by hotspot)
 * 
 * @author Benjamin George Roberts
 */
public class OpenJDKContainerClassLoader extends BootstrapClassLoader {
  
  private static int containerCount = 0;
  
  private final ImmutableEntryHashSetRVM<Atom> checkedReplacementClasses = new ImmutableEntryHashSetRVM<Atom>();
  private final String myName;
  
  /**
   * Instantiate a container classloader using the OpenJDK classpath provided by the commandline
   */
  public OpenJDKContainerClassLoader() {
    this(CommandLineArgs.getOpenJDKClasses());
  }
  
  /**
   * Instantiate a container classloader
   * @param containerClassPath Classpath container will load classes from
   */
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
    Atom classNameAtom, replacementClassNameAtom = null;
    Class<?> loadedClass = null;
    
    // target class must be in correct format for bootstrap class check
    if (className.startsWith("L") && className.endsWith(";"))
      classNameAtom = Atom.findOrCreateAsciiAtom(className.replace('.', '/'));
    else
      classNameAtom = Atom.findOrCreateAsciiAtom('L' + className.replace('.', '/') + ';');
    
    if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: " + classNameAtom + " is a bootstrap class descriptor? " + classNameAtom.isBootstrapClassDescriptor());
    if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: " + classNameAtom + " has been checked for replacement? " + checkedReplacementClasses.contains(classNameAtom));
    
    // load the target class
    RVMType loadedType = loaded.get(className);
    if(loadedType != null)
      loadedClass = loadedType.getClassForType();
    else
      loadedClass = findClass(className);
    
    try {      
      // Check if there has been an attempt to load the replacement JDK class of this name
      if (classNameAtom.isBootstrapClassDescriptor() && !checkedReplacementClasses.contains(classNameAtom)) {
        checkedReplacementClasses.add(classNameAtom);
        replacementClassNameAtom = toReplacementClassAtom(classNameAtom);
        
        if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: Checking for replacement class for " + classNameAtom + " named " + replacementClassNameAtom);
        
        // Load, resolve and instantiate the replacement class if it exists.
        RVMClass replacementClass = TypeReference.findOrCreate(findClass(replacementClassNameAtom.toString())).resolve().asClass();
        replacementClass.resolve();
        replacementClass.instantiate();
        replacementClass.initialize();
        
        if(VM.TraceClassLoading) VM.sysWriteln("OpenJDKContainer: Replacement class " + replacementClassNameAtom + " loaded for " + classNameAtom);
        
      }
    } catch (ClassNotFoundException e) {
      if(VM.TraceClassLoading) 
        VM.sysWriteln("OpenJDKContainer: No replacement class for " + classNameAtom);
      // e.printStackTrace();
    } catch(NoSuchMethodError e) {
      VM.sysWriteln("OpenJDKContainer: Failed to load replacement class " + replacementClassNameAtom + " for " + classNameAtom);
      VM.sysWriteln(e.toString());
      e.printStackTrace();
      throw new ClassNotFoundException("Failed to load replacement class " + replacementClassNameAtom, e);
    }
    
    // Resolve the target class if required
    if (resolveClass)
      resolveClass(loadedClass);
    
    return loadedClass;
  }
  
  /**
   * Return an atom representing the name of the corresponding replacement class.
   * This defines the naming nomenclature that all replacement classes must conform to.
   * 
   * Example:
   * If "Ljava/lang/Object;" were to be loaded, the classloader would check to see if
   * "Lopenjdk6/java/lang/Object;" were present in the classpath.
   * @param classNameAtom Name of class to find replacement of
   * @return Name of corresponding replacement class
   */
  private static Atom toReplacementClassAtom(Atom classNameAtom) {
    String className = classNameAtom.toString();
    
    if(className.startsWith("L") && className.endsWith(";"))
      className = className.substring(1, className.length() - 1);
    
    return Atom.findOrCreateAsciiAtom("Lopenjdk6/" + className + ';');
  }
  
  @Override
  public String toString() {
    return myName;
  }  
}
