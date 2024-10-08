package com.codeshaper.jello.editor.utils;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;

import org.apache.commons.io.FilenameUtils;

/**
 * A collection of utility methods for working with files.
 */
public class JelloFileUtils {

	/**
	 * Renames a file.
	 * 
	 * @param toBeRenamed The file to rename.
	 * @param newName     The new name for the file, without an extension.
	 * @return the newly renamed file, or {@link null} on error.
	 */
	public static File renameFile(File toBeRenamed, String newName) {
		if (!toBeRenamed.isFile()) {
			return null; // This method isn't meant for directories.
		}

		String extension = FilenameUtils.getExtension(toBeRenamed.getName());

		File newFile = new File(toBeRenamed.getParent(), newName + "." + extension);
		if (newFile.exists()) {
			return null; // Can't rename, file already exists.
		}

		try {
			boolean success = toBeRenamed.renameTo(newFile);
			if(success) {
				return newFile;
			} else {
				return null;
			}
		} catch (SecurityException e) {
			return null;
		}
	}

	/**
	 * //TODO method overview.
	 * 
	 * In a directory with the following files.
	 * 
	 * <pre>
	 * hello.txt
	 * world.txt
	 * world(1).txt
	 * </pre>
	 * 
	 * Calls would return:
	 * 
	 * <pre>
	 * getAvailableFileName(new File("hello.txt")) = "hello(1).txt"
	 * getAvailableFileName(new File("world.txt")) = "world(2).txt"
	 * getAvailableFileName(new File("spam.txt")) = "spam.txt"
	 * </pre>
	 * 
	 * @param file the file to get a unique name for.
	 * @return A file with a unique name.
	 */
	public static File getAvailableFileName(File file) {
		if (!file.exists()) {
			return file; // Name is available, do nothing.
		}

		File pathTo = file.getParentFile();
		String name;
		String fileExtension = null;

		if (file.isDirectory()) {
			name = file.getName(); // Directory name.
		} else {
			name = FilenameUtils.removeExtension(file.getName());
			fileExtension = FilenameUtils.getExtension(file.getName());
		}

		File newName = file;
		int counter = 1;
		do {
			if (file.isDirectory()) {
				newName = new File(pathTo, name + counter);
			} else {
				newName = new File(pathTo, String.format("%s(%s).%s", name, counter, fileExtension));
			}
			counter++;
		} while (newName.exists());

		return newName;
	}
}
