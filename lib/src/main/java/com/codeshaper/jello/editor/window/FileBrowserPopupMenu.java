package com.codeshaper.jello.editor.window;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.codeshaper.jello.editor.CreateAssetEntries;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.CreateAssetEntries.MenuEntry;
import com.codeshaper.jello.editor.event.ProjectReloadListener;
import com.codeshaper.jello.editor.utils.JelloFileUtils;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

public abstract class FileBrowserPopupMenu extends JPopupMenu {

	private final Path assetsDirectory;
	private final CreateAssetEntries createEntries;
	private final MenuCreate create;
	private final JMenuItem newFolder;
	private final JMenuItem openLocation;
	private final JMenuItem delete;
	private final JMenuItem rename;
	private final JMenuItem copyPath;

	/**
	 * The file this popup was created for.
	 */
	protected File file;

	public FileBrowserPopupMenu(Path assetsDirectory) {
		this.assetsDirectory = assetsDirectory;
		this.createEntries = new CreateAssetEntries();

		this.add(this.create = new MenuCreate(createEntries));

		this.add(this.newFolder = new JMenuItem("New Folder"));
		this.newFolder.addActionListener(e -> {
			if (this.file != null) {
				File newDirectory = new File(this.getDirectory(this.file), "New Folder");
				newDirectory = JelloFileUtils.getAvailableFileName(newDirectory);
				try {
					newDirectory.mkdirs();
					this.onNewFolder(newDirectory);
				} catch (SecurityException exception) {
					Debug.logError("Could not create directory %s", newDirectory.toString());
				}
			}
		});

		this.addSeparator();

		this.add(this.openLocation = new JMenuItem("Open Location"));
		openLocation.addActionListener(e -> {
			if (this.file != null) {
				try {
					Desktop.getDesktop().open(this.file.getParentFile());
				} catch (Exception exception) {
					Debug.logError("Unable to open \"%s\" in the File Explorer", this.file);
				}
			}
		});

		this.add(this.delete = new JMenuItem("Delete"));
		this.delete.addActionListener(e -> {
			if (this.file != null) {
				this.onDelete(file);
			}
		});

		this.add(this.rename = new JMenuItem("Rename"));
		this.rename.addActionListener(e -> {
			this.onRename(this.file);
		});

		this.add(this.copyPath = new JMenuItem("Copy Path"));
		this.copyPath.addActionListener(e -> {
			if (this.file != null) {
				StringSelection selection = new StringSelection(this.file.getPath());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
			}
		});
	}

	/**
	 * Called when the New Folder button is clicked, after the folder has been
	 * created.
	 * 
	 * @param newDirectory the newly created folder.
	 */
	protected abstract void onNewFolder(File newDirectory);

	/**
	 * Called when the Delete button is clicked.
	 * 
	 * @param file the file that was deleted.
	 */
	protected abstract void onDelete(File file);

	/**
	 * Called when the Rename button is clicked.
	 * 
	 * @param file the file to rename.
	 */
	protected abstract void onRename(File file);

	/**
	 * Called when the Create -> [Asset Name] button is clicked.
	 * 
	 * @param newAsset a {@link File} pointing to the newly created asset.
	 */
	protected abstract void onCreate(File newAsset);

	/**
	 * Sets the File (or directory) that the menu should operate on. This should be
	 * called before showing the toolbar.
	 * 
	 * @param file
	 */
	public void setTargetFile(File file) {
		this.file = file;
		
		boolean allowDeletingAndRenaming = !file.equals(JelloEditor.instance.assetsFolder.toFile());

		this.delete.setEnabled(allowDeletingAndRenaming);
		this.rename.setEnabled(allowDeletingAndRenaming);
	}

	/**
	 * If {@code fileOrDirectory} is a file, this returns the directory holding the
	 * folder. If {@code fileOrDirectory} is a directory, it is returned.
	 * 
	 * @param fileOrDirectory
	 * @return
	 */
	private File getDirectory(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			return fileOrDirectory;
		} else {
			return fileOrDirectory.getParentFile();
		}
	}

	private class MenuCreate extends JMenu implements ProjectReloadListener {

		private final CreateAssetEntries createEntries;

		public MenuCreate(CreateAssetEntries createEntries) {
			super("Create");

			this.createEntries = createEntries;

			JelloEditor.instance.addProjectReloadListener(this);

			this.rebuild();
		}

		@Override
		public void onProjectReload(Phase phase) {
			if (phase == Phase.POST_REBUILD) {
				this.rebuild();
			}
		}

		private void rebuild() {
			this.removeAll();

			List<JMenuItem> menuEntries = new ArrayList<JMenuItem>();

			menuEntries.add(new JMenuItemNewFile("Script", "NewScript.java", "/newScriptFileContents.txt") {
				@Override
				protected String processLine(String line, File file) {
					String packageName = "TODO"; // TODO
					String className = FilenameUtils.removeExtension(file.getName());
					return line.replace("<package_name>", packageName).replace("<class_name>", className);
				}
			});
			menuEntries.add(new JMenuItemNewFile("Shader", "NewShader.shader", "/newShaderFileContents.txt"));

			for (MenuEntry entry : this.createEntries) {
				menuEntries.add(new JMenuItemNewSerializedObject(entry));
			}

			Collections.sort(menuEntries, (o1, o2) -> (o1.getText().compareTo(o2.getText())));
			for (JMenuItem menuItem : menuEntries) {
				this.add(menuItem);
			}
		}

		private class JMenuItemNew extends JMenuItem {

			public JMenuItemNew(String text) {
				super(text);
			}

			/**
			 * 
			 * @param defaultName the default name of the asset, including it's extension.
			 * @return
			 */
			protected String getNewAssetName(String defaultFileName) {
				File directory = getDirectory(file);

				File file = new File(directory, defaultFileName);
				file = JelloFileUtils.getAvailableFileName(file);

				String s = (String) JOptionPane.showInputDialog(
						this,
						"Name:",
						"Name Asset",
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						FilenameUtils.removeExtension(file.getName())); // Initial value.
				return s + "." + FilenameUtils.getExtension(file.getName());
			}
		}

		private class JMenuItemNewFile extends JMenuItemNew {

			public JMenuItemNewFile(String labelText, String defaultFileNameAndExtension, String fileContentsPath) {
				super(labelText);

				this.addActionListener((e) -> {
					File directory = getDirectory(file);

					String s = this.getNewAssetName(defaultFileNameAndExtension);
					File file = new File(directory, s);
					try {
						if (!file.exists()) {
							file.createNewFile();
						}

						// Read the template file.
						InputStream stream = this.getClass().getResourceAsStream(fileContentsPath);
						List<String> lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);

						// Write the new file to disk.
						try (Writer writer = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
							for (String line : lines) {
								line = this.processLine(line, file);
								writer.append(line);
								writer.append(System.lineSeparator());
							}
						}

						// Add the new Asset to the Asset Database.
						if(JelloEditor.instance.assetDatabase.addAsset(new AssetLocation(file))) {
							onCreate(file);
						}
					} catch (Exception exception) {
						Debug.log("Error creating new Asset");
						exception.printStackTrace();
					}
				});
			}

			/**
			 * Called for every line in the template file before writing the line to the new
			 * file.
			 * 
			 * @param line the line from the template file.
			 * @param file a file pointing to the newly created asset.
			 * @return the line to write to the file.
			 */
			protected String processLine(String line, File file) {
				return line;
			}
		}

		private class JMenuItemNewSerializedObject extends JMenuItemNew {

			public JMenuItemNewSerializedObject(MenuEntry entry) {
				super(entry.getMenuName());
				this.addActionListener(e -> {
					String newAssetName = FilenameUtils.removeExtension(
							this.getNewAssetName(entry.getNewAssetName() + "." + SerializedJelloObject.EXTENSION));
					File assetLocation = getDirectory(file);

					Path path = assetsDirectory.relativize(assetLocation.toPath());
					SerializedJelloObject asset = JelloEditor.instance.assetDatabase.createAsset(
							entry.clazz,
							path,
							newAssetName);
					if (asset != null) {
						onCreate(new File(path.toFile(), newAssetName + "." + SerializedJelloObject.EXTENSION));
					}
				});
			}

		}
	}
}