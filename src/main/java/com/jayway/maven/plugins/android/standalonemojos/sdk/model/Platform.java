package com.jayway.maven.plugins.android.standalonemojos.sdk.model;

import java.io.File;

public class Platform
extends Item
{
	public static final String ELEMENT_NAME = NS + "platform";

	public String getInstallSubDir()
	{
		return "platforms" + File.separator + "android-" + getApiLevel();
	}
}
