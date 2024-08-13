package com.codeshaper.jello.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.engine.database.AssetDatabase;

/**
 * An {@link AssetLocation} provides the location of an Asset within the
 * project.
 */
public final class AssetLocation {

	private Path relativePath;

	public AssetLocation(Path relativePath) {
		this.relativePath = relativePath;
	}

	/**
	 * Gets an {@link InputStream} to the Asset. The returned Input Stream must be
	 * closed by the caller. If this location does not point to an Asset, null is
	 * returned.
	 * 
	 * @return
	 * @see AssetLocation#isValid()
	 */
	public InputStream getInputSteam() {
		if (this.pointsToBuiltin()) {
			return this.getClass().getResourceAsStream("/" + this.relativePath);
		} else {
			try {
				return new FileInputStream(this.getFullPath().toFile());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Gets the name of the file this points to, without an extension.
	 * 
	 * @return the file name.
	 */
	public String getName() {
		return FilenameUtils.removeExtension(this.relativePath.getFileName().toString());
	}

	/**
	 * Gets the relative path that this points to. The path is relative to the
	 * {@code/assets} folder.
	 * 
	 * @return
	 */
	public Path getPath() {
		return this.relativePath;
	}

	/**
	 * Gets the full path to the Asset on disk. For builtin Assets,
	 * {@link CachedAsset#getPath()} is returned.
	 * 
	 * @return
	 */
	public Path getFullPath() {
		if (this.pointsToBuiltin()) {
			return this.getPath();
		} else {
			return AssetDatabase.getInstance().assetsFolder.resolve(this.relativePath);
		}
	}

	/**
	 * Checks if this points to a builtin Asset.
	 * 
	 * @return {@code true} if the Asset is a builtin Asset.
	 */
	public boolean pointsToBuiltin() {
		return this.relativePath.startsWith("builtin");
	}

	/**
	 * Checks if this AssetLocation points to an actual file, or in the case of this
	 * pointing to a builtin Asset, does the builtin Asset exist.
	 * 
	 * @return {@code true} if this points to a file.
	 */
	// TODO make this work in builds as well.
	public boolean isValid() {
		if (this.pointsToBuiltin()) {
			return this.getClass().getResource("/" + this.relativePath) != null;
		} else {
			return Files.exists(this.relativePath);
		}
	}

	public void updateLocation(Path newRelativePath) {
		this.relativePath = newRelativePath;
	}
}
