package com.jayway.maven.plugins.android.standalonemojos.sdk;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal sdkClean
 * @requiresProject false
 * 
 * @author aleksandar.vidakovic@gmail.com
 */
public class SdkCleanMojo
extends AbstractSdkMojo
{
	public void execute() 
	throws MojoExecutionException, MojoFailureException
    {
		try
        {
			getLog().warn("Cleaning :" + sdkPath);
			
			FileUtils.deleteDirectory(sdkPath);
			sdkPath.mkdir();
        }
        catch (Exception e)
        {
	        throw new MojoExecutionException("Could not clean Android SDK directory: " + sdkPath, e);
        }
    }
}
