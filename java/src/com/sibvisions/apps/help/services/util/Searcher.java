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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.sibvisions.util.ArrayUtil;
import com.sibvisions.util.ThreadHandler;
import com.sibvisions.util.log.LoggerFactory;
import com.sibvisions.util.type.CommonUtil;
import com.sibvisions.util.type.FileUtil;

/**
 * The <code>Searcher</code> creates for all "help" files an index in a specific folder.
 * This index can be used to search for files.
 * 
 * @author René Jahn
 */
public class Searcher
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	
	
	/** The index reader. **/
	private IndexReader indexReader;
	
	/** The index searcher for the index reader. **/
	private IndexSearcher indexSearcher;
	
	/** The standard analyzer. **/
	private Analyzer analyzer;
	
	/** The query parser to parse the search text. **/
	private MultiFieldQueryParser parser;
	
	/** The create index thread. **/
	private Thread thCreateIndex;

	/** The path to the base directory where the files are located. **/
	private File fiDirectory;

	/** The path to the index directory. **/
	private File fiIndexDirectory;
	
	/** A unique application key to create a folder for the index. **/
	private String sUniqueApplicationKey;

	/** If the index is initialized. **/
	private boolean bInitialized = false;

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	
	
	/**
	 * New instance of <code>Searcher</code>.
	 * 
	 * @param pUniqueApplicationKey a unique application key to create a folder
	 */
	public Searcher(String pUniqueApplicationKey)
	{
		sUniqueApplicationKey = pUniqueApplicationKey;
	}
		
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	
	
	/**
	 * Initializes the <code>Searcher</code>. 
	 * 
	 * @throws IOException if accessing the index file failed.
	 */
	public synchronized void initialize() throws IOException
	{		
		if (hasNoOrOldIndex(sUniqueApplicationKey))
		{
			initializeIndexDirectory(sUniqueApplicationKey);
					
			thCreateIndex = ThreadHandler.start(new CreateIndex());
			
			long lNow = System.currentTimeMillis();

			try
			{
				//wait max 30 seconds for index creation
				while (!isIndexCreated() && lNow + 30000 >= System.currentTimeMillis())
				{
					Thread.sleep(100);
				}
			}
			catch (InterruptedException ie)
			{
				LoggerFactory.getInstance(Searcher.class).debug(ie);
			}
		}
	}	
	
	/**
	 * Creates all needed directories in the tmp folder.
	 * 
	 * @param pUniqueApplicationKey a unique key for the application.
	 */
	private void initializeIndexDirectory(String pUniqueApplicationKey)
	{
		if (fiDirectory == null)
		{
			throw new RuntimeException("No base directory set: Use setDirectory");
		}

		File fiSearchIndex = new File(new File(System.getProperty("java.io.tmpdir")), "/ohlineHelpServicesSearchIndex");
		
		if (!fiSearchIndex.exists())
		{
			if (!fiSearchIndex.mkdir())
			{
				throw new RuntimeException("Not able to create directory: " + fiSearchIndex.getAbsolutePath());
			}
		}
		
		File fiIndex = new File(fiSearchIndex, pUniqueApplicationKey);

		if (!fiIndex.exists())
		{
			if (!fiIndex.mkdir())
			{
				throw new RuntimeException("Not able to create directory: " + fiIndex.getAbsolutePath());
			}
		}

		setIndexDirectory(fiIndex);		
	}
	
	/**
	 * Returns true if the index directory has files.
	 * 
	 * @param pUniqueApplicationKey the unique application key
	 * @return true if the index directory has files.
	 * 
	 * @throws IOException if accessing the index file failed.
	 */
	public boolean hasNoOrOldIndex(String pUniqueApplicationKey) throws IOException
	{	
		File fiIndexDir = getIndexDirectory();
		
		if (fiIndexDir == null || !fiIndexDir.exists())
		{
			return true;
		}

		File[] fiContent = fiIndexDir.listFiles();
		
		if (fiContent == null || fiContent.length == 0
			|| fiIndexDir.lastModified() + 86400000 <= System.currentTimeMillis()) // is index directory older than one day (86400000 Milliseconds)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sets the directory where the index files should be stored.
	 * 
	 * @param pPath the directory
	 */
	public void setIndexDirectory(File pPath)
	{
		fiIndexDirectory = pPath;
	}
	
	/**
	 * Gets the directory where the index files should be stored.
	 * 
	 * @return the directory
	 */
	public File getIndexDirectory()
	{
		return fiIndexDirectory;
	}
	
	/**
	 * Sets directory where the files are located.
	 * 
	 * @param pPath the directory
	 */
	public void setDirectory(File pPath)
	{
		fiDirectory = pPath;
	}
	
	/**
	 * Gets the directory where the files are located.
	 * 
	 * @return the directory
	 */
	public File getDirectory()
	{
		return fiDirectory;
	}
	
	/**
	 * Checks if the Thead <code>CreateIndex</code> is alive.
	 * 
	 * @return true if the index is already created.
	 */
	public synchronized boolean isIndexCreated()
	{
		return ThreadHandler.isStopped(thCreateIndex);
	}
	
	/**
	 * Searches the files to the sSearch string.
	 * 
	 * @param sSearch the search string.
	 * @param numberHits the max number of hints.
	 * @return A list with the path to all founded files.
	 * @throws IOException if accessing the index file failed.
	 * @throws ParseException if parsing the input failed.
	 */
	public List<String> searchIndex(String sSearch, int numberHits) throws IOException, ParseException
	{
		if (bInitialized)
		{
			parser.setAllowLeadingWildcard(true);
			
			List<String> files = new ArrayList<String>();
			
			if (!sSearch.startsWith("*"))
			{
				sSearch = "*" + sSearch;
			}
			
			if (!sSearch.endsWith("*"))
			{
				sSearch = sSearch + "*";
			}
				
			Query query = parser.parse(sSearch);
			
			TopDocs results = indexSearcher.search(query, numberHits);
			
			ScoreDoc[] hits = results.scoreDocs;
			
			Document doc;
			
			String sPath;
			
			LoggerFactory.getInstance(Searcher.class).debug("Found hits: ", Integer.valueOf(hits.length));
			
			for (int i = 0; i < hits.length; i++)
			{
				doc = indexSearcher.doc(hits[i].doc);
				
				sPath = doc.get("path");
				
				//No duplicates
				if (!files.contains(sPath))
				{
					files.add(sPath);
				}
			}
	
			return files;
		}
		
		return new ArrayUtil<String>();
	}

	/**
	 * Closes search engine.
	 */
	private void close()
	{
		if (indexReader != null)
		{
			try
			{
				indexReader.close();
			}
			catch (Exception e)
			{
				//nothing to be done
			}
		}
		
		if (analyzer != null)
		{
			analyzer.close();
		}
	}
	
	/**
	 * Deletes the search index completely.
	 * 
	 * @return <code>true</code> if delete was successful, <code>false</code> otherwise
	 */
	public boolean deleteIndex()
	{
		close();
		
		return FileUtil.delete(fiIndexDirectory);
	}
	
	//****************************************************************
	// Subclass definition
	//****************************************************************	
	
	/**
	 * Creates the index in the index directory. The class must be public or package private because of
	 * serialization.
	 * 
	 * @author Stefan Wurm
	 */
	class CreateIndex implements Runnable 
	{
	    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	    // Interface implementation
	    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		/**
		 * {@inheritDoc}
		 */
		public void run()
		{			
			try
			{
				close();

				LoggerFactory.getInstance(Searcher.class).debug("Index directory = ", fiIndexDirectory);
				
				Directory dir = FSDirectory.open(fiIndexDirectory);
				
				analyzer = new StandardAnalyzer(Version.LUCENE_47);
				
				IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

				IndexWriter writer = null;
				
				try
				{
					writer = new IndexWriter(dir, iwc);
					indexDocs(writer, fiDirectory);
				}
				finally
				{
					if (writer != null)
					{
						writer.close();
					}
				}
				
				// IndexReader can only be created after index creation.

				indexReader = DirectoryReader.open(FSDirectory.open(fiIndexDirectory));
				indexSearcher = new IndexSearcher(indexReader);
				analyzer = new StandardAnalyzer(Version.LUCENE_47);		

				parser = new MultiFieldQueryParser(Version.LUCENE_47, new String[] {"contents", "name"}, analyzer);
				
				bInitialized = true;
			}
			catch (IOException e)
			{
				LoggerFactory.getInstance(Searcher.class).debug(e);

				close();
				
				bInitialized = false;
			}
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// User-defined methods
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	

		/**
		 * Creates an index for the given file.
		 * 
		 * @param writer the index writer.
		 * @param file the file.
		 * @throws IOException 
		 */
		private void indexDocs(IndexWriter writer, File file) throws IOException 
		{
			if (file.canRead() && accept(file)) 
			{
				if (file.isDirectory()) 
				{
					String[] files = file.list();
					
					if (files != null) 
					{
						for (int i = 0; i < files.length; i++) 
						{
							indexDocs(writer, new File(file, files[i]));
						}
					}
				} 
				else 
				{					
					LoggerFactory.getInstance(Searcher.class).debug("Index file: ", file);

					FileInputStream fis = null;
				    
                    try
                    {
                        Document doc = new Document();
    
                        Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
                        
                        doc.add(pathField);
                        
                        doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));
                        
                        if (file.getName().endsWith(".pdf"))
                        {
                            RandomAccessFile raf = null;
                            
                            try
                            {
                                raf = new RandomAccessFile(file, "r");
                            
                                PDFParser pdfParser = new PDFParser(raf);  
                                pdfParser.parse();  
        
                                COSDocument cd = pdfParser.getDocument();
        
                                try
                                {
                                    PDFTextStripper stripper = new PDFTextStripper();  
                                    
                                    doc.add(new TextField("contents", stripper.getText(new PDDocument(cd)), Field.Store.YES));
                                }
                                finally
                                {
                                    cd.close();
                                }
                            }
                            finally
                            {
                                CommonUtil.close(raf);
                            }
                        }
                        else if (file.getName().endsWith(".doc")) // Word
                        {
                            //Include Apache POI
                            
    //                          POIFSFileSystem fs = new POIFSFileSystem(fis);  
    //                          WordExtractor extractor = new WordExtractor(fs);  
    //                          String wordText = extractor.getText();                              
                        }
                        else if (file.getName().endsWith(".xls")) // Excel
                        {
                            //Include Apache POI
                            
    //                          POIFSFileSystem fs = new POIFSFileSystem(fis);  
    //                          ExcelExtractor extractor = new ExcelExtractor(fs);  
    //                          String excelText = extractor.getText();                             
                        }
                        else if (file.getName().endsWith(".ppt")) // Powerpoint
                        {
    //                          POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream("filename.ppt"));  
    //                          PowerPointExtractor extractor  = new PowerPointExtractor(fs);  
    //                          String powerText = extractor.getText();                             
                        }
                        else
                        {
                            try
                            {
                                fis = new FileInputStream(file);
                                
                                doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));
                            }
                            catch (FileNotFoundException fnfe)
                            {
                            	fnfe.printStackTrace();
                                // at least on windows, some temporary files raise this exception with an "access denied" message
                                // checking if the file can be read doesn't help
                                return;
                            }
                        }
    
                        doc.add(new StringField("name", file.getName(), Field.Store.YES));
                        
                        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) 
                        {
                            writer.addDocument(doc);
                        }
                        else
                        {
                            writer.updateDocument(new Term("path", file.getPath()), doc);
                        }
                    }
                    finally
                    {
                        CommonUtil.close(fis);
                    }
				}
			}
		}
		
		/**
		 * Gets whether the File should be indexed.
		 * 
		 * @param pFile the file.
		 * @return true if the file should be indexed.
		 */
		private boolean accept(File pFile)
		{
			return pFile != null
				   && !pFile.getName().startsWith(".")
				   && !"structure.css".equalsIgnoreCase(pFile.getName());			
		}
		
	} 	// CreateIndex

} 	// Searcher
