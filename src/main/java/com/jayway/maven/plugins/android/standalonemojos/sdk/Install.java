package com.jayway.maven.plugins.android.standalonemojos.sdk;

import java.io.File;

/**
 * Configuration for an Android SDK Install.
 * 
 */
public class Install
{
	/**
	 * @parameter expression="${android.sdk.path}"
	 * 
	 * @deprecated TODO: remove this when sdk is available for sub-classes
	 */
	protected File path;
	/**
	 * @parameter expression="${android.install.os}"
	 */
	protected String os;
	/**
	 * @parameter expression="${android.install.revision}"
	 */
	protected String revision = "06";
	/**
	 * @parameter expression="${android.install.architecture}"
	 */
	protected String architecture;
	/**
	 * @parameter expression="${android.install.repositoryUrl}"
	 */
	protected String repositoryUrl = "http://dl-ssl.google.com/android/repository/";
	/**
	 * @parameter expression="${android.install.sdkUrl}"
	 */
	protected String sdkUrl = "http://dl.google.com/android/";
	/**
	 * @parameter expression="${android.install.downloadDir}"
	 */
	protected String downloadDir = System.getProperty("user.home") + "/.m2/repository/.android-sdk/downloads/";
	/**
	 * @parameter expression="${android.install.overwrite}"
	 */
	protected Boolean overwrite = false;
	/**
	 * @parameter expression="${android.install.agree}"
	 */
	protected Boolean agree = false;
	/**
	 * @parameter expression="${android.install.verbose}"
	 */
	protected Boolean verbose = false;
	/**
	 * @parameter expression="${android.install.obsolete}"
	 */
	protected Boolean obsolete = false;

	public String getOs()
	{
		return os;
	}

	public String getRevision()
	{
		return revision;
	}

	public String getArchitecture()
	{
		return architecture;
	}

	public String getRepositoryUrl()
	{
		return repositoryUrl;
	}

	public String getSdkUrl()
	{
		return sdkUrl;
	}

	public String getDownloadDir()
	{
		return downloadDir;
	}

	public Boolean getOverwrite()
	{
		return overwrite;
	}

	public Boolean getAgree()
	{
		return agree;
	}

	public Boolean getVerbose()
	{
		return verbose;
	}

	public Boolean getObsolete()
	{
		return obsolete;
	}

	public File getPath()
    {
    	return path;
    }
}
