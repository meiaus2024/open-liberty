/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
package com.ibm.ws.sip.monitor.pmi.inbound;

import com.ibm.websphere.monitor.meters.Meter;
import com.ibm.ws.sip.container.pmi.listener.ApplicationsPMIListener;
import com.ibm.ws.sip.monitor.mxbeans.InboundRequestCountersMXBean;
import com.ibm.ws.sip.monitor.pmi.application.ApplicationModule;
import com.ibm.ws.sip.monitor.pmi.application.ApplicationsModule;

public class InboundRequestCounters extends Meter implements
		InboundRequestCountersMXBean {

	/** Singleton - initialized on activate */
    private static InboundRequestCounters s_singleton = null;  
    
	@Override
	public long getTotalInboundRequests(String appName, String methodName) {
		ApplicationsPMIListener appsModule = ApplicationsModule.getInstance();
		if (appsModule != null) {
			ApplicationModule module = (ApplicationModule) appsModule.getApplicationModule(appName);
			return module.getAppSessionsCounter().getInboundRequestCount(methodName);
		}
		return 0;
	}

	public static InboundRequestCounters getInstance() {
		if (s_singleton == null)
			s_singleton = new InboundRequestCounters();		
		return s_singleton;
	}

}
