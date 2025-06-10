package com.codeshaper.jello.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.engine.database.AssetDatabase;

/**
 * An {@link AssetLocation} provides a way of specifying the location of an
 * Asset within the project. AssetLocations are essentially just a String
 * wrapped in a class the provided methods for manipulating and examining the
 * paths. AssetLocations can point to both builtin assets, and assets within the
 * project's {@code assets} folder.
 * <p>
 * AssetLocations by default point to the {@code asset} folder. When creating
 * AssetLocations, forward slashes "{@code /}" are used to point to sub folders.
 * Ex. {@code textures/player.png}  The extension of an asset must be specified,
 * as it is possible for there to be multiple assets with the same name, but of
 * different types (and thus have different extensions), in the same folder.
 * <p>
 * When referencing builtin assets, the path should be prefixed with "builtin/".
 */
public final class AssetLocation {

	private Path relativePath;

	public AssetLocation(String location) {
		this(Path.of(location));
	}

	/**
	 * 
	 * @throws IllegalStateException if no AssetDatabse has been created yet
	 * @param file
	 */
	public AssetLocation(File file) {
		AssetDatabase database = AssetDatabase.getInstance();
		if (database == null) {
			throw new IllegalStateException("Asset Database has not been created yet");
		}

		this.relativePath = database.assetsFolder.relativize(file.toPath());
	}

	public AssetLocation(Path relativePath) {
		this.relativePath = relativePath;
	}

	@Override
	public String toString() {
		return this.relativePath.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		return obj instanceof AssetLocation && this.relativePath.equals(((AssetLocation) obj).relativePath);
	}

	@Override
	public int hashCode() {
		return this.relativePath.hashCode();
	}

	/**
	 * Gets an {@link InputStream} the provide the contents of the file. The
	 * returned Input Stream must be closed by the caller. If this AssetLocation
	 * does not point to an Asset, null is returned.
	 * 
	 * @return
	 * @see AssetLocation#isValid()
	 */
	// TODO make this work in builds
	public InputStream getInputSteam() {
		if (this.isBuiltin()) {
			return this.getClass().getResourceAsStream("/" + this.relativePath);
		} else {
			try {
				return new FileInputStream(this.getFile());
			} catch (FileNotFoundException | SecurityException e) {
				return null;
			}
		}
	}

	/**
	 * Gets the name of the file this points to, without an extension.
	 * 
	 * @return the file name
	 */
	public String getName() {
		return FilenameUtils.removeExtension(this.relativePath.getFileName().toString());
	}

	/**
	 * Gets the extension of the file this AssetLocation points to.
	 * 
	 * @return
	 */
	public String getExtension() {
		if (this.isValid()) {
			return FilenameUtils.getExtension(this.relativePath.getFileName().toString());
		} else {
			return StringUtils.EMPTY;
		}
	}

	/**
	 * Gets a {@link File} pointing to this Asset on disk. For builtin Asset's,
	 * {@code null} is returned, as there is no physical file representing them.
	 * 
	 * @return a {@link File} pointing to the Asset.
	 * @see AssetLocation#isBuiltin()
	 */
	public File getFile() {
		if (this.isBuiltin()) {
			return null;
		} else {
			return this.getFullPath().toFile();
		}
	}

	/**
	 * Gets the relative path that this points to. The path is relative to the
	 * {@code/assets} folder.
	 * 
	 * @return
	 */
	public Path getRelativePath() {
		return this.relativePath;
	}

	/**
	 * Checks if this AssetLocation points to a builtin Asset.
	 * 
	 * @return {@code true} if this points to a builtin Asset
	 */
	public boolean isBuiltin() {
		return this.relativePath.startsWith("builtin");
	}

	/**
	 * Checks if this AssetLocation points to an actual file, or in the case of this
	 * pointing to a builtin Asset, does the builtin Asset exist.
	 * 
	 * @return {@code true} if this points to a file.
	 */
	// TODO make this work in builds.
	public boolean isValid() {
		if (this.isBuiltin()) {
			return this.getClass().getResource("/" + this.relativePath) != null;
		} else {
			return Files.exists(this.getFullPath());
		}
	}

	/**
	 * Updates the path to this AssetLocation. This should be used over
	 * instantiating a new object when there are many references to the already
	 * existing AssetLocation object. No checks are done to insure the new path
	 * points to an existing Asset.
	 * 
	 * @param newRelativePath the new path to an Asset
	 */
	public void updateLocation(Path newRelativePath) {
		this.relativePath = newRelativePath;
	}

	private Path getFullPath() {
		return AssetDatabase.getInstance().assetsFolder.resolve(this.relativePath);
	}
}
