package com.tns;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.AssetManager;

public class FileSystem
{
	public static String readAssetFile(AssetManager manager, String path)
	{
		InputStream stream = null;
		try
		{
			stream = manager.open(path);
			return readAll(stream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return "";
		}
		finally
		{
			if(stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String readAll(InputStream inputStream) throws IOException
	{
		StringBuilder text;
		BufferedReader buffReader = null;

		try
		{
			buffReader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			text = new StringBuilder();

			while ((line = buffReader.readLine()) != null)
			{
				text.append(line);
				text.append('\n');
			}
		}
		catch (IOException e){
			return "";
		}
		finally
		{
			if (buffReader != null){
				buffReader.close();
			}
		}

		return text.toString();
	}
	
	public static String readFile(String path)
	{
		File file = new File(path);
		try
		{
			return readText(file);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	public static String readText(File file) throws FileNotFoundException, IOException{
		BufferedInputStream inputStream = null;
		try
		{
			inputStream = new BufferedInputStream(new FileInputStream(file));
			return readAll(inputStream);
		}
		catch (FileNotFoundException e){
			return "";
		}
		finally
		{
			if (inputStream != null){
				inputStream.close();
			}
		}
	}
	
	public static JSONObject readJSONFile(File file) throws IOException, JSONException
	{
		JSONObject object = null;

		BufferedInputStream inputStream = null;
		try
		{
			inputStream = new BufferedInputStream(new FileInputStream(file));
			String content = readAll(inputStream);
			
			if (content != null)
			{
				object = new JSONObject(content);
			}
		}
		finally
		{
			if (inputStream != null)
				inputStream.close();
		}

		return object;
	}
	
	public static String resolveRelativePath(String path, String currentDirectory){
		try{
			URI uri = new URI(currentDirectory);
			String resolvedPath = uri.resolve(path).getPath();
			
			return resolvedPath;
		}
		catch(URISyntaxException e){
			return null;
		}
	}
}
