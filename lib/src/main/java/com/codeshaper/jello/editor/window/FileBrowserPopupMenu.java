package com.codeshaper.jello.editor.window;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.commons.io.FilenameUtils;

import com.codeshaper.jello.editor.CreateAssetEntries;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.CreateAssetEntries.MenuEntry;
import com.codeshaper.jello.editor.event.ProjectReloadListener;
import com.codeshaper.jello.editor.utils.JelloFileUtils;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

public abstract class FileBrowserPopupMenu extends JPopupMenu {

	private final Path assetsDirectory;
	private final CreateAssetEntries createEntries;
	private final JMenuItem newFolder;
	private final JMenuItem openLocation;
	private final JMenuItem delete;
	private final JMenuItem rename;
	private final JMenuItem copyPath;

	protected File file;

	public FileBrowserPopupMenu(Path assetsDirectory) {
		this.assetsDirectory = assetsDirectory;
		this.createEntries = new CreateAssetEntries();
		
		this.add(new CreateAssetMenu(createEntries));

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
	 * @param file        the file that was deleted.
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
	 * @param asset
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
	}

	private File getDirectory(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			return fileOrDirectory;
		} else {
			return fileOrDirectory.getParentFile();
		}
	}
	
	public class CreateAssetMenu extends JMenu implements ProjectReloadListener {

		private final CreateAssetEntries createEntries;
		
		public CreateAssetMenu(CreateAssetEntries createEntries) {
			super("Create");
			
			this.createEntries = createEntries;
			
			JelloEditor.instance.addProjectReloadListener(this);
		}		
		
		@Override
		public void onProjectReload(Phase phase) {
			if(phase == Phase.POST_REBUILD) {
				this.rebuild();
			}
		}
		
		private void rebuild() {
			this.removeAll();
			
			for (MenuEntry entry : this.createEntries) {
				JMenuItem menuItem = new JMenuItem(entry.getMenuName());
				menuItem.addActionListener(e -> {
					JelloEditor.getWindow(FileBrowserWindow.class);
					
					File directory = getDirectory(file);
					String assetName = entry.getNewAssetName();
					
					// Somehow... this works.
					File f = new File(directory, assetName + "." + SerializedJelloObject.EXTENSION);
					f = JelloFileUtils.getAvailableFileName(f);								
					assetName = FilenameUtils.removeExtension(f.getName());
					
					String newAssetName = (String)JOptionPane.showInputDialog(
			                this,
			                "Name:",
			                "Name Asset",
			                JOptionPane.PLAIN_MESSAGE,
			                null,
			                null,
			                assetName);
					
					SerializedJelloObject asset = JelloEditor.instance.assetDatabase.createAsset(entry.clazz,
							assetsDirectory.relativize(directory.toPath()), newAssetName);
					if (asset != null) {
						onCreate(directory, asset);
					}
				});
				this.add(menuItem);
			}
		}
	}
}