package com.codeshaper.jello.editor.scripts;

import java.awt.Component;
import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.editor.EditorAssetDatabase;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Script;
import com.codeshaper.jello.engine.rendering.Renderer;

/**
 * Provides a way of compiling the {@link Script} Assets within a project, and
 * an interface for retrieving the {@link Class}es of them.
 */
public class ScriptCompiler {

	private final EditorAssetDatabase database;
	private final File compiledClassDirectory;
	private final List<CompiledScript> allCompiledClasses;

	private ClassLoader classLoader;

	public ScriptCompiler(File rootProjectFolder, EditorAssetDatabase database) {
		this.database = database;
		this.compiledClassDirectory = new File(rootProjectFolder, "compiledSource");
		this.allCompiledClasses = new ArrayList<CompiledScript>();
	}

	/**
	 * Compiles all {@link Script} Assets in the project.
	 */
	public boolean compileProject() {
		this.allCompiledClasses.clear();

		if (this.compiledClassDirectory.exists()) {
			try {
				FileUtils.cleanDirectory(this.compiledClassDirectory);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.compiledClassDirectory.mkdirs();
		}
		
		this.classLoader = new ScriptClassLoader(this.compiledClassDirectory);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		int errorCount = 0;

		// Compile all classes.

		Collection<File> allFiles = FileUtils.listFiles(this.database.assetsFolder.toFile(), new String[] { "java" },
				true);
		for (File sourceFile : allFiles) {
			boolean success = this.compile(sourceFile, compiler);
			if (!success) {
				Debug.logError("Error compiling %s", sourceFile.toString());
			} else {
				try {
					String className = FilenameUtils.removeExtension(sourceFile.getName());
					String fullName;

					Path p = this.database.assetsFolder.relativize(sourceFile.toPath()).getParent();
					if (p != null) {
						// Script is not in the default package.
						fullName = p.toString().replace(File.separatorChar, '.') + '.' + className;
					} else {
						// Script is in the default package.
						fullName = className;
					}

					Class<?> cls = Class.forName(fullName, false, this.classLoader);
					
					CompiledScript cs = new CompiledScript(sourceFile, cls);					
					this.allCompiledClasses.add(cs);
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

	/**
	 * Gets all Script classes that have a matching annotation.
	 * 
	 * @param annotation the annotation to check for
	 * @return a {@link List} of classes with an annotation of type
	 *         {@code annotation}
	 */
	public <T extends Annotation> List<Class<?>> getScriptsWithAnnotation(Class<T> annotation) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (CompiledScript cs : this.allCompiledClasses) {
			if (cs.compiledClass.getAnnotation(annotation) != null) {
				classes.add(cs.compiledClass);
			}
		}
		return classes;
	}

	/**
	 * Gets all Script classes that are, or are a subtype of {@code type}. The
	 * passed class will not necessarily be included in the list, if it was not
	 * defined in the /assets folder. An example of this would be defining a
	 * {@link Component} that extends {@link Renderer} and, and passing
	 * {@link Renderer} to this method.
	 * 
	 * @param type
	 * @return a {@link List} of classes that are, or are a subtype of {@code type}
	 */
	public <T> List<Class<T>> getScriptsOfType(Class<T> type) {
		List<Class<T>> classes = new ArrayList<Class<T>>();
		for (CompiledScript cs : this.allCompiledClasses) {
			if (type.isAssignableFrom(cs.compiledClass)) {
				@SuppressWarnings("unchecked")
				Class<T> castCls = (Class<T>) cs.compiledClass;
				classes.add(castCls);
			}
		}
		return classes;
	}
	
	/**
	 * 
	 * @param cls
	 * @return
	 */
	public File getSourceFile(Class<?> cls) {
		for(CompiledScript cs : this.allCompiledClasses) {
			if(cs.compiledClass.equals(cls)) {
				return cs.sourceFile;
			}
		}
		
		return null;
	}
	
	/**
	 * Compiles a .java file into a .class file.
	 * 
	 * @return {@link true} if the compilation was successful, false if there was an
	 *         error.
	 */
	private boolean compile(File sourceFile, JavaCompiler compiler) {
		int status = compiler.run(
				null, null, null,
				"-d",
				this.compiledClassDirectory.toString(),
				sourceFile.toString());
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
		Path pathToScriptFromAssets = script.location.getRelativePath().getParent();

		if (pathToScriptFromAssets == null) {
			return this.compiledClassDirectory.toPath();
		} else {
			return this.compiledClassDirectory.toPath().resolve(pathToScriptFromAssets);
		}
	}
	
	private class CompiledScript {
		
		public final File sourceFile;
		public final Class<?> compiledClass;
		
		public CompiledScript(File file, Class<?> cls) {
			this.sourceFile = file;
			this.compiledClass = cls;
		}
	}
}
