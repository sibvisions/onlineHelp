/*
 * Copyright 2022 SIB Visions GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sibvisions.apps.help;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.engine.adapter.HttpServerHelper;

import com.sibvisions.rad.server.http.HttpContext;
import com.sibvisions.rad.server.http.rest.JSONUtil;
import com.sibvisions.util.log.ILogger.LogLevel;
import com.sibvisions.util.log.LoggerFactory;

/**
 * The <code>ServerServlet</code> initializes default values before the application will be started. 
 * 
 * @author René Jahn
 */
public class ServerServlet extends org.restlet.ext.servlet.ServerServlet
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Overwritten methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates the server instance and initializes global application properties like alive check
	 * and logging.
	 * 
	 * @param pRequest the client request
	 * @return the server helper
	 */
	@Override
	protected HttpServerHelper createServer(HttpServletRequest pRequest)
	{
		try
    	{
			if (LoggerFactory.getInstance(ServerServlet.class.getPackage().getName()).isEnabled(LogLevel.DEBUG))
			{
				JSONUtil.setDumpStreamEnabled(true);
			}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}

    	return super.createServer(pRequest);
	}
	
	/**
	 * Creates the application and sets the resource phase controller if defined in deployment descriptor.
	 * 
	 * @param pContext the application context
	 * @return the application instance
	 */
	@Override
	protected Application createApplication(Context pContext)
	{
		Application app = super.createApplication(pContext);
		
		String sTimeout = getInitParameter("session-timeout");
		
		if (sTimeout != null)
		{
			try
			{
				int iTimeout = Integer.parseInt(sTimeout);
				
				((ServiceAdapter)app).setSessionTimeout(iTimeout);
			}
			catch (Throwable th)
			{
				LoggerFactory.getInstance(ServerServlet.class.getPackage().getName()).debug(th);
			}
		}
		
		String sPrettyPrint = getInitParameter("json.prettyPrint");
		
		if (Boolean.parseBoolean(sPrettyPrint))
		{
			((ServiceAdapter)app).setJsonPrettyPrint(true);
		}
		
		return app;
	}
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public void service(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException 
	{
        HttpContext ctxt = new HttpContext(pRequest, pResponse);
        
        try
        {
            super.service(pRequest, pResponse);
        }
        finally
        {
            ctxt.release();
        }
	}
    
}	// ServerServlet
