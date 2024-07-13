package com.codeshaper.jello.editor.window;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.codeshaper.jello.editor.EditorAssetDatabase;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

public abstract class FileBrowserPopupMenu extends JPopupMenu {

	private final JMenu create;
	private final JMenuItem newFolder;
	private final JMenuItem openLocation;
	private final JMenuItem delete;
	private final JMenuItem rename;
	private final JMenuItem copyPath;

	protected File file;

	public FileBrowserPopupMenu() {
		this.add(this.create = new JMenu("Create"));

		this.add(this.newFolder = new JMenuItem("New Folder"));
		this.newFolder.addActionListener(e -> {
			if (this.file != null) {
				File newDirectory = new File(this.getDirectory(this.file), "New Folder");
				try {
					newDirectory.mkdirs();
					this.onNewFolder(newDirectory);
				} catch (SecurityException exception) {
					Debug.logError("Could not create directory %s", newDirectory);
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
				boolean showError = false;
				try {
					boolean wasDeleted = Desktop.getDesktop().moveToTrash(this.file);
					if (wasDeleted) {
						this.onDelete(this.file);
					} else {
						showError = true;
					}
				} catch (Exception exception) {
					exception.printStackTrace();
					showError = true;
				}

				if (showError) {
					Debug.logError("Unable to move %s to the Recycle Bin", this.file);
				}
			}
		});

		this.add(this.rename = new JMenuItem("Rename"));
		this.rename.addActionListener(e -> {
			if (this.file.isDirectory()) {
				this.onRename(this.file);
			}
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
	 * Called when a file is successfully deleted when the Delete button is clicked.
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
	 * @param directory the directory to create the asset it.
	 * @param data
	 */
	protected abstract void onCreate(File directory, SerializedJelloObject asset);

	/**
	 * Sets the File (or directory) that the menu should operate on. This should be
	 * called before showing the toolbar.
	 * 
	 * @param file
	 * @param allowDeletingAndRenaming are the delete and rename items enabled.
	 */
	public void setTargetFile(File file, boolean allowDeletingAndRenaming) {
		this.file = file;

		this.delete.setEnabled(allowDeletingAndRenaming);
		this.rename.setEnabled(allowDeletingAndRenaming);

		this.create.removeAll();

		for (var entry : JelloEditor.instance.assetDatabase.createAssetEntries) {
			JMenuItem menuItem = new JMenuItem(entry.getMenuName());
			menuItem.addActionListener(e -> {
				EditorAssetDatabase database = JelloEditor.instance.assetDatabase;
				File directory = this.getDirectory(this.file);
				SerializedJelloObject asset = database.createAsset(entry.clazz, directory.toPath(), entry.getNewAssetName());
				if(asset != null) {
					this.onCreate(this.getDirectory(this.file), asset);
				}
			});
			this.create.add(menuItem);
		}
	}

	private File getDirectory(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			return fileOrDirectory;
		} else {
			return fileOrDirectory.getParentFile();
		}
	}
}