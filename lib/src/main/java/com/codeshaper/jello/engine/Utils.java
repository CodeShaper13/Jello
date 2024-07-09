package com.codeshaper.jello.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

	private Utils() { }
	
	public static String readFile(Path path) {
		String str = null;
		try {
			str = new String(Files.readAllBytes(path));
		} catch(IOException e) {
			System.out.println("error reading shader file");
		}
		return str;
	}
	
	public static String readFile(String path) {
		return Utils.readFile(Paths.get(path));
	}
}
