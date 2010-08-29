package com.jayway.maven.plugins.android.standalonemojos.sdk.model;

import java.io.File;

public class Sample
extends Item
{
	public static final String ELEMENT_NAME = NS + "sample";
	
	public String getInstallSubDir()
	{
		return "samples" + File.separator + "android-" + getApiLevel();
	}
}
