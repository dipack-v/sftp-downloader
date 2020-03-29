package com.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class DownloaderProps {

	private static Properties properties = new Properties();

	static {
		InputStream file;
		try {
			file = new FileInputStream(new File("./downloader.properties"));
			properties.load(file);
		} catch (FileNotFoundException exp) {
			exp.printStackTrace();
		} catch (IOException exp) {
			exp.printStackTrace();
		}
	}

	public static String getPropertyValue(String propertyKey) {
		if (propertyKey != null && !propertyKey.isEmpty()) {
			return properties.getProperty(propertyKey);
		}
		return null;
	}
	
	public static String[] getPropertyValues(String propertyKey) {
		if (propertyKey != null && !propertyKey.isEmpty()) {
			return StringUtils.split(properties.getProperty(propertyKey), ",");
		}
		return null;
	}

}
