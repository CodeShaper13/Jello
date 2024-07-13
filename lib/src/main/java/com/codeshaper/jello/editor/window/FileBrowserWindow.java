package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
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

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;

public class FileBrowserWindow extends EditorWindow {

	private static final String PROP_SHOW_EXTENSIONS = "showExtensions";
	private static final String PROP_DIVIDER_LOCATION = "dividerLocation";
	
	/**
	 * The /assets folder.
	 */
	private final File rootDirectory;
	private final FolderHierarchyModel fileTreeModel;
	private final FolderHierarchy folderTree;
	private final DefaultListModel<File> fileListModel;
	private final FileList fileList;
	private final FileBrowserPopupMenu popupMenu;
	private final JSplitPane splitPane;
	private JCheckBoxMenuItem showExtensions;

	public FileBrowserWindow() {
		super("Project", "fileViewer");
		
		this.rootDirectory = JelloEditor.instance.assetsFolder.toFile();

		this.fileListModel = new DefaultListModel<File>();
		this.fileList = new FileList(this.fileListModel);

		this.fileTreeModel = new FolderHierarchyModel(rootDirectory);
		this.folderTree = new FolderHierarchy(this.fileTreeModel);

		this.popupMenu = new FileBrowserPopupMenu() {

			@Override
			protected void onNewFolder(File newDirectory) {
				fileTreeModel.reload();
			}

			@Override
			protected void onRename(File file) {
				if (file.isFile()) {
					// Not yet implemented.
				} else {
					folderTree.startEditingAtPath(folderTree.getSelectionPath());
				}
			}

			@Override
			protected void onDelete(File file) {
				if (file.isDirectory()) {
					fileTreeModel.reload();
				} else {
					fileListModel.removeElement(this.file);
					JelloEditor.instance.assetDatabase.removeAsset(this.file.toPath());
				}
			}

			@Override
			protected void onCreate(File directory, SerializedJelloObject asset) {
				fileList.setTargetDirectory(directory);
			}
		};

		JScrollPane fileListScrollBar = new JScrollPane(this.folderTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JScrollPane folderContentsScrollBar = new JScrollPane(this.fileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.setLayout(new BorderLayout());
		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileListScrollBar, folderContentsScrollBar);		
		this.splitPane.setResizeWeight(0.5);
		this.splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e -> {
			int location = this.splitPane.getDividerLocation();
			this.setProperty(PROP_DIVIDER_LOCATION, location);
		});
		this.splitPane.setAutoscrolls(false);
		this.add(this.splitPane, BorderLayout.CENTER);

		this.showExtensions = new JCheckBoxMenuItem("Show Extensions");
		this.showExtensions.addActionListener(e -> {
			this.setProperty(PROP_SHOW_EXTENSIONS, this.showExtensions.isSelected());
			this.fileList.updateUI(); // Causes a redraw.
		});
		/*
		File f1 = new File("C:\\Users\\Pj\\Desktop\\jelloprojects\\dev\\assets\\New Folderx");
		File f2 = new File("C:\\Users\\Pj\\Desktop\\jelloprojects\\dev\\assets\\New Folder");
		File f3 = new File("C:\\Users\\Pj\\Desktop\\jelloprojects\\dev\\assets\\text.txt");
		File f4 = new File("C:\\Users\\Pj\\Desktop\\jelloprojects\\dev\\assets\\text.txt");

		System.out.println(this.getAvailabeFileName(f1));
		System.out.println(this.getAvailabeFileName(f2));
		System.out.println(this.getAvailabeFileName(f3));
		System.out.println(this.getAvailabeFileName(f4));
		*/
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
	
	@Override
	public void updateProperties() {
		boolean showExtensions = this.getPropertyBoolean(PROP_SHOW_EXTENSIONS, true);
		System.out.println(showExtensions);
		this.showExtensions.setSelected(showExtensions);
		this.splitPane.setDividerLocation(this.getPropertyInt(PROP_DIVIDER_LOCATION, 100));		
	}

	private File getAvailabeFileName(File file) {
		if (!file.exists()) {
			return file; // Name is available, do nothing.
		}

		File pathTo = file.getParentFile();
		String name;
		String fileExtension = null;

		if (file.isDirectory()) {
			name = file.getName(); // Directory name.
		} else {
			name = FilenameUtils.removeExtension(file.getName());
			fileExtension = FilenameUtils.getExtension(file.getName());
		}

		File newName = file;
		int counter = 1;
		do {
			if (file.isDirectory()) {
				newName = new File(pathTo, name + counter);
			} else {
				newName = new File(pathTo, String.format("%s(%s).%s", name, counter, fileExtension));
			}
			counter++;
		} while (newName.exists());

		return newName;
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
			return !((File) node).isDirectory();
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
			fileList.setTargetDirectory(null);

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

		public FolderHierarchy(FolderHierarchyModel fileTreeModel) {
			super(fileTreeModel);

			this.setCellEditor(new MyTreeCellEditor(folderTree, (DefaultTreeCellRenderer) this.getCellRenderer()));
			this.setEditable(true);
			this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "startEditing");
			this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			this.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					File selected = getSelectedDirectory();
					if (selected != null) {
						fileList.setTargetDirectory(selected);
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
				boolean isRoot = selectedDirectory.equals(rootDirectory);
				popupMenu.setTargetFile(selectedDirectory, !isRoot);
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

	private class FileList extends JList<File> {

		public FileList(DefaultListModel<File> model) {
			super(model);

			this.setCellRenderer(new ListFileRenderer());
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			this.setVisibleRowCount(-1);
			this.addListSelectionListener(e -> {
				InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
				File file = this.getSelectedValue();
				if (file != null) {
					// Convert to a relative path.
					Path pathAbsolute = file.toPath();
					Path pathBase = JelloEditor.instance.assetsFolder;
					Path pathRelative = pathBase.relativize(pathAbsolute);

					Asset asset = JelloEditor.instance.assetDatabase.getAsset(pathRelative);
					inspector.setTarget(asset);
				}
			});
		}

		@Override
		public JPopupMenu getComponentPopupMenu() {
			File selected = fileList.getSelectedValue();
			if (selected != null) {
				popupMenu.setTargetFile(selected, true);
				return popupMenu;
			} else {
				File selectedDirecotry = folderTree.getSelectedDirectory();
				if (selectedDirecotry != null) {
					popupMenu.setTargetFile(selectedDirecotry, false);
					return popupMenu;
				} else {
					return super.getComponentPopupMenu();
				}
			}
		}

		/**
		 * Sets the folder that the right panel will display.
		 * 
		 * @param folder
		 */
		public void setTargetDirectory(File directory) {
			DefaultListModel<File> model = (DefaultListModel<File>) this.getModel();
			model.removeAllElements();

			if (directory != null) {
				for (File file : directory.listFiles((FileFilter) FileFilterUtils.fileFileFilter())) {
					model.addElement(file);
				}
			}
		}

		private class ListFileRenderer extends JLabel implements ListCellRenderer<File> {

			public ListFileRenderer() {
				this.setIcon(UIManager.getIcon("FileView.fileIcon"));
				this.setHorizontalAlignment(JLabel.CENTER);
				this.setHorizontalTextPosition(JLabel.CENTER);
				this.setVerticalTextPosition(JLabel.BOTTOM);
				this.setPreferredSize(new Dimension(80, 80));
				this.setOpaque(true);
			}

			@Override
			public Component getListCellRendererComponent(JList<? extends File> list, File file, int index,
					boolean isSelected, boolean cellHasFocus) {

				// Remove the extension on SerializedJelloObjects.
				String fileName = file.getName();
				//if (FilenameUtils.getExtension(fileName).equals(SerializedJelloObject.EXTENSION)) {
					this.setText(showExtensions.isSelected() ? fileName : FilenameUtils.removeExtension(fileName));
				//} else {
				//	this.setText(fileName);
				//}

				if (isSelected) {
					this.setBackground(list.getSelectionBackground());
					this.setForeground(list.getSelectionForeground());
				} else {
					this.setBackground(list.getBackground());
					this.setForeground(list.getForeground());
				}

				return this;
			}
		}
	}
}