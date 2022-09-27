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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.rad.type.bean.Bean;
import javax.rad.type.bean.IBean;
import javax.rad.util.TranslationMap;

import com.sibvisions.util.ArrayUtil;
import com.sibvisions.util.type.FileUtil;

/**
 * The <code>EntryHelper</code> class is a utility class for reading help entries/files.
 * 
 * @author René Jahn
 */
public class EntryHelper 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** the configuration. */
	private Config config;
	
	/** the translation. */
	private TranslationMap trans;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Creates a new instance of <code>EntryHelper</code>.
	 * 
	 * @param pConfig the configuration
	 */
	public EntryHelper(Config pConfig)
	{
		config = pConfig;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Sets the translation.
	 * 
	 * @param pTranslation the translation
	 */
	public void setTranslation(TranslationMap pTranslation)
	{
		trans = pTranslation;
	}
	
	/**
	 * Gets the translation.
	 * 
	 * @return the translation
	 */
	public TranslationMap getTranslation()
	{
		return trans;
	}
	
	/**
	 * Search all help entries/files in the structure path of current configuration.
	 * 
	 * @return all found entries
	 */
	public List<IBean> search()
	{
		List<IBean> liFiles = new ArrayUtil<IBean>();
		
		File fiStructure = config.getStructurePath();
		
		HashMap<String, Integer> hmpIDCache = new HashMap<String, Integer>();
		hmpIDCache.put(fiStructure.getAbsolutePath(), Integer.valueOf(-1));
		
		IBean bnHome = new Bean();
		bnHome.put("id", Integer.valueOf(-1));
		bnHome.put("name", "HOME");
		
		File fiHome = new File(fiStructure, "index.html");
		
		if (fiHome.exists())
		{
			bnHome.put("type", "file");
			bnHome.put("url", getURL(fiHome));
		}		
		
		liFiles.add(bnHome);
		
		search(fiStructure, liFiles, hmpIDCache);
		
		return liFiles;
	}
	
	/**
	 * Search all help entries/files in the given path.
	 * @param pPath
	 * @param pFiles
	 * @param pIDCache
	 */
	private void search(File pPath, List<IBean> pFiles, HashMap<String, Integer> pIDCache)
	{
		File[] files = pPath.listFiles(new StructureFilenameFilter());
		
    	if (files != null)
    	{
    		Arrays.sort(files, new FileComparator());
    	
	    	Bean bean;
			
	    	String sAbsPath;
	    	
	    	Integer id;
	    	
			for (int i = 0; i < files.length; i++)
			{
				sAbsPath = files[i].getAbsolutePath();
				
				if (files[i].isDirectory())
				{
					id = Integer.valueOf(pIDCache.size());
					
					pIDCache.put(sAbsPath, id);
	
					String sName = convertName(files[i].getName(), true);
					
					if (trans != null)
					{
						sName = trans.translate(sName);
					}
					
					String sURL = getURL(files[i]);
					
					bean = new Bean();
					bean.put("id", id);
					bean.put("type", "folder");
					bean.put("name", sName);
					bean.put("icon", getIcon(files[i]));
					
					if (sURL != null)
					{
						bean.put("url", sURL);
					}
					
					bean.put("parentID", pIDCache.get(pPath.getAbsolutePath()));
					
					pFiles.add(bean);
					
					search(files[i], pFiles, pIDCache);
				}
				else
				{
					bean = createFileEntry(files[i]);
					bean.put("parentID", pIDCache.get(pPath.getAbsolutePath()));
					
					pFiles.add(bean);
				}
			}
    	}
	}
	
	/**
	 * Creates a file entry for the given file.
	 * 
	 * @param pFile the file
	 * @return the entry as bean
	 */
	public Bean createFileEntry(File pFile)
	{
		String sFileName = pFile.getName();

		String sName = convertName(sFileName, false);
		
		if (trans != null)
		{
			sName = trans.translate(sName);
		}

		Bean bean = new Bean();
		
		String sExtension = FileUtil.getExtension(sFileName);
		
		if ("html".equalsIgnoreCase(sExtension)
			|| "htm".equalsIgnoreCase(sExtension))
		{
			bean.put("type", "file");
		}
		else
		{
			bean.put("type", "download");
		}
		
		bean.put("name", sName);
		bean.put("icon", getIcon(pFile));
		bean.put("url", getURL(pFile));
		
		return bean;
	}
	
	/**
	 * Converts the name of a help entry to a human readable string, e.g.
	 * 01_Common, Information.doc, TK
	 * Standorte$apps.rfid.workscreen.stamm.TKStandorte.html
	 * 
	 * @param pName the original entry name
	 * @param pKeepExtension <code>true</code> to keep the extension in the
	 *                       human readable string, otherwise <code>false</code>
	 * @return the human readable string
	 */
	private String convertName(String pName, boolean pKeepExtension)
	{
		String sName = pName;
		
		//01_Common -> Common
		
		//remove sort order
		int iPos = sName.indexOf('_');
		
		if (iPos > 0)
		{
			sName = sName.substring(iPos + 1);
		}
		
		iPos = sName.indexOf("$");
		
		if (pKeepExtension)
		{
			//TK Standorte$apps.rfid.workscreen.stamm.TKStandorte.html -> TK Standorte.html
			//TK Standorte$apps.rfid.workscreen.stamm.TKStandorte      -> TK Standorte
			if (iPos > 0)
			{
				int iDotPos = sName.lastIndexOf('.');
				
				if (iDotPos > iPos)
				{
					//remove the quick-link ($xxxxx) but keep the extension
					sName = sName.substring(0, iPos) + sName.substring(iDotPos);
				}
				else
				{
					//no extension -> remove the quick-link
					sName = sName.substring(0, iPos);
				}
			}
		}
		else
		{
			//TK Standorte$apps.rfid.workscreen.stamm.TKStandorte.html -> TK Standorte
			//TK Standorte.html      								   -> TK Standorte
			if (iPos > 0)
			{
				//remove quick-link information and extension
				sName = sName.substring(0, iPos);
			}
			else
			{
				//remove file extension
				iPos = sName.lastIndexOf('.');
				
				if (iPos > 0)
				{
					sName = sName.substring(0, iPos);
				}
			}
		}
		
		return sName;
	}	
	
	/**
	 * Returns the icon path for the given file.
	 * 
	 * @param pFile the file.
	 * @return the icon path.
	 */
	private String getIcon(File pFile)
	{
		String sName = convertName(pFile.getName(), false);
		
		String sIcon = null;
		
		if (pFile.isDirectory())
		{
			sIcon = getIcon(sName);
			
			if (sIcon == null)
			{
				sIcon = getIcon("folder");
			}
		}
		else
		{
			//detect the icon with the extension of the file
			int iPos = pFile.getName().lastIndexOf('.');
			
			if (iPos > 0)
			{
				sIcon = getIcon(pFile.getName().substring(iPos + 1));
			}
			
			//use the default "file" icon
			if (sIcon == null) 
			{
				sIcon = getIcon("file");
			}
		}
		
		return sIcon;
	}
	
	/**
	 * Gets the relative path for an icon name.
	 * 
	 * @param pName the name of the icon
	 * @return the relative path of the icon if icon exists, otherwise <code>null</code>
	 */
	private String getIcon(String pName)
	{
		String sHelpPath = config.getHelpPath();
		
		if (sHelpPath.endsWith("/"))
		{
			sHelpPath = sHelpPath.substring(0, sHelpPath.length() - 1);
		}
		
		String sRelativePath = sHelpPath + "/images/tree/" + pName.toLowerCase() + ".png";
		
		if (new File(config.getRootPath(), sRelativePath).isFile())
		{
			return sRelativePath;
		}
		
		return null;
	}
	
	/**
	 * Gets the relative URL of the given file. If path is a directory, the index.html file will be used as file.
	 * 
	 * @param pPath the desired file
	 * @return the URL to the file, accessible via web browser
	 * @see #getServerURL()
	 */
	private String getURL(File pPath)
	{
		if (pPath.isDirectory())
		{
			//check index of directory
			File fiIndex = new File(pPath, "index.html");

			if (fiIndex.exists())
			{
				return getURL(fiIndex);
			}
			else
			{
				return null;
			}
		}

		String sRelativePath = pPath.getAbsolutePath().substring(config.getRootPath().getAbsolutePath().length() + 1).replace("\\", "/");
		
		String[] sPathEntries = sRelativePath.split("/");
		
		StringBuilder sb = new StringBuilder();
		
		for (String sPath : sPathEntries)
		{
			sb.append("/");
			sb.append(encodeURLPart(sPath));
		}
		
		return sb.toString();
	}	
	
	/**
	 * Encodes the part for an URL.
	 * 
	 * @param pURL the url part
	 * @return the encoded part
	 */
	private static String encodeURLPart(String pURL)
	{
		StringBuffer sbfUrl = new StringBuffer();
		
		int iChar;

		try
		{
			byte[] by = pURL.getBytes("ISO-8859-1");
			
			for (int i = 0, anz = by.length; i < anz; i++)
			{
				iChar = by[i];
				
				if ((iChar >= 'a' && iChar <= 'z')
					|| (iChar >= 'A' && iChar <= 'Z')
					|| (iChar >= '0' && iChar <= '9')
					|| iChar == '$'
					|| iChar == '-'
					|| iChar == '_'
					|| iChar == '.'
					|| iChar == '!'
					|| iChar == '\''
					|| iChar == '('
					|| iChar == ')'
					|| iChar == ','
					|| iChar == '/')
				{
					sbfUrl.append((char)iChar);
				}
				else
				{
					sbfUrl.append('%');
					sbfUrl.append(String.valueOf(Integer.toHexString(((iChar & 0xff) + 256))).substring(1));
				}
			}
			
			return sbfUrl.toString();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	//****************************************************************
	// Subclass definition
	//****************************************************************

	/**
	 * The <code>StructureFilenameFilter</code> is the {@link FilenameFilter} implementation
	 * for listing help contents.
	 * 
	 * @author René Jahn
	 */
	private class StructureFilenameFilter implements FilenameFilter
	{
		/**
		 * {@inheritDoc}
		 */
		public boolean accept(File pDir, String pName)
		{ 
			return pName != null
			       && !pName.startsWith(".")
				   && !"index.html".equalsIgnoreCase(pName)
			       && !"structure.css".equalsIgnoreCase(pName);
		}
		  
	}	// StructureFilenameFilter
	
	/**
	 * The <code>FileComparator</code> compares filenames and checks special sort
	 * keys like digits as prefix of the filename e.g. 00_filename.csv
	 * 
	 * @author René Jahn
	 */
	public class FileComparator implements Comparator<File>
	{
		/**
		 * Compares two filenames and takes care of sort prefix. If the prefix is
		 * missing, then the filename will be compared via string compare.
		 * 
		 * @param pFirst a file
		 * @param pSecond another file
		 * @return {@inheritDoc}
		 */
		public int compare(File pFirst, File pSecond)
		{
			Integer iFirst = getSortPrefix(pFirst);
			Integer iSecond = getSortPrefix(pSecond);
			
			
			if (iFirst != null && iSecond != null)
			{
				return iFirst.compareTo(iSecond);
			}
			else if (iFirst == null && iSecond != null)
			{
				return 1;
			}
			else if (iSecond == null && iFirst != null)
			{
				return -1;
			}
			else
			{
				return pFirst.getName().toLowerCase().compareTo(pSecond.getName().toLowerCase());
			}
		}
		
		/**
		 * Gets the sort prefix from a filename e.g. 10 from 10_filename.csv.
		 * 
		 * @param pFile the file to check
		 * @return the sort prefix of the filename or <code>null</code> if the file
		 *         has no numeric prefix
		 */
		private Integer getSortPrefix(File pFile)
		{
			String sName = pFile.getName();
			
			int iPos = sName.indexOf('_');
			
			if (iPos > 0)
			{
				try
				{
					return Integer.valueOf(Integer.parseInt(sName.substring(0, iPos))); 
				}
				catch (Exception e)
				{
					return null;
				}
			}
			
			return null;
		}
		
	}	// FileComparator	

}
