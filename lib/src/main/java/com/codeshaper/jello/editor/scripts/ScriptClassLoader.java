package com.codeshaper.jello.editor.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;

public class ScriptClassLoader extends ClassLoader {
	
	private final LinkedList<ILoader> loaders;
	private final Set<String> loadedClasses;
	private final Set<String> unavailableClasses;
    private final ClassLoader parentClassLoader;;

    private ScriptClassLoader() {
    	this.loaders = new LinkedList<>();
    	this.loadedClasses = new HashSet<>();
    	this.unavailableClasses = new HashSet<>();
        this.parentClassLoader = ScriptClassLoader.class.getClassLoader();
    }
    
	public ScriptClassLoader(Collection<File> paths) {
		this();
		
		this.createLoaders(paths);
	}

	public ScriptClassLoader(File... paths) {
		this();
		
		List<File> p = new ArrayList<File>();
		for (File file : paths) {
			p.add(file);
		}
		
		this.createLoaders(p);
	}
	
	public ScriptClassLoader(String... paths) {
		this();
		
		List<File> p = new ArrayList<File>();
		for (String path : paths) {
			p.add(new File(path));
		}
		
		this.createLoaders(p);
	}
	
    @Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (this.loadedClasses.contains(name) || this.unavailableClasses.contains(name)) {
			return super.loadClass(name); // Use default CL cache
		}

		byte[] newClassData = loadNewClass(name);
		if (newClassData != null) {
			this.loadedClasses.add(name);
			return loadClass(newClassData, name);
		} else {
			this.unavailableClasses.add(name);
			return this.parentClassLoader.loadClass(name);
		}
	}
	
	private byte[] loadNewClass(String name) {
		for (ILoader loader : loaders) {
			byte[] data = loader.load(name.replaceAll("\\.", "/") + ".class");
			if (data != null) {
				return data;
			}
		}
		return null;
	}
	
	private void createLoaders(Collection<File> paths) {
		for (File file : paths) {
			if(!file.exists()) {
				System.err.println("File \"" + file.toString() + "\" does not exists");
				continue;
			} else if(file.isDirectory()) {
				this.loaders.add(new DirectoryLoader(file));
			} else {
				try {
					JarFile jarFile = new JarFile(file);
					this.loaders.add(new JarLoader(jarFile));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private Class<?> loadClass(byte[] classData, String name) {
		Class<?> clazz = defineClass(name, classData, 0, classData.length);
		if (clazz != null) {
			if (clazz.getPackage() == null) {
				definePackage(name.replaceAll("\\.\\w+$", ""), null, null, null, null, null, null, null);
			}
			resolveClass(clazz);
		}
		return clazz;
	}
	
	private class DirectoryLoader implements ILoader {

		private final File directory;
		
		public DirectoryLoader(File dir) {
			this.directory = dir;
		}
		
		@Override
		public byte[] load(String filePath) {
			File file = findFile(filePath, this.directory);
			if (file == null) {
				return null;
			}
			
			try {
				return Files.readAllBytes(file.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		private File findFile(String filePath, File classPath) {
			File file = new File(classPath, filePath);
			return file.exists() ? file : null;
		}		
	}
	
	private class JarLoader implements ILoader {

		private final JarFile jarFile;
		
		public JarLoader(JarFile jarFile) {
			this.jarFile = jarFile;
		}
		
		@Override
		public byte[] load(String filePath) {
			ZipEntry entry = this.jarFile.getJarEntry(filePath);
			if (entry == null) {
				return null;
			}
			
			try {
				return IOUtils.toByteArray(jarFile.getInputStream(entry));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}	
		
		@Override
		protected void finalize() throws Throwable {
            this.jarFile.close();
            
			super.finalize();
		}
	}
	
	private interface ILoader {

		byte[] load(String filePath);
	}
}