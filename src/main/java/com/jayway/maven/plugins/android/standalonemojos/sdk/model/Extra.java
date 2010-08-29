package com.jayway.maven.plugins.android.standalonemojos.sdk.model;

public class Extra
extends Item
{
	public static final String ELEMENT_NAME = NS + "extra";
	
	public String getInstallSubDir()
	{
		return path;
	}
}
