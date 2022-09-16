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
package com.sibvisions.apps.help.services;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import javax.rad.type.bean.IBean;

import org.restlet.Request;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.sibvisions.apps.help.services.util.Config;
import com.sibvisions.apps.help.services.util.EntryHelper;
import com.sibvisions.apps.help.services.util.Searcher;
import com.sibvisions.util.ArrayUtil;

/**
 * The <code>ContentService</code> is responsible for the help content listing.
 *  
 * @author René Jahn
 */
public class SearchService extends AbstractService 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** the global search cache. */
	private static Hashtable<String, Searcher> htSearcher = new Hashtable<String, Searcher>();
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Gets a list of all matching entries.
	 * 
	 * @return the found entries list
	 * @throws Exception if configuration detection fails
	 */
	@Get
	public Representation getResult() throws Exception
	{
		Config cfg = createConfiguration();
		
		String sKey = getUniqueApplicationKey();
		
		Searcher search = htSearcher.get(sKey);
		
		if (search == null)
		{
			search = new Searcher(sKey);
			search.setDirectory(cfg.getStructurePath());
			
			search.initialize();
			
			htSearcher.put(sKey, search);
		}
		
		String sSearchTerm = getQuery().getFirstValue("term");
		
		File file;
		
		List<IBean> liResults = new ArrayUtil<IBean>();
		
		EntryHelper eh = new EntryHelper(cfg);
		eh.setTranslation(loadTranslation(cfg));
		
		for (String fileName : search.searchIndex(sSearchTerm, 100))
		{
			file = new File(fileName);
			
			if (file.exists())
			{
				liResults.add(eh.createFileEntry(file));
			}
		}
		
		return toInternalRepresentation(liResults);
	}
	
	
	/**
	 * Returns a String which is unique for the application.
	 * 
	 * @return a String which is unique for the application.
	 * @throws MalformedURLException if the URL is malformed.
	 */
	protected String getUniqueApplicationKey() throws MalformedURLException
	{
		URL urlDocBase = Request.getCurrent().getResourceRef().toUrl();
		
		String sServer = urlDocBase.getAuthority();
		
		if (urlDocBase.getPath() != null)
		{
			int iPos = urlDocBase.getPath().lastIndexOf('/');
			
			sServer += urlDocBase.getPath().substring(0, iPos);
		}
		
		sServer = sServer.replaceAll("/", "_").replaceAll(":", "_");
		
		String sPath = getParameterPath();
		
		// in case of multi-path help -> use path for search
		if (sPath != null)
		{
			sServer += sPath.replace("/", "_");
		}
		
		return sServer;
	}
	
}
