/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
package io.openliberty.checkpoint.testapp.jaxws.wsr.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceRef;

import io.openliberty.checkpoint.jaxws.fat.util.TestUtils;
import io.openliberty.checkpoint.testapp.jaxws.wsr.server.stub.People;
import io.openliberty.checkpoint.testapp.jaxws.wsr.server.stub.PeopleService;

// test Class level service injection
@WebServiceRef(name = "services/serviceInjectionClassLevel", type = PeopleService.class)
@WebServlet("/ServiceInjectionClassLevelServlet")
public class ServiceInjectionClassLevelServlet extends HttpServlet {
    private static final long serialVersionUID = -1L;

    public ServiceInjectionClassLevelServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String target = request.getParameter("target");

        InitialContext context;
        try {
            context = new InitialContext();
            Object service = context.lookup("java:comp/env/services/serviceInjectionClassLevel");

            People bill = ((PeopleService) service).getBillPort();
            //workaround for the hard-coded server addr and port in wsdl
            TestUtils.setEndpointAddressProperty((BindingProvider) bill, request.getLocalAddr(), request.getLocalPort());
            out.println(bill.hello(target));
        } catch (NamingException e) {
            throw new ServletException(e);
        }

    }

}
