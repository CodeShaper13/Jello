package com.codeshaper.jello.editor;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;

import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Script;

/**
 * Provides a way of compiling the {@link Script} Assets within a project, and
 * an interface for retrieving the {@link Class}es of them.
 */
public class ScriptCompiler {

	private final EditorAssetDatabase database;
	private final File compiledClassDirectory;
	private final List<Class<?>> allCompiledClasses;
	private final URLClassLoader classLoader;

	public ScriptCompiler(File rootProjectFolder, EditorAssetDatabase database) {
		this.database = database;
		this.compiledClassDirectory = new File(rootProjectFolder, "compiledSource");
		this.allCompiledClasses = new ArrayList<Class<?>>();

		this.classLoader = this.createClassLoader();
	}

	/**
	 * Compiles all {@link Script} Assets in the project.
	 */
	public boolean compileProject() {
		this.allCompiledClasses.clear();
		try {
			FileUtils.cleanDirectory(this.compiledClassDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		int errorCount = 0;

		// Compile all classes
		List<Path> paths = this.database.getAllAssetsOfType(Script.class, true);
		for (Path path : paths) {
			Script script = (Script) this.database.getAsset(path);

			boolean success = this.compile(script, compiler);
			if (!success) {
				Debug.logError("Error compiling %s", script.location.getFullPath() + ".java");
			} else {
				try {
					String className = script.getAssetName();
					String fullName;
					
					Path p = script.location.getPath().getParent();
					if(p != null) {
						// Script is not in the default package.
						fullName = p.toString().replace(File.separatorChar, '.') + '.' + className;
					} else {
						fullName = className;
					}
					
					Class<?> cls = Class.forName(fullName, false, this.classLoader);
					this.allCompiledClasses.add(cls);
				} catch (LinkageError e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// This should never happen.
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
		}

		return errorCount == 0;
	}

	public <T extends Annotation> List<Class<?>> getAllScriptsWithAnnotation(Class<T> annotation) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (Class<?> cls : this.allCompiledClasses) {
			if (cls.getAnnotation(annotation) != null) {
				classes.add(cls);
			}
		}
		return classes;
	}

	public <T> List<Class<T>> getAllScriptsOfType(Class<T> type) {
		List<Class<T>> classes = new ArrayList<Class<T>>();
		for (Class<?> cls : this.allCompiledClasses) {
			if (type.isAssignableFrom(cls)) {
				@SuppressWarnings("unchecked")
				Class<T> castCls = (Class<T>) cls;
				classes.add(castCls);
			}
		}
		return classes;
	}

	/**
	 * Compiles the Script into a .class file.
	 * 
	 * @return {@link true} if the compilation was successful, false if there was an
	 *         error.
	 */
	private boolean compile(Script script, JavaCompiler compiler) {		
		// Compile source file.
		int status = compiler.run(
				null, null, null,
				"-d",
				this.compiledClassDirectory.toString(),
				script.location.getFullPath().toString());
		return status == 0;
	}

	/**
	 * Gets the complete path to the directory of where the compiled .class file
	 * will be located for this script. Depending on if the script has been compiled
	 * or not, the directory(s) may not exist.
	 * 
	 * @return
	 */
	private Path getCompiledClassDirectory(Script script) {
		Path pathToScriptFromAssets = script.location.getPath().getParent();

		if (pathToScriptFromAssets == null) {
			return this.compiledClassDirectory.toPath();
		} else {
			return this.compiledClassDirectory.toPath().resolve(pathToScriptFromAssets);
		}
	}

	private URLClassLoader createClassLoader() {
		try {
			URL url = this.compiledClassDirectory.toURI().toURL();
			return URLClassLoader.newInstance(new URL[] { url });
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
