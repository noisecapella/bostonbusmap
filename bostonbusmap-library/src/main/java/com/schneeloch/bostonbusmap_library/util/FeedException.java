package com.schneeloch.bostonbusmap_library.util;

public class FeedException extends Exception
{
	public FeedException()
	{
		super("The feed is reporting an error");
	}
}
