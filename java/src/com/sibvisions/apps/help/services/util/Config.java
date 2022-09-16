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
package com.sibvisions.apps.help.services.util;

import java.io.File;

/**
 * The <code>Config</code> is a container for path configuration. 
 * 
 * @author René Jahn
 */
public class Config
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** the root path. */
	private File fiRootPath;
	
	/** the structure path. */
	private File fiStructurePath;
	
	/** the help path. */
	private String sHelpPath;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Creates a new instance of <code>Config</code>.
	 * 
	 * @param pRootPath the root path
	 * @param pStructurePath the structure path
	 * @param pHelpPath the help path
	 */
	public Config(File pRootPath, File pStructurePath, String pHelpPath)
	{
		fiRootPath = pRootPath;
		fiStructurePath = pStructurePath;
		
		sHelpPath = pHelpPath;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Gets the root directory.
	 * 
	 * @return the directory
	 */
	public File getRootPath()
	{
		return fiRootPath;
	}
	
	/**
	 * Gets the help structure directory.
	 * 
	 * @return the directory
	 */
	public File getStructurePath()
	{
		return fiStructurePath;
	}
	
	/** 
	 * Gets the help path. This is the resource path where the root path can be found.
	 * 
	 * @return the help path
	 */
	public String getHelpPath()
	{
		return sHelpPath;
	}
	
}	// Config

