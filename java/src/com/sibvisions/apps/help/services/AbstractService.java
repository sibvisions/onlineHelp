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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.rad.util.TranslationMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.restlet.data.Form;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sibvisions.apps.help.services.util.Config;
import com.sibvisions.rad.server.config.ApplicationZone;
import com.sibvisions.rad.server.config.Configuration;
import com.sibvisions.rad.server.config.Configuration.ApplicationListOption;
import com.sibvisions.rad.server.http.rest.JSONUtil;
import com.sibvisions.util.FileSearch;
import com.sibvisions.util.log.LoggerFactory;
import com.sibvisions.util.type.FileUtil;
import com.sibvisions.util.type.StringUtil;

/**
 * The <code>AbstractService</code> is the base service definition.
 * 
 * @author René Jahn
 */
public abstract class AbstractService extends ServerResource 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Gets the JSON representation for the given object.
	 * 
	 * @param pObject any object
	 * @return the JSON representation
	 */
	protected Representation toInternalRepresentation(Object pObject)
	{
        JacksonRepresentation<Object> rep = new JacksonRepresentation<Object>(pObject);
        
        ObjectMapper mapper = rep.getObjectMapper();
        
        JSONUtil.configureObjectMapper(mapper);
        
        return rep;
	}
	
	/**
	 * Gets the value of parameter: <code>path</code>.
	 * 
	 * @return the value of <code>path</code> parameter or <code>null</code> if missing
	 */
	protected String getParameterPath()
	{
		return getQuery().getFirstValue("path");
	}
	
	/**
	 * Creates a new configuration based on the given request.
	 * 
	 * @return the configuration
	 * @throws Exception if path detection fails
	 */
	protected Config createConfiguration() throws Exception
	{
		HttpServletRequest request = ServletUtils.getRequest(getRequest());
		
	    ServletContext ctxt = request.getServletContext();
	    
	    String sRootPath = ctxt.getRealPath("/");
	    
	    if (sRootPath == null)
	    {
	    	try
	    	{
	    		URL url = ctxt.getResource("/");
	    		sRootPath = url.getFile();
	    	}
	    	catch (Exception e)
	    	{
	    		LoggerFactory.getInstance(getClass()).info(e);
	    	}
	    }
	    
	    if (sRootPath != null)
	    {
	    	sRootPath = new File(sRootPath).getCanonicalPath();
	    }
		
	    //ctxt.getContextPath()); --> /onlineHelpServices
	    //ctxt.getRealPath("/")); --> /Users/rjahn/ROOT/tools/eclipse_workspace_photon/.metadata/.plugins/org.eclipse.wst.server.core/tmp13/wtpwebapps/onlineHelpServices/
	    
	    String sHelpPath = getParameterPath();	    
	    
		File fiRoot = null;
		
		if (sRootPath != null)
		{
			FileSearch fs = new FileSearch();
			fs.search(sRootPath, true, "*/structure/");
			
			List<String> liFoundDir = fs.getFoundDirectories();
			
			if (liFoundDir.size() == 1)
			{
				fiRoot = new File(liFoundDir.get(0)).getParentFile();
			}
			else if (liFoundDir.size() > 1)
			{
				if (sHelpPath != null)
				{
					File fiHelpPath = new File(sRootPath, sHelpPath);
					
					if (fiHelpPath.exists() && new File(fiHelpPath, "/structure/").isDirectory())
					{
						fiRoot = fiHelpPath;
					}
				}
			}
		}
		
		//root directory was not found -> maybe .war was not unpacked!
		//try to fallback to application detection
		if (fiRoot == null)
		{
			List<String> liAppNames = Configuration.listApplicationNames(ApplicationListOption.Visible);
			
			if (liAppNames.size() == 1)
			{
				ApplicationZone zone = Configuration.getApplicationZone(liAppNames.get(0));
				
				File fiAppDir = zone.getDirectory();
				
				if (fiAppDir != null)
				{
					File fiHelpDir = new File(fiAppDir, "/help");
					
					if (fiHelpDir.exists() && fiHelpDir.isDirectory())
					{
						FileSearch fs = new FileSearch();
						fs.search(fiHelpDir, true, "*/structure/");

						List<String> liFoundDir = fs.getFoundDirectories();

						if (liFoundDir.size() == 1)
						{
							fiRoot = new File(liFoundDir.get(0)).getParentFile();
						}
						else if (liFoundDir.size() > 1)
						{
							if (sHelpPath != null)
							{
								File fiHelpPath = new File(fiHelpDir, sHelpPath);
								
								if (fiHelpPath.exists() && new File(fiHelpPath, "/strucure/").isDirectory())
								{
									fiRoot = fiHelpPath;
								}
							}
						}
					}
				}
			}
		}
		
		return new Config(fiRoot, new File(fiRoot, "/structure/"), sHelpPath);
	}	
	
	/**
	 * Loads the translation.
	 * 
	 * @param pConfig the configuration
	 * @return the translation
	 */
	protected TranslationMap loadTranslation(Config pConfig)
	{
		Form query = getQuery();
		
		String sLanguage = query.getFirstValue("language");
		
		if (StringUtil.isEmpty(sLanguage))
		{
			sLanguage = ServletUtils.getRequest(getRequest()).getLocale().getLanguage(); 
		}
		
		TranslationMap tmap = loadTranslation(pConfig, sLanguage);
		
		return tmap;
	}
	
	/**
	 * Loads a translation for a given language.
	 * 
	 * @param pConfig the configuration
	 * @param pLanguage the language code e.g. en, de, ru. If the language is
	 *                  <code>null</code> than the language of the default locale will
	 *                  be used
	 * @return the found translation map or <code>null</code> if no translation is available
	 */
	private TranslationMap loadTranslation(Config pConfig, String pLanguage)
	{
		String sLanguage = pLanguage.toLowerCase();
		
		File fiDir = pConfig.getRootPath();
		
		String sDefaultTranslation = "helptranslation.xml";
		
		File fiTrans = new File(fiDir, "/translation/" + sDefaultTranslation);
		
		String sTranslation = fiTrans.getName();
		
		String sExt = FileUtil.getExtension(sTranslation);
		String sName = FileUtil.removeExtension(sTranslation);
		
		TranslationMap map = new TranslationMap();
		
		//load default language file
		Properties prop = loadTranslation(fiTrans);
		
		if (prop != null)
		{
			map.setAsProperties(prop);
		}
		
		//try to load a language file with specific locale
		String sLocalizedTranslation = sName + "_" + sLanguage + "." + sExt;
		
		fiTrans = new File(fiDir, "/translation/" + sLocalizedTranslation);
		
		prop = loadTranslation(fiTrans);
		
		if (prop != null)
		{
			map.setAsProperties(prop);
		}
		
		map.setLanguage(sLanguage);
		
		return map;
	}
	
	/**
	 * Loads a translation resource into a translation map.
	 * 
	 * @param pFile the language resource name
	 * @return the language properties or <code>null</code> if the file was not found
	 */
	private Properties loadTranslation(File pFile)
	{
		InputStream isTranslation = null;
		
		try
		{
			//load the locale specific translation
			isTranslation = new FileInputStream(pFile);
			
			//load the default translation
			if (isTranslation != null)
			{
				Properties properties = new Properties();
				
				//don't use the isTranslation because we have problems with some browser plugin versions
				//(stream closed exceptions)
				properties.loadFromXML(new BufferedInputStream(isTranslation));
				
				return properties;
			}
		}
		catch (Exception e)
		{
			LoggerFactory.getInstance(getClass()).debug("Error loading translation", e);
		}
		finally
		{
			if (isTranslation != null)
			{
				try
				{
					isTranslation.close();
				}
				catch (Exception e)
				{
					//nothing to be done
				}
			}
		}
		
		return null;
	}
	
}
