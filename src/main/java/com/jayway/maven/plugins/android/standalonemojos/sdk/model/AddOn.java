package com.jayway.maven.plugins.android.standalonemojos.sdk.model;

import java.io.File;
import java.util.Locale;

public class AddOn
extends Item
{
	public static final String ELEMENT_NAME = NS + "add-on";

	public String getInstallSubDir()
	{
		String name = "addon_" + getName() + "_" + getVendor() + "_" + getApiLevel();
		name = name.replaceAll("\\ ", "_");
		name = name.replaceAll("\\.", "");
		name = name.toLowerCase(Locale.ENGLISH);
		
		return "add-ons" + File.separator + name;
	}
}
