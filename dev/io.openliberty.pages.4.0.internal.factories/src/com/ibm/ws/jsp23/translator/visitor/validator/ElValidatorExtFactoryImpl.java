/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.jsp23.translator.visitor.validator;

import org.osgi.service.component.annotations.Component;

import com.ibm.ws.jsp.translator.visitor.validator.ElValidatorExt;
import com.ibm.ws.jsp.translator.visitor.validator.ElValidatorExtFactory;

@Component(property = { "service.vendor=IBM" })
public class ElValidatorExtFactoryImpl implements ElValidatorExtFactory {

    private static final ELValidatorExtImpl eve = new ELValidatorExtImpl();

    @Override
    public ElValidatorExt getELValidatorExt() {
        return eve;
    }

}
