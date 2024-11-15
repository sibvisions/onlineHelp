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

import jvx.rad.util.TranslationMap;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.sibvisions.apps.help.services.util.Config;

/**
 * The <code>ContentService</code> is responsible for the help content listing.
 *  
 * @author René Jahn
 */
public class TranslationService extends AbstractService 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Gets a list of all available help entries.
	 * 
	 * @return the entries list (self joined)
	 * @throws Exception if configuration detection fails
	 */
	@Get
	public Representation getTranslation() throws Exception
	{
		Config cfg = createConfiguration();

		TranslationMap tmap = loadTranslation(cfg);
		
		return toInternalRepresentation(tmap);
	}
	
}
