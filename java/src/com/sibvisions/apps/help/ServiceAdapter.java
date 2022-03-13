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

import java.util.HashSet;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Parameter;
import org.restlet.engine.application.CorsFilter;
import org.restlet.routing.Router;
import org.restlet.util.Series;

import com.sibvisions.apps.help.services.ContentService;
import com.sibvisions.apps.help.services.SearchService;
import com.sibvisions.util.type.StringUtil;

/**
 * The <code>ServiceAdapter</code> configures the application and defines global routes.
 * 
 * @author René Jahn
 */
public class ServiceAdapter extends Application
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** the session timeout for sessions created with this adapter. */
	private int iSessionTimeout = -1;
	
	/** whether JSON should be pretty printed. */
	private boolean bJsonPrettyPrint;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overwritten methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 * 
	 * @return the inbound root restlet
	 */
	@Override
	public synchronized Restlet createInboundRoot() 
	{
		Context ctxt = getContext();
		
		Router router = new Router(ctxt);
		router.attach("/api/content", ContentService.class);
		router.attach("/api/search", SearchService.class);
		
		Series<Parameter> serParam = ctxt.getParameters();
		
		String sCorsOrigin = getConfig(serParam, "cors.origin", null);
		
		if (!StringUtil.isEmpty(sCorsOrigin))
		{
            CorsFilter cfRouter = new CorsFilter(ctxt, router);
            cfRouter.setAllowedOrigins(new HashSet<String>(StringUtil.separateList(sCorsOrigin, ",", true)));
            cfRouter.setAllowedCredentials(true);
            cfRouter.setSkippingResourceForCorsOptions(true);
         
            return cfRouter;
		}
		else
		{
			return router;
		}
	}	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Sets the session timeout for this application.
	 * 
	 * @param pTimeout the timeout in minutes
	 */
	public void setSessionTimeout(int pTimeout)
	{
		iSessionTimeout = pTimeout;
	}
	
	/**
	 * Gets the session timeout of this application.
	 * 
	 * @return the timeout in minutes
	 */
	public int getSessionTimeout()
	{
		return iSessionTimeout;
	}
	
	/**
	 * Sets whether json should be pretty printed.
	 * 
	 * @param pPretty <code>true</code> to pretty print json, <code>false</code> otherwise
	 */
	public void setJsonPrettyPrint(boolean pPretty)
	{
		bJsonPrettyPrint = pPretty;
	}
	
	/**
	 * Gets whether json will be pretty printed.
	 * 
	 * @return <code>true</code> if json will be pretty printed, <code>false</code> otherwise
	 */
	public boolean isJsonPrettyPrint()
	{
		return bJsonPrettyPrint;
	}
	
	/**
	 * Gets a value from the configuration.
	 * 
	 * @param pConfig the configuration
	 * @param pKey the key
	 * @param pDefault the default value if no or an empty value was found
	 * @return the found or default value
	 */
	private String getConfig(Series<Parameter> pConfig, String pKey, String pDefault)
	{
		String sValue = pConfig.getFirstValue(pKey);
		
		if (StringUtil.isEmpty(sValue))
		{
			return pDefault;
		}
		
		return sValue;
	}	
	
}	// ServiceAdapter
