package com.jayway.maven.plugins.android.standalonemojos.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.shared.io.download.DownloadManager;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jayway.maven.plugins.android.standalonemojos.sdk.model.AddOn;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Archive;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Doc;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Extra;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Item;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Lib;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.License;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Platform;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Repository;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Sample;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Tool;

/**
 * @author aleksandar.vidakovic@gmail.com
 */
public abstract class AbstractSdkMojo
extends AbstractMojo
{
    /**
     * @parameter expression="${android.sdk.path}"
     */
	protected File sdkPath = new File(System.getProperty("user.home") + "/.m2/repository/.android-sdk/");
    /**
     * @parameter expression="${android.sdk.os}" 
     */
	protected String os;
    /**
     * @parameter expression="${android.sdk.revision}"
     */
	protected String revision = "06";
    /**
     * The Fonz
     * @parameter expression="${android.sdk.architecture}"
     */
	protected String architecture;
    /**
     * @parameter expression="${android.sdk.repositoryUrl}"
     */
	protected String repositoryUrl = "http://dl-ssl.google.com/android/repository/";
    /**
     * @parameter expression="${android.sdk.url}"
     */
	protected String sdkUrl = "http://dl.google.com/android/";
    /**
     * @parameter expression="${android.sdk.downloadDir}"
     */
	protected String downloadDir = System.getProperty("user.home") + "/.m2/repository/.android-sdk/downloads/";
    /**
     * @parameter expression="${android.sdk.overwrite}"
     */
	protected Boolean overwrite = false;
    /**
     * @parameter expression="${android.sdk.agree}"
     */
	protected Boolean agree = false;
    /**
     * @parameter expression="${android.sdk.verbose}"
     */
	protected Boolean verbose = false;

	protected Map<String, String> apiLevelToVersion = new HashMap<String, String>();
	
	protected Repository repository;
	
	/**
     * @component role="org.apache.maven.shared.io.download.DownloadManager" role-hint="default"
     * @required
     */
	protected DownloadManager downloadManager;

	/**
     * @component role="org.codehaus.plexus.archiver.UnArchiver" role-hint="tar.gz"
     * @required
     */
	protected UnArchiver tgzUnArchiver;

	/**
     * @component role="org.codehaus.plexus.archiver.UnArchiver" role-hint="zip"
     * @required
     */
	protected UnArchiver zipUnArchiver;
	
	public Repository downloadRepository()
	{
		String file = "repository.xml";
		
		try
		{
        	download(repositoryUrl + file, downloadDir + file, overwrite);

			repository = parse(new FileInputStream(new File(downloadDir + file)));

			for(Platform platform : repository.getPlatforms())
			{
				if(verbose)
				{
					getLog().info("Mapping API level " + platform.getApiLevel() + " to platform version " + platform.getVersion());
				}

				try
		        {
					apiLevelToVersion.put(platform.getApiLevel(), platform.getVersion());
		        }
		        catch (Exception e)
		        {
			        getLog().error(e.toString(), e);
		        }
			}

			return repository;
		}
		catch (Exception e)
		{
			getLog().error(e.getMessage(), e);
		}

		return null;
	}

	public File downloadSdk()
	{
		String file = "android-sdk_r" + revision + "-" + os + (getArchitecture()==null? "" : "_" + getArchitecture()) + "." + ("linux".equals(os)? "tgz" : "zip");
		
		try
		{
			download(sdkUrl + file, downloadDir + file, overwrite);

        	if(verbose)
        	{
    			getLog().info(sdkUrl + file + " downloaded to " + downloadDir + file);
        	}
		}
		catch (Exception e)
		{
			getLog().error(e.getMessage(), e);
		}

		return new File(downloadDir + file);
	}
	
	protected File install(File fromFile, File dest, boolean overwrite, boolean rename)
	{
		// TODO: use later the Maven infrastructure for file traversals; this is a bit of a hack, but works so far

		File tmp = new File(downloadDir, "tmp");
        tmp.mkdirs();
        
        File result = null;
        
		try
        {
	        if(verbose)
	        {
	        	getLog().info("Installing " + fromFile + " to directory " + dest);
	        }
	        
	        UnArchiver unArchiver = null;
	        
	        if(fromFile.getAbsolutePath().endsWith("zip"))
	        {
	        	unArchiver = zipUnArchiver;
	        }
	        else if(fromFile.getAbsolutePath().endsWith("tgz") || fromFile.getAbsolutePath().endsWith("tar.gz"))
	        {
	        	unArchiver = tgzUnArchiver;
	        }
	        
	        unArchiver.setSourceFile(fromFile);
	        unArchiver.setDestDirectory(tmp);
	        unArchiver.setOverwrite(overwrite);
        	unArchiver.extract();
        	
        	File[] extracted = tmp.listFiles();
        	
        	if(extracted!=null && extracted.length>0)
        	{
        		if(rename)
        		{
                	result = dest;
        		}
        		else
        		{
        			result = new File(dest, extracted[0].getName());
        		}
        		
        		// directory exists already; probably from a previous install
        		if(result.exists())
        		{
        			for(File f : extracted[0].listFiles())
        			{
        				if(f.isDirectory())
        				{
                    		FileUtils.copyDirectory(f, new File(result, f.getName()));
        				}
        				else
        				{
                    		FileUtils.copyFile(f, new File(result, f.getName()));
        				}
        			}
        		}
        		else
        		{
            		FileUtils.moveDirectory(extracted[0], result);
        		}
        	}
        }
        catch (Exception e)
        {
			getLog().error(e.getMessage(), e);
        }
        
        return result;
	}
	
	public void writeSourceProperties(Item item)
	{
		writeSourceProperties(getInstallDir(item), item);
	}
		
	public void writeSourceProperties(File dir, Item item)
	{
		File file = new File(dir, "source.properties");
		
		try
        {
			String platformVersion = apiLevelToVersion.get(item.getApiLevel());
			String pr = item.getRevision();
			
			if(pr.length()>1 && pr.startsWith("0"))
			{
				pr = pr.substring(1);
			}
			
			if(verbose)
			{
				getLog().info("Writing source.properties: " + file.getAbsolutePath());
				
				if(item.getApiLevel()!=null && platformVersion!=null)
				{
					getLog().info("API Level " + item.getApiLevel() + " maps to " + platformVersion);
				}
				else
				{
					getLog().info("No API Level defined or no matching platform version found.");
				}
			}
			
			StringBuffer buf = new StringBuffer();
			
		    buf.append("Pkg.UserSrc=false" + "\n"); // TODO: where does this come from?
		    buf.append("Pkg.Desc=" + item.getDescription() + "\n");
		    if(item.getDescriptionUrl()!=null) buf.append("Pkg.DescUrl=" + item.getDescriptionUrl().replaceAll("\\:", "\\\\\\:") + "\n");
		    if(platformVersion!=null) buf.append("Platform.Version=" + platformVersion + "\n");
		    buf.append("Pkg.Revision=" + pr + "\n");
		    if(item.getApiLevel()!=null) buf.append("AndroidVersion.ApiLevel=" + item.getApiLevel() + "\n");

		    getLog().debug("[source.properties] Find out if the property 'Archive.Os' and 'Archive.Arch' are necessary!");
		    // TODO: buf.append("Archive.Os=ANY" + "\n");
		    // TODO: buf.append("Archive.Arch=ANY" + "\n");
		    
		    if(item instanceof Extra)
		    {
			    buf.append("Extra.Path=" + ((Extra)item).getPath() + "\n");
			    // TODO: this covers only downloads from the main repository
				buf.append("Pkg.SourceUrl=" + repositoryUrl.replaceAll("\\:", "\\\\\\:") + "repository.xml");
		    }

		    FileUtils.writeStringToFile(file, buf.toString());
        }
        catch (Exception e)
        {
	        getLog().error(e.toString(), e);
        }
	}

	protected File downloadItem(Item item, String os)
	{
		String file = null;
		
		for (Archive archive : item.getArchives())
		{
			if (os.equals(escapeOs(archive.getOs())))
			{
				file = archive.getUrl();
				break;
			}
		}
		
		if(file==null)
		{
			for (Archive archive : item.getArchives())
			{
				if ("any".equals(archive.getOs()))
				{
					file = archive.getUrl();
					break;
				}
			}
		}

		try
		{
			if (file != null)
			{
				download(repositoryUrl + file, downloadDir + file, overwrite);

	        	if(verbose)
	        	{
	    			getLog().info(item.getDescription() + " (" + repositoryUrl + file + " downloaded to " + downloadDir + file + ")");
	        	}
			}
		}
		catch (Exception e)
		{
			getLog().error(e.getMessage(), e);
		}

    	return new File(downloadDir + file);
	}
	
	protected String escapeOs(String os)
	{
		if(os.toLowerCase(Locale.ENGLISH).contains("win"))
		{
			return "windows";
		}
		else if(os.toLowerCase(Locale.ENGLISH).contains("mac"))
		{
			return "mac";
		}
		else if(os.toLowerCase(Locale.ENGLISH).contains("linux"))
		{
			return "linux";
		}
		else
		{
			return os;
		}
	}
	
	protected boolean isUnix()
	{
		return (os.toLowerCase(Locale.ENGLISH).contains("linux"));
	}
	
	protected boolean isWindows()
	{
		return (os.toLowerCase(Locale.ENGLISH).contains("windows"));
	}
	
	protected boolean isMac()
	{
		return (os.toLowerCase(Locale.ENGLISH).contains("mac"));
	}

	protected void download(String url, String to, boolean overwrite)
	throws Exception
	{
    	File toFile = new File(to);
    	
		if(overwrite || !toFile.exists())
		{
	    	if(verbose)
	    	{
				getLog().info("Downloading: " + url + " to " + to);
	    	}

	    	File f = downloadManager.download(url, new DefaultMessageHolder());
	    	
	    	if(toFile.exists())
	    	{
	    		FileUtils.deleteQuietly(toFile);
	    		toFile = new File(to);
	    	}
	    	
	    	FileUtils.moveFile(f, toFile);
		}
		else if(verbose)
		{
			getLog().warn("File exists: " + toFile);
		}
	}

	private Repository parse(InputStream is)
	throws IOException, SAXException, ParserConfigurationException
	{
		Repository repository = new Repository();
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		Document doc = builder.parse(is);
		
		Element root = doc.getDocumentElement();
		
		NodeList children = root.getChildNodes();
		
		for(int i=0; i<children.getLength(); i++)
		{
			Node node = children.item(i);
			
			if(License.ELEMENT_NAME.equals(node.getNodeName()))
			{
				License license = new License();
				
				license.setText(getTextContent(node));
				
				repository.setLicense(license);
			}
			else
			{
				Item item = parseItem(node);
				
				if(item!=null)
				{
					repository.addItem(item);
				}
			}
		}
		
		return repository;
	}
	
	private Item parseItem(Node parent)
	{
		Item item = null;
		
		if(Platform.ELEMENT_NAME.equals(parent.getNodeName()))
		{
			item = new Platform();
		}
		else if(AddOn.ELEMENT_NAME.equals(parent.getNodeName()))
		{
			item = new AddOn();
		}
		else if(Extra.ELEMENT_NAME.equals(parent.getNodeName()))
		{
			item = new Extra();
		}
		else if(Sample.ELEMENT_NAME.equals(parent.getNodeName()))
		{
			item = new Sample();
		}
		else if(Doc.ELEMENT_NAME.equals(parent.getNodeName()))
		{
			item = new Doc();
		}
		else if(Tool.ELEMENT_NAME.equals(parent.getNodeName()))
		{
			item = new Tool();
		}
		
		if(item!=null)
		{
			NodeList children = parent.getChildNodes();
			
			for(int i=0; i<children.getLength(); i++)
			{
				Node node = children.item(i);
				
				if(Item.NAME.equals(node.getNodeName()))
				{
					item.setName(getTextContent(node));
				}
				else if(Item.API_LEVEL.equals(node.getNodeName()))
				{
					item.setApiLevel(getTextContent(node));
				}
				else if(Item.VERSION.equals(node.getNodeName()))    
				{
					item.setVersion(getTextContent(node));
				}
				else if(Item.VENDOR.equals(node.getNodeName())) 
				{
					item.setVendor(getTextContent(node));
				}
				else if(Item.CODENAME.equals(node.getNodeName())) 
				{
					item.setCodename(getTextContent(node));
				}
				else if(Item.REVISION.equals(node.getNodeName())) 
				{
					item.setRevision(getTextContent(node));
				}
				else if(Item.MIN_TOOLS_REV.equals(node.getNodeName())) 
				{
					item.setMinimumToolsRevision(getTextContent(node));
				}
				else if(Item.DESCRIPTION.equals(node.getNodeName())) 
				{
					item.setDescription(getTextContent(node));
				}
				else if(Item.DESC_URL.equals(node.getNodeName())) 
				{
					item.setDescriptionUrl(getTextContent(node));
				}
				else if(Item.PATH.equals(node.getNodeName())) 
				{
					item.setPath(getTextContent(node));
				}
				else if(Item.ARCHIVES.equals(node.getNodeName()))
				{
					item.setArchives(parseArchives(node));
				}
				else if(Item.LIBS.equals(node.getNodeName()))
				{
					item.setLibs(parseLibs(node));
				}
				else if(Item.LICENSE.equals(node.getNodeName()))
				{
					// TODO: implement this
					getLog().debug("Passing license...");
				}
				else if(Item.OBSOLETE.equals(node.getNodeName()))
				{
					item.setObsolete(true);
				}
			}
		}
		
		return item;
	}

	private List<Archive> parseArchives(Node parent)
	{
		List<Archive> archives = new ArrayList<Archive>();
		
		NodeList children = parent.getChildNodes();
		
		for(int k=0; k<children.getLength(); k++)
		{
			Node archiveNode = children.item(k);
			NodeList archiveNodeChildren = archiveNode.getChildNodes();
			
			Archive archive = new Archive();
			boolean propertySet = false;
			
			NamedNodeMap attributes = archiveNode.getAttributes();
			
			if(attributes!=null)
			{
				for(int j=0; j<attributes.getLength(); j++)
				{
					Node attribute = attributes.item(j);
					
					if(Archive.OS.equals(attribute.getNodeName()))
					{
						archive.setOs(attribute.getFirstChild().getNodeValue());
						propertySet = true;
					}
					else if(Archive.ARCH.equals(attribute.getNodeName()))
					{
						archive.setArchitecture(attribute.getFirstChild().getNodeValue());
						propertySet = true;
					}
				}
			}
			
			for(int i=0; i<archiveNodeChildren.getLength(); i++)
			{
				Node node = archiveNodeChildren.item(i);
				
				if(Archive.CHECKSUM.equals(node.getNodeName()))
				{
					archive.setChecksum(getTextContent(node));
					
					archive.setChecksumType(node.getAttributes().getNamedItem(Archive.TYPE).getFirstChild().getNodeValue());
					propertySet = true;
				}
				else if(Archive.SIZE.equals(node.getNodeName()))
				{
					String value = getTextContent(node);
					
					if(value!=null)
					{
						archive.setSize(Integer.valueOf(value));
						propertySet = true;
					}
				}
				if(Archive.URL.equals(node.getNodeName()))
				{
					archive.setUrl(getTextContent(node));
					propertySet = true;
				}
			}

			if(propertySet)
			{
				archives.add(archive);
				propertySet = false;
			}
		}
		
		return archives;
	}

	private List<Lib> parseLibs(Node parent)
	{
		List<Lib> libs = new ArrayList<Lib>();
		
		// TODO: implement this
		
		return libs;
	}
	
	private String getTextContent(Node node)
	{
		if(node.getFirstChild()!=null && node.getFirstChild().getNodeType()==Node.TEXT_NODE)
		{
			return node.getFirstChild().getNodeValue();
		}
		else
		{
			return node.getNodeValue();
		}
	}

	public String getRepositoryUrl()
    {
    	return repositoryUrl;
    }

	public void setRepositoryUrl(String repositoryUrl)
    {
    	this.repositoryUrl = repositoryUrl;
    }

	public String getSdkUrl()
    {
    	return sdkUrl;
    }

	public void setSdkUrl(String sdkUrl)
    {
    	this.sdkUrl = sdkUrl;
    }

	public String getDownloadDir()
    {
    	return downloadDir;
    }

	public void setDownloadDir(String downloadDir)
    {
    	this.downloadDir = downloadDir;
    }

	public Boolean getOverwrite()
    {
    	return overwrite;
    }

	public void setOverwrite(Boolean overwrite)
    {
    	this.overwrite = overwrite;
    }
	
	public Boolean getVerbose()
    {
    	return verbose;
    }

	public void setVerbose(Boolean verbose)
    {
    	this.verbose = verbose;
    }

	public File getSdkPath()
    {
    	return sdkPath;
    }

	public void setSdkPath(File sdkPath)
    {
    	this.sdkPath = sdkPath;
    }

	public String getOs()
    {
    	return os;
    }

	public void setOs(String os)
    {
    	this.os = os;
    }

	public String getRevision()
    {
    	return revision;
    }

	public void setRevision(String revision)
    {
    	this.revision = revision;
    }

	public String getArchitecture()
    {
    	return architecture;
    }

	public void setArchitecture(String architecture)
    {
    	this.architecture = architecture;
    }

	protected File getInstallRoot()
	{
		return getInstallDir().getParentFile();
	}
	
	protected File getInstallDir()
	{
		File path = new File(sdkPath, "android-sdk-" + os + (architecture!=null? "_" + architecture : ""));
		
		if(!path.exists())
		{
			path.mkdirs();
		}
		
		return path;
	}
	
	protected File getInstallDir(Item item)
	{
		return new File(getInstallDir(), item.getInstallSubDir());
	}

	protected static String getDefaultOperatingSystem()
	{
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

		if(os.indexOf( "win" ) >= 0)
		{
			return "windows";
		}
		else if(os.indexOf( "mac" ) >= 0)
		{
			return "mac";
		}
		else if(os.indexOf( "linux" ) >= 0)
		{
			return "linux";
		}
		else
		{
			return "unknown";
		}
	}
 
	protected static String getDefaultArchitecture()
	{
		String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);

		// NOTE: only mac and linux SDKs have architecture params
		if(!"win".equals(getDefaultOperatingSystem()) && arch.indexOf( "x86" ) >= 0)
		{
			return "86";
		}
		else
		{
			return null;
		}
	}
	
	class RootFileSelector
	implements FileSelector
	{
		private String selection = null;

		public boolean isSelected(FileInfo info) throws IOException
        {
			int pos = info.getName().indexOf("/");
			
        	if((pos==-1 || pos+1==info.getName().length()) && selection==null)
	        {
        		if(verbose)
        		{
                	getLog().info("Selected archive root: " + info.getName());
        		}

        		selection = info.getName();
	        }
	        
	        return true;
        }
		
		public String getSelection()
		{
			return selection;
		}
	}
}
