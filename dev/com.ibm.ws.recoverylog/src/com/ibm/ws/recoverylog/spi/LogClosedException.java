/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
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

package com.ibm.ws.recoverylog.spi;

//------------------------------------------------------------------------------
// Class: LogClosedException
//------------------------------------------------------------------------------
/**
* This exception is generated when an attempt is made to call an operation that
* requires the log to be open but the log is actually closed.
*/
public class LogClosedException extends Exception
{
    public LogClosedException()
    {
        this(null);
    }
    
    public LogClosedException(Throwable cause)
    {
        super(cause);   
    }
}

