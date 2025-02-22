/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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
package io.openliberty.data.internal.persistence.metadata;

import com.ibm.websphere.csi.J2EEName;
import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.container.service.metadata.extended.IdentifiableComponentMetaData;
import com.ibm.ws.runtime.metadata.ComponentMetaData;
import com.ibm.ws.runtime.metadata.MetaDataImpl;
import com.ibm.ws.runtime.metadata.ModuleMetaData;

/**
 * Dummy ComponentMetaData for a module or application.
 * This metadata is used as a ComponentMetaData for an application artifact,
 * such as when the repository is defined in a library of the application
 * rather than a web or ejb component.
 */
@Trivial
public class DataComponentMetaData extends MetaDataImpl //
                implements ComponentMetaData, IdentifiableComponentMetaData {

    public final ClassLoader classLoader;
    private final String identifier;
    private final ModuleMetaData moduleMetadata;

    public DataComponentMetaData(String identifier,
                                 ModuleMetaData metadata,
                                 ClassLoader classLoader) {
        super(0);

        this.classLoader = classLoader;
        this.identifier = identifier;
        this.moduleMetadata = metadata;
    }

    @Override
    @Trivial
    public J2EEName getJ2EEName() {
        return moduleMetadata.getJ2EEName();
    }

    @Override
    public ModuleMetaData getModuleMetaData() {
        return moduleMetadata;
    }

    @Override
    public String getName() {
        return moduleMetadata.getJ2EEName().toString();
    }

    @Override
    public String getPersistentIdentifier() {
        return identifier;
    }
}