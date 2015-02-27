package com.schneeloch.bostonbusmap_library.util;

public class FeedException extends RuntimeException
{
	public FeedException(String msg, Exception e)
	{
		super(msg, e);
	}
}
