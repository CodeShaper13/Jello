package com.codeshaper.jello.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

	private Utils() { }
	
	public static String readFile(String path) {
		String str = null;
		try {
			str = new String(Files.readAllBytes(Paths.get(path)));
		} catch(IOException e) {
			System.out.println("error reading shader file");
		}
		return str;
	}
}
