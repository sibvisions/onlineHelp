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

import java.util.List;

import javax.rad.util.TranslationMap;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.ClientInfo;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.sibvisions.rad.server.http.rest.JSONUtil;
import com.sibvisions.util.type.CodecUtil;

/**
 * The <code>TestServices</code> class is the test class for help services.
 * 
 * @author René Jahn
 */
public class TestServices 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** the base URL for tests. */
	private static String sBaseUrl = "http://localhost:8085/onlineHelpServices/services/help/";

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Gets the application base URL.
	 * 
	 * @return the base URL
	 */
	protected static String getBaseURL()
	{
		// see build.xml
		String sUrl = System.getProperty("com.sibvisions.apps.help.ServiceAdapter");
		
		if (sUrl != null)
		{
			if (!sUrl.endsWith("/"))
			{
				return sUrl + "/";
			}
			else
			{
				return sUrl;
			}
		}
		
		return sBaseUrl;
	}

	/**
	 * Creates a call request.
	 * 
	 * @param pCommand the command
	 * @param pParameter additional URL parameters (key=value)
	 * @return the request
	 */
	protected ClientResource createRequest(String pCommand, String... pParameter) 
	{
		StringBuilder sbAttribs = new StringBuilder();
		
		String[] sParam;
		
		if (pParameter != null && pParameter.length > 0)
		{
			sbAttribs.append("?");
			
			for (int i = 0, anz = pParameter.length; i < anz; i++)
			{
				if (i > 0)
				{
					sbAttribs.append("&");
				}
				
				sParam = pParameter[i].split("=");
				
				if (sParam.length > 1)
				{
					sbAttribs.append(sParam[0] + "=" + CodecUtil.encodeURLParameter(sParam[1]));
				}
				else
				{
					sbAttribs.append(pParameter[i]);
				}
			}
		}
		
		String sCommand = pCommand;
		
		if (sCommand.startsWith("/"))
		{
			sCommand = sCommand.substring(1);
		}
		
		String sBaseURL = getBaseURL();
		
		ClientResource cres = new ClientResource(sBaseURL + "api/" + pCommand + sbAttribs.toString());
		//see http://stackoverflow.com/questions/6462142/length-required-411-length-required-in-a-restlet-client
		cres.setRequestEntityBuffering(true);
		
		ClientInfo clinf = new ClientInfo();
		clinf.setAgent("JVx.mobile TestClient");
		
		cres.setClientInfo(clinf);
		
		return cres;
		
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Tests
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Tests, getting help contents.
	 * 
	 * @throws Exception if test fails
	 */
	@Test
	public void testGetContent() throws Exception
	{
		JSONUtil.setDumpStreamEnabled(true);
		
		ClientResource cres = createRequest("content", "path=/");
		
		Representation rep = cres.get();
		
		Object obj = JSONUtil.getObject(rep);
		
		Assert.assertNotNull(obj);
		Assert.assertEquals(24, ((List<?>)obj).size());
		
		cres = createRequest("content", "path=/multihelp/help_en");
		
		rep = cres.get();
		
		obj = JSONUtil.getObject(rep);
		
		Assert.assertNotNull(obj);
		Assert.assertEquals(19, ((List<?>)obj).size());
	}
	
	/**
	 * Tests, search contents.
	 * 
	 * @throws Exception if test fails
	 */
	@Test
	public void testSearch() throws Exception
	{
		JSONUtil.setDumpStreamEnabled(true);
		
		ClientResource cres = createRequest("search", "path=/", "term=shows");
		
		Representation rep = cres.get();
		
		Object obj = JSONUtil.getObject(rep);
		
		Assert.assertNotNull(obj);
		Assert.assertEquals(2, ((List<?>)obj).size());
		

		cres = createRequest("search", "path=/multihelp/help_en", "term=shows");
		
		rep = cres.get();
		
		obj = JSONUtil.getObject(rep);
		
		Assert.assertNotNull(obj);
		Assert.assertEquals(2, ((List<?>)obj).size());
	}
	
	/**
	 * Tests, search contents.
	 * 
	 * @throws Exception if test fails
	 */
	@Test
	public void testTanslation() throws Exception
	{
		JSONUtil.setDumpStreamEnabled(true);
		
		ClientResource cres = createRequest("translation", "path=/");
		
		Representation rep = cres.get();
		
		TranslationMap tmap = (TranslationMap)JSONUtil.getObject(rep, TranslationMap.class);
		
		Assert.assertNotNull(tmap);
		Assert.assertEquals("de", tmap.getLanguage());
		Assert.assertFalse(tmap.getAsProperties().isEmpty());
		
		cres = createRequest("translation", "path=/", "language=en");
		
		rep = cres.get();
		
		tmap = (TranslationMap)JSONUtil.getObject(rep, TranslationMap.class);
		
		Assert.assertNotNull(tmap);
		Assert.assertEquals("en", tmap.getLanguage());
		Assert.assertTrue(tmap.getAsProperties().isEmpty());
	}
	
}
