package com.wizarpos.util;

public class AppUtil
{
	public AppUtil() {
	}
	
	public static byte[] removeTailF(byte[] buffer)
	{
		int length = buffer.length;
		for(; length > 0; length--)
		{
			if(buffer[length - 1] != 'F')
				break;
		}
		if(length == buffer.length)
		{
			return buffer;
		}else{
			byte[] destBuffer = new byte[length];
			System.arraycopy(buffer, 0, destBuffer, 0, length);
			return destBuffer;
		}
	}
}
