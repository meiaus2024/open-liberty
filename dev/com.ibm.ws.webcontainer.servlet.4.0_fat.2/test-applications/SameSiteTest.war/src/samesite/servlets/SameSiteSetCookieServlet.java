/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package samesite.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This Servlet is invoked with the same request as the SameSiteSetCookieFilter.
 *
 * The SameSiteSetCookieFilter will Call the setHeader and addHeader methods
 * on the HttpServletResponse for the Set-Cookie header.
 */
@WebServlet(urlPatterns = "/TestSetCookie")
public class SameSiteSetCookieServlet extends HttpServlet {

    /**  */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter pw = resp.getWriter();
        pw.println("Welcome to the SameSiteSetCookieServlet!");
        pw.println("Set-Cookie headers:");
        ArrayList<String> headers = new ArrayList<String>(resp.getHeaders("Set-Cookie"));

        for (String header : headers) {
            pw.println(header);
        }
    }

}
