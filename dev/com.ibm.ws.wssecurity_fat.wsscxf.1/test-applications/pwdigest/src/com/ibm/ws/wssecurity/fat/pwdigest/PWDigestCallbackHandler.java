/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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

package com.ibm.ws.wssecurity.fat.pwdigest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

public class PWDigestCallbackHandler implements CallbackHandler {

    private final Map<String, String> passwords = new HashMap<String, String>();

    public PWDigestCallbackHandler() {
        passwords.put("user1", "security");
        passwords.put("user2", "security");
        passwords.put("user3", "badPW3");
        // user4 specifically excluded
        passwords.put("user5", "security");
        passwords.put("user6", "badPw6");
        passwords.put("user22", "security");
        passwords.put("user77", "security");
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        System.out.println("in Handle of server callback");
        for (int i = 0; i < callbacks.length; i++) {
            System.out.println("Server Callback processing");
            WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];

            String pass = passwords.get(pc.getIdentifier());
            System.out.println("Will return : " + pass);
            if (pass != null) {
                pc.setPassword(pass);
                return;
            }
        }

        throw new IOException();
    }

    // Add an alias/password pair to the callback mechanism.
    public void setAliasPassword(String alias, String password) {
        passwords.put(alias, password);
    }
}
