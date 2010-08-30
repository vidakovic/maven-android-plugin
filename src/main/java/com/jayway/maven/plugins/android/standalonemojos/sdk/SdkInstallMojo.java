package com.jayway.maven.plugins.android.standalonemojos.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.jayway.maven.plugins.android.standalonemojos.sdk.model.AddOn;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Doc;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Extra;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Platform;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Repository;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Sample;
import com.jayway.maven.plugins.android.standalonemojos.sdk.model.Tool;

/**
 * @goal sdkInstall
 * @requiresProject false
 * 
 * @author aleksandar.vidakovic@gmail.com
 */
public class SdkInstallMojo
extends AbstractSdkMojo
{
	public void execute() 
	throws MojoExecutionException, MojoFailureException
    {
		// step 1: refresh and parse repository
		Repository repository = downloadRepository();
		
		getLog().info("\n\n" + repository.getLicense().getText());
		getLog().info("\n\n");
		
		// step 2: EULA agreement
		try
        {
	        if(!agree)
	        {
	        	getLog().warn("Do you agree with the EULA? [y/N]");

	        	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	        	
	        	String answer = in.readLine();
	        	
	        	if(answer!=null && ("yes".equals(answer.toLowerCase()) || "y".equals(answer.toLowerCase())))
	        	{
	        		getLog().info("You agreed with the EULA!");
	        	}
	        	else
	        	{
	        		getLog().error("You have to agree with the EULA!");
	        		return;
	        	}
	        }
        }
        catch (IOException e)
        {
	        getLog().error(e);
	        return;
        }
		
		// step 3: download the SDK
		File sdkFile = downloadSdk();
		
		// step 4: install SDK
		install(sdkFile, getInstallRoot(), overwrite, false);
		
		// platform
		for(Platform platform : repository.getPlatforms())
		{
			File file = downloadItem(platform, os);
			
			if(file!=null && file.exists())
			{
				install(file, getInstallDir(platform), overwrite, true);
			}
		}
		
		// add-on
		for(AddOn addOn : repository.getAddOns())
		{
			File file = downloadItem(addOn, os);

			if(file!=null && file.exists())
			{
				install(file, getInstallDir(addOn), overwrite, true);
				writeSourceProperties(addOn);
			}
		}
		
		// extra
		for(Extra extra : repository.getExtras())
		{
			File file = downloadItem(extra, os);
			
			if(file!=null && file.exists())
			{
				File result = install(file, getInstallDir(extra), overwrite, true);
				writeSourceProperties(result, extra);
			}
		}
		
		// sample
		for(Sample sample : repository.getSamples())
		{
			File file = downloadItem(sample, os);

			if(file!=null && file.exists())
			{
				install(file, getInstallDir(sample), overwrite, true);
			}
		}
		
		// doc
		for(Doc doc : repository.getDocs())
		{
			File file = downloadItem(doc, os);

			if(file!=null && file.exists())
			{
				install(file, getInstallDir(doc), overwrite, true);
			}
		}
		
		// tool
		for(Tool t : repository.getTools())
		{
			File file = downloadItem(t, os);

			if(file!=null && file.exists())
			{
				install(file, getInstallDir(t), overwrite, true);
			}
		}
    }
}
