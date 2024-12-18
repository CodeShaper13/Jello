package com.codeshaper.jello.editor;

import java.awt.Image;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconCache {

	private final Hashtable<String, Icon> icons;

	public IconCache() {
		this.icons = new Hashtable<String, Icon>();
	}

	/**
	 * Gets an {@link Icon} from a path. First the /assets folder is searched, and
	 * if no icon is found, then the jar is searched. If no icon is found in either,
	 * {@code null} is returned. If {@code location} is {@code null}, {@code null}
	 * is returned.
	 * 
	 * @param location the path to the icon
	 * @return an icon
	 */
	public Icon getIcon(String location) {
		if (location == null) {
			return null;
		}

		Icon icon = this.icons.get(location);
		if (icon != null) {
			return icon;
		} else {
			// Try and load the icon from the /assets folder
			try {
				Path fullPath = Paths.get(JelloEditor.instance.assetsFolder.toString(), location);
				if (Files.exists(fullPath)) { // check if file exists in the project...
					ImageIcon image = new ImageIcon(fullPath.toString());
					if (image.getIconWidth() != 16 || image.getIconHeight() != 16) {
						icon = new ImageIcon(image.getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
					} else {
						icon = image;
					}
					return icon;
				}
			} catch (InvalidPathException e) {
				return null;
			}

			// Try and load the icon from the jar.
			URL url = IconCache.class.getResource(location);
			if (url != null) {
				icon = new ImageIcon(url);
				this.icons.put(location, icon);
				return icon;
			}

			return null;
		}
	}

	/**
	 * Clears the cache.
	 */
	public void clearCache() {
		this.icons.clear();
	}
}
