package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.ComponentEditor;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;

public class FileBrowserWindow extends EditorWindow {

	private static final String PROP_SHOW_EXTENSIONS = "showExtensions";
	private static final String PROP_DIVIDER_LOCATION = "dividerLocation";

	/**
	 * The /assets folder.
	 */
	private final File rootDirectory;
	private final FileSearchBar searchBar;
	private final FolderHierarchyModel fileTreeModel;
	private final FolderHierarchy folderTree;
	private final FileList fileList;
	private final FileBrowserPopupMenu popupMenu;
	private final JSplitPane splitPane;
	private JCheckBoxMenuItem showExtensions;

	public FileBrowserWindow() {
		super("Project", "fileViewer");

		this.setLayout(new BorderLayout());

		this.rootDirectory = JelloEditor.instance.assetsFolder.toFile();

		this.popupMenu = new FileBrowserPopupMenu(this.rootDirectory.toPath()) {

			@Override
			protected void onNewFolder(File newDirectory) {
				fileTreeModel.reload();
			}

			@Override
			protected void onRename(File file) {
				if (file.isDirectory()) {
					folderTree.startEditingAtPath(folderTree.getSelectionPath());
				} else {
					String newFileName = (String) JOptionPane.showInputDialog(
							this,
							"New Name:",
							"Rename File",
							JOptionPane.PLAIN_MESSAGE,
							null,
							null,
							FilenameUtils.removeExtension(file.getName()));
					if (newFileName != null) {
						boolean success = JelloEditor.instance.assetDatabase.renameAsset(
								new AssetLocation(file), newFileName);
						if (success) {
							fileList.refresh();
						} else {
							Toolkit.getDefaultToolkit().beep();
						}
					}
				}
			}

			@Override
			protected void onDelete(File file) {
				if (file.isDirectory()) {
					boolean showError;
					try {
						showError = !Desktop.getDesktop().moveToTrash(this.file);
					} catch (Exception exception) {
						exception.printStackTrace();
						showError = true;
					}

					if (showError) {
						Debug.logError("Unable to move %s to the Recycle Bin", this.file);
					}
					fileTreeModel.reload();
				} else {
					JelloEditor.instance.assetDatabase.deleteAsset(
							new AssetLocation(file));
					fileList.refresh();
				}
			}

			@Override
			protected void onCreate(File newAsset) {
				fileList.refresh();
			}
		};

		this.fileList = new FileList(this.popupMenu);

		this.searchBar = new FileSearchBar("Search");
		this.searchBar.addActionListener((e) -> {
			String s = this.searchBar.getSearchText().trim();
			if (!StringUtils.isWhitespace(s)) {
				this.fileList.setTarget(this.rootDirectory, s);
			}
		});
		this.add(this.searchBar, BorderLayout.NORTH);

		this.fileTreeModel = new FolderHierarchyModel(rootDirectory);
		this.folderTree = new FolderHierarchy(this.fileTreeModel);

		JScrollPane fileListScrollBar = new JScrollPane(this.folderTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JScrollPane folderContentsScrollBar = new JScrollPane(this.fileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileListScrollBar, folderContentsScrollBar);
		this.splitPane.setDividerLocation(JelloEditor.instance.properties.getInt(PROP_DIVIDER_LOCATION, 100));
		this.splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e -> {
			int location = this.splitPane.getDividerLocation();
			JelloEditor.instance.properties.setInt(PROP_DIVIDER_LOCATION, location);
		});
		this.splitPane.setAutoscrolls(false);
		this.add(this.splitPane, BorderLayout.CENTER);

		this.showExtensions = new JCheckBoxMenuItem("Show Extensions");
		boolean show = JelloEditor.instance.properties.getBoolean(PROP_SHOW_EXTENSIONS, true);
		this.showExtensions.setSelected(show);
		this.fileList.setShowExtensions(show);

		this.showExtensions.addActionListener(e -> {
			boolean show1 = this.showExtensions.isSelected();
			JelloEditor.instance.properties.setBoolean(PROP_SHOW_EXTENSIONS, show1);
			this.fileList.setShowExtensions(show1);
		});
	}

	@Override
	public void addMoreOptions(JPopupMenu menu) {
		JMenuItem refresh = new JMenuItem("Refresh");
		refresh.addActionListener(e -> {
			this.fileTreeModel.reload();
		});
		menu.add(refresh);

		menu.add(this.showExtensions);
	}

	@Override
	public boolean isWrappableInScrollpane() {
		return false;
	}

	/**
	 * Sets the FileBrowser's target. If the target doesn't exist, nothing happens.
	 * 
	 * @param location the location of the target
	 */
	public void setTarget(AssetLocation location) {
		System.out.println("setTarget " + location);
		if (location.isValid() && !location.isBuiltin()) {
			this.fileList.setTarget(location.getFile());
		}
	}

	private class FolderHierarchyModel implements TreeModel {

		private final ArrayList<TreeModelListener> mListeners = new ArrayList<>();
		private final File rootDirectory;

		public FolderHierarchyModel(File rootDirectory) {
			this.rootDirectory = rootDirectory;
		}

		@Override
		public Object getRoot() {
			return this.rootDirectory;
		}

		@Override
		public Object getChild(Object parent, int index) {
			return this.getSubDirectories((File) parent)[index];
		}

		@Override
		public int getChildCount(Object parent) {
			return this.getSubDirectories((File) parent).length;
		}

		@Override
		public boolean isLeaf(Object node) {
			return ((File) node).listFiles((FileFilter) FileFilterUtils.directoryFileFilter()).length == 0;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			File oldFile = (File) path.getLastPathComponent();
			String newName = (String) newValue;
			File newFile = new File(oldFile.getParentFile(), newName);
			oldFile.renameTo(newFile);
			reload();
		}

		@Override
		public int getIndexOfChild(Object pParent, Object pChild) {
			File[] files = this.getSubDirectories((File) pParent);
			for (int i = 0; i < files.length; i++) {
				if (files[i] == pChild)
					return i;
			}
			return -1;
		}

		@Override
		public void addTreeModelListener(TreeModelListener pL) {
			mListeners.add(pL);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener pL) {
			mListeners.remove(pL);
		}

		/**
		 * stolen from
		 * http://developer.classpath.org/doc/javax/swing/tree/DefaultTreeModel-source.html
		 *
		 * <p>
		 * Invoke this method if you've modified the TreeNodes upon which this model
		 * depends. The model will notify all of its listeners that the model has
		 * changed. It will fire the events, necessary to update the layout caches and
		 * repaint the tree. The tree will <i>not</i> be properly refreshed if you call
		 * the JTree.repaint instead.
		 * </p>
		 * <p>
		 * This method will refresh the information about whole tree from the root. If
		 * only part of the tree should be refreshed, it is more effective to call
		 * {@link #reload(TreeNode)}.
		 * </p>
		 */
		public void reload() {
			fileList.refresh();// .setTarget(null);

			// Need to duplicate the code because the root can formally be
			// no an instance of the TreeNode.
			int n = getChildCount(getRoot());
			int[] childIdx = new int[n];
			Object[] children = new Object[n];

			for (int i = 0; i < n; i++) {
				childIdx[i] = i;
				children[i] = getChild(getRoot(), i);
			}

			fireTreeStructureChanged(this, new Object[] { getRoot() }, childIdx, children);
		}

		/**
		 * stolen from
		 * http://developer.classpath.org/doc/javax/swing/tree/DefaultTreeModel-source.html
		 *
		 * fireTreeStructureChanged
		 *
		 * @param source       the node where the model has changed
		 * @param path         the path to the root node
		 * @param childIndices the indices of the affected elements
		 * @param children     the affected elements
		 */
		protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
			TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
			for (TreeModelListener listener : mListeners) {
				listener.treeStructureChanged(event);
			}
		}

		/**
		 * Gets all sub directories of a passed directory.
		 * 
		 * @param parent
		 * @return an array of directories. The array will be empty if there are no sub
		 *         directories are there was an error.
		 */
		private File[] getSubDirectories(File parent) {
			try {
				File[] files = parent.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
				return files != null ? files : new File[0];
			} catch (SecurityException e) {
				return new File[0];
			}
		}
	}

	private class FolderHierarchy extends JTree {

		private static final ImageIcon FOLDER_CLOSED_ICON = new ImageIcon(
				ComponentEditor.class.getResource("/editor/icons/folder_closed.png"));
		private static final ImageIcon FOLDER_OPEN_ICON = new ImageIcon(
				ComponentEditor.class.getResource("/editor/icons/folder_open.png"));

		public FolderHierarchy(FolderHierarchyModel fileTreeModel) {
			super(fileTreeModel);

			DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) this.getCellRenderer();
			renderer.setLeafIcon(FOLDER_CLOSED_ICON);
			renderer.setClosedIcon(FOLDER_CLOSED_ICON);
			renderer.setOpenIcon(FOLDER_OPEN_ICON);

			this.setCellEditor(new MyTreeCellEditor(folderTree, renderer));
			this.setShowsRootHandles(true);
			this.setEditable(true);
			this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "startEditing");
			this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			this.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					File selected = getSelectedDirectory();
					if (selected != null) {
						searchBar.setSearchText(null);
						fileList.setTarget(selected);
					}
				}
			});
		}

		@Override
		public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			if (value instanceof File file) {
				return file.getName();
			}
			return "";
		}

		@Override
		public JPopupMenu getComponentPopupMenu() {
			File selectedDirectory = folderTree.getSelectedDirectory();
			if (selectedDirectory != null) {
				popupMenu.setTargetFile(selectedDirectory);
				return popupMenu;
			} else {
				return super.getComponentPopupMenu();
			}
		}

		/**
		 * Gets the currently selected folder in the hierarchy.
		 * 
		 * @return the selected folder, or {@code null} if no folder is selected.
		 */
		public File getSelectedDirectory() {
			Object selected = folderTree.getLastSelectedPathComponent();
			if (selected != null) {
				return (File) selected;
			} else {
				return null;
			}
		}

		private class MyTreeCellEditor extends DefaultTreeCellEditor {

			public MyTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
				super(tree, renderer);
			}

			@Override
			public boolean isCellEditable(EventObject e) {
				// Prevent the /assets folder from being renamed.
				return super.isCellEditable(e) && !((File) lastPath.getLastPathComponent()).equals(rootDirectory);
			}
		}
	}
}