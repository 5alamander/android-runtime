package com.tns;

import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.tns.internal.ExtractPolicy;

import android.content.Context;

public class AssetExtractor
{
	final static String ZIP_FILTER = "assets";
	final static int BUFSIZE = 100000;
	final static String LOGTAG = "AssetExtractor";

	static void Log(String string)
	{
		Log.v(LOGTAG, string);
	}

	static void copyStreams(InputStream is, FileOutputStream fos)
	{
		BufferedOutputStream os = null;
		try
		{
			byte data[] = new byte[BUFSIZE];
			int count;
			os = new BufferedOutputStream(fos, BUFSIZE);
			while ((count = is.read(data, 0, BUFSIZE)) != -1)
			{
				os.write(data, 0, count);
			}
			os.flush();
		}
		catch (IOException e)
		{
			Log("Exception while copying: " + e);
		}
		finally
		{
			try
			{
				os.close();
				is.close();
			}
			catch (IOException e2)
			{
				Log("Exception while closing the stream: " + e2);
			}
		}
	}

	public static void extractAssets(Context context, ExtractPolicy extractPolicy, boolean writableAssets)
	{
		String appRoot = getAppRoot(context);
		
		if (extractPolicy.extract(appRoot))
		{
			extractAssetsHelper(context, extractPolicy, writableAssets);
		}
	}
	
	private static String getAppRoot(Context context)
	{
		return context.getFilesDir().getPath();
	}
	
	private static void extractAssetsHelper(Context context, ExtractPolicy extractPolicy, boolean writableAssets)
	{
		try
		{
			Runtime runtime = Runtime.getRuntime();
			String appRoot = getAppRoot(context);
			
			if (writableAssets)
			{
				// TODO: chmod 777 is potentially dangerous - read/write/execute for everyone
				runtime.exec("chmod 777 " + appRoot);
			}
			
			String packageCodePath = context.getPackageCodePath();
			Log.i(Platform.DEFAULT_LOG_TAG, "extractAssets: from " + packageCodePath);
			File zipFile = new File(packageCodePath);
			ZipFile zip = new ZipFile(packageCodePath);
			Vector<ZipEntry> files = pluginsFilesFromZip(zip);
			int zipFilterLength = ZIP_FILTER.length();

			Enumeration<?> entries = files.elements();

			while (entries.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String entryName = entry.getName();
				if (entryName.endsWith(".dex"))
					continue;
				String path = entryName.substring(zipFilterLength);
				
				File outputFile = new File(appRoot, path);
				outputFile.getParentFile().mkdirs();

				if (extractPolicy.shouldSkip(outputFile, zipFile, entry))
				{
					Log(outputFile.getName() + " already extracted.");
				}
				else
				{
					FileOutputStream fos = null;
					try
					{
						fos = new FileOutputStream(outputFile);
						copyStreams(zip.getInputStream(entry), fos);
						Log("Copied " + entry + " to " + appRoot + "/" + path);
						String curPath = outputFile.getAbsolutePath();
						if (writableAssets)
						{
							do
							{
								runtime.exec("chmod 777 " + curPath);
								curPath = new File(curPath).getParent();
							}
							while (!curPath.equals(appRoot));
						}
					}
					finally
					{
						if (fos != null)
							fos.close();
					}
				}
			}
		}
		catch (IOException e)
		{
			Log.e(Platform.DEFAULT_LOG_TAG, "Error extracting assests: " + e.getMessage());
		}
	}

	private static Vector<ZipEntry> pluginsFilesFromZip(ZipFile zip)
	{
		Vector<ZipEntry> list = new Vector<ZipEntry>();
		Enumeration<?> entries = zip.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry) entries.nextElement();
			if (shouldExtractFile(entry))
			{
				list.add(entry);
			}
		}
		return list;
	}
	
	private static Boolean shouldExtractFile(ZipEntry entry)
	{
		String name = entry.getName();
		
		// extract metadata files
		if (name.startsWith("assets/metadata"))
		{
			return true;
		}
		
		// extract assets/internal
		return name.startsWith("assets/internal");
	}
}