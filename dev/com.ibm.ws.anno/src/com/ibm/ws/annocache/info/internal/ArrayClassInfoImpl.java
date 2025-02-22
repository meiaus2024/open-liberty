/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.annocache.info.internal;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.wsspi.annocache.info.ClassInfo;

public class ArrayClassInfoImpl extends ClassInfoImpl {
    private static final Logger logger = Logger.getLogger("com.ibm.ws.annocache.info");
    private static final String CLASS_NAME = ArrayClassInfoImpl.class.getSimpleName();

    //

    // Array classes are named using the element class name plus "[]".
    // This is a change relative to prior implementations, which used the
    // element name as the array class info name.
    //
    // Use of a distinct name is rather necessary, as method rely on it
    // it to distinguish return types and parameter types, and fields
    // rely on it to distinguish the field value type.

    // Created by the ClassInfoCache; the type class name is probably
    // the element class name plus "[]", but, let the caller choose the name.

    public ArrayClassInfoImpl(String arrayClassName, ClassInfoImpl elementClass) {
        super(arrayClassName, 0, elementClass.getInfoStore());

        String methodName = "<init>";

        this.elementClass = elementClass;

        if (logger.isLoggable(Level.FINER) ) {
            logger.logp(Level.FINER, CLASS_NAME, methodName,
                         "[ {0} ] Created on element [ {1} ]",
                         new Object[] { getHashText(), getElementClass().getHashText() });
        }
    }

    // Changed to pass in the type class name.  That centralizes
    // the calls to obtain the name for a particular class info to
    // the ClassInfoCache.

    @Deprecated
    public ArrayClassInfoImpl(ClassInfoImpl elementClass) {
        this(elementClass.getName() + "[]", elementClass);
    }

    //

    /**
     * <p>Tell if this class info was created to represent an unloadable class.
     * Stub values are assigned, and the interface and superclass values are forced.</p>
     * 
     * @return True if this class object is artificial. Otherwise, false.
     */
    @Override
    public boolean isArtificial() {
        return false;
    }

    // Typing ...

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isJavaClass() {
        return false;
    }

    //

    protected ClassInfoImpl superClass;

    @Override
    public ClassInfoImpl getSuperclass() {
        if (superClass == null) {
            superClass = getInfoStore().getDelayableClassInfo(getSuperclassName());
        }

        return superClass;
    }

    @Override
    public String getSuperclassName() {
        return ClassInfo.OBJECT_CLASS_NAME;
    }

    //

    // The current implementation does not support the package of an array class.

    protected String getPackageNameFromClassName(String className) {
        return null;
    }

    //

    public String getElementClassName() {
        return this.elementClass.getName();
    }

    @Override
    public String getName() {
        return this.elementClass.getName();
    }

    // TODO: Is this correct?  If put into a class info collection, this would
    //       collide with the element class.

    // Note: As a result, array classes MUST NOT be cached.

    @Override
    public String getQualifiedName() {
        return this.elementClass.getName();
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public PackageInfoImpl getPackage() {
        return null;
    }

    //

    protected ClassInfoImpl elementClass;

    public ClassInfoImpl getElementClass() {
        return this.elementClass;
    }

    public ClassInfoImpl getArrayClass() {
        return this.elementClass;
    }

    //   

    @Override
    public List<String> getInterfaceNames() {
        return Collections.emptyList();
    }

    @Override
    public List<ClassInfoImpl> getInterfaces() {
        return Collections.emptyList();
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isAnnotationClass() {
        return false;
    }

    // TODO: Should this be a test of whether the other type is an array type and
    //       the other type's element type is assignable from this array's element type?

    @Override
    public boolean isAssignableFrom(String className) {
        return getElementClass().isAssignableFrom(className);
    }

    @Override
    public boolean isInstanceOf(String className) {
        return getElementClass().isInstanceOf(className);
    }

    //

    @Override
    public List<FieldInfoImpl> getDeclaredFields() {
        return Collections.emptyList();
    }

    @Override
    public List<MethodInfoImpl> getDeclaredConstructors() {
        return Collections.emptyList();
    }

    @Override
    public List<MethodInfoImpl> getDeclaredMethods() {
        return Collections.emptyList();
    }

    @Override
    public List<MethodInfoImpl> getMethods() {
        return Collections.emptyList();
    }

    //

    @Override
    public boolean isFieldAnnotationPresent() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isMethodAnnotationPresent() {
        return false;
    }

    //

    @Override
    public void log(Logger useLogger) {
        String methodName = "log";

        if ( !useLogger.isLoggable(Level.FINER) ) {
            return;
        }
 
        useLogger.logp(Level.FINER, CLASS_NAME, methodName,
                    "Array Class [ {0} ]", getHashText());
        useLogger.logp(Level.FINER, CLASS_NAME, methodName,
                    "  Element ClassInfo [ {0} ]", getElementClass().getHashText());

        // Don't log the details of the element class.
        // An array can have no annotations, so don't log those.
    }

    //

    @Override
    @Trivial
    public void log(TraceComponent tc) {
        if (tc.isDebugEnabled()) {
            Tr.debug(tc, MessageFormat.format("Array Class [ {0} ]", getHashText()));
            Tr.debug(tc, MessageFormat.format("  Element ClassInfo [ {0} ]", getElementClass().getHashText()));
        }

        // Don't log the details of the element class.
        // An array can have no annotations, so don't log those.
    }
}
