package com.javaspi.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.javaspi.exception.ConfigNotFoundException;

public class Util {

	public static Properties getProperties(String context) throws IOException, ConfigNotFoundException {
		if(context == null) {
			throw new ConfigNotFoundException();
		}
		Properties prop = new Properties();
		prop.load(Util.class.getClassLoader().getResourceAsStream("META-INF"+File.separator+"JSPI"+File.separator+context+".properties"));
		return prop;
	}
	
	public static void main(String[] args) throws IOException, ConfigNotFoundException {
		System.out.println(getProperties("adesivo2").get("jspi.url"));
		System.out.println(getProperties(null).get("jspi.url"));
//		System.out.println(System.getProperty("user.dir"));
	}
}
