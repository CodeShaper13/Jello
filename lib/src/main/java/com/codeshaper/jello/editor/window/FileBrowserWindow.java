package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.swing.WrapLayout;
import com.codeshaper.jello.editor.util.FolderFilter;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.TextAsset;

import ModernDocking.Dockable;
import ModernDocking.app.Docking;

public class FileBrowserWindow extends EditorWindow {

	private AssetFolder rootFolder;
	private final FileTreeModel fileTreeModel;
	private final JTree fileTree;
	private final DefaultListModel<File> fileListModel;
	private final JList<File> fileList;

	public FileBrowserWindow() {
		super("Project", "fileViewer");

		this.rootFolder = new AssetFolder(new File(JelloEditor.instance.rootProjectFolder, "assets"));

		this.fileListModel = new DefaultListModel<File>();
		this.fileList = new JFileList(this.fileListModel);
		
		this.fileTreeModel = new FileTreeModel(rootFolder);
		this.fileTree = new JFolderTree(this.fileTreeModel);
		
		JScrollPane fileListScrollBar = new JScrollPane(this.fileTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JScrollPane folderContentsScrollBar = new JScrollPane(this.fileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		this.setLayout(new BorderLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileListScrollBar, folderContentsScrollBar);
		splitPane.setAutoscrolls(false);
		this.add(splitPane, BorderLayout.CENTER);
	}

	@Override
	public void addMoreOptions(JPopupMenu menu) {
		JMenuItem refresh = new JMenuItem("Refresh");
		refresh.addActionListener(e -> {
			this.fileTreeModel.reload();
		});
		menu.add(refresh);
	}

	@Override
	public boolean isWrappableInScrollpane() {
		return false;
	}

	/**
	 * Sets the folder that the right panel will display.
	 */
	private void setTargetFolder(AssetFolder folder) {
		this.fileListModel.removeAllElements();

		if (folder != null) {
			for (File file : folder.file.listFiles()) {
				System.out.println(file.toString());
				if (file.isFile()) {
					this.fileListModel.addElement(file);
				}
			}
		}
	}

	private class FileTreeModel implements TreeModel {

		private final ArrayList<TreeModelListener> mListeners = new ArrayList<>();
		private final AssetFolder mFile;

		public FileTreeModel(AssetFolder pFile) {
			mFile = pFile;
		}

		@Override
		public Object getRoot() {
			return mFile;
		}

		@Override
		public Object getChild(Object parent, int index) {
			return ((AssetFolder) parent).getSubFolders()[index];
		}

		@Override
		public int getChildCount(Object parent) {
			return ((AssetFolder) parent).getSubFolders().length;
		}

		@Override
		public boolean isLeaf(Object node) {
			return !((AssetFolder) node).isDirectory();
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			AssetFolder oldTmp = (AssetFolder) path.getLastPathComponent();
			File oldFile = oldTmp.file;
			String newName = (String) newValue;
			File newFile = new File(oldFile.getParentFile(), newName);
			oldFile.renameTo(newFile);
			reload();
		}

		@Override
		public int getIndexOfChild(Object pParent, Object pChild) {
			AssetFolder[] files = ((AssetFolder) pParent).getSubFolders();
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
			setTargetFolder(null);

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
	}

	private class AssetFolder {

		public final File file;

		public AssetFolder(File file) {
			this.file = file;
		}

		public boolean isDirectory() {
			return this.file.isDirectory();
		}

		public AssetFolder[] getSubFolders() {			
			File[] directories = this.file.listFiles(new FolderFilter());
			AssetFolder[] folders = new AssetFolder[directories.length];
			for (int i = 0; i < directories.length; i++) {
				File f = directories[i];
				folders[i] = new AssetFolder(f);
			}
			return folders;
		}

		@Override
		public String toString() {
			return this.file.getName();
		}
	}

	private class JFolderTree extends JTree {
		
		private JPopupMenu menu;
		
		private JMenuItem menuDelete;
		private JMenuItem menuRename;
		
		public JFolderTree(FileTreeModel fileTreeModel) {
			super(fileTreeModel);
		
			this.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					Object selectedNode = fileTree.getLastSelectedPathComponent();
					if (selectedNode instanceof AssetFolder folder) {
						setTargetFolder(folder);
					}
				}
			});
			this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			this.setEditable(true);
			
			this.menu = new JPopupMenu();
			
			JMenu createMenu = new JMenu("Create");
			// TODO
			this.menu.add(createMenu);

			JMenuItem newFolder = new JMenuItem("New Folder");
			newFolder.addActionListener(e -> {
				AssetFolder selected = getSelectedFolder();
				if (selected != null) {
					File newF = new File(selected.file, "New Folder");
					newF.mkdirs();
					fileTreeModel.reload();
				}
			});
			this.menu.add(newFolder);

			this.menu.addSeparator();

			JMenuItem openLocation = new JMenuItem("Open Location");
			openLocation.addActionListener(e -> {
				AssetFolder selected = this.getSelectedFolder();
				try {
					if (selected != null) {
						Desktop.getDesktop().open(selected.file.getParentFile());
					}
				} catch (IOException exception) {
					Debug.logError("Unable to open \"{0}\" in the File Explorer", selected.file);
				}
			});
			this.menu.add(openLocation);

			this.menuDelete = new JMenuItem("Delete");
			this.menuDelete.addActionListener(e -> {
				AssetFolder selected = this.getSelectedFolder();
				if (selected != null) {
					if (!Desktop.getDesktop().moveToTrash(selected.file)) {
						Debug.logError("Unable to move \"{0}\" to the Recycle Bin", selected.file);
					}
				}
				fileTreeModel.reload();
			});
			this.menu.add(this.menuDelete);

			this.menuRename = new JMenuItem("Rename");
			this.menuRename.addActionListener(e -> {
				this.startEditingAtPath(this.getSelectionPath());
			});
			this.menu.add(this.menuRename);

			JMenuItem copyPath = new JMenuItem("Copy Path");
			copyPath.addActionListener(e -> {
				AssetFolder selected = getSelectedFolder();
				if (selected != null) {
					StringSelection selection = new StringSelection(selected.file.getPath());
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
				}
			});
			menu.add(copyPath);
		}
		
		@Override
		public JPopupMenu getComponentPopupMenu() {
			AssetFolder folder = (AssetFolder) fileTree.getLastSelectedPathComponent();
			if(folder != null) {
				boolean isRoot = folder == rootFolder;
				this.menuDelete.setEnabled(!isRoot);
				this.menuRename.setEnabled(!isRoot);
				return this.menu;
			} else {
				return super.getComponentPopupMenu();
			}
		}	
		
		private AssetFolder getSelectedFolder() {
			return (AssetFolder) fileTree.getLastSelectedPathComponent();
		}
	}
	
	private class JFileList extends JList<File> {

		private JPopupMenu menu;
		
		public JFileList(DefaultListModel<File> model) {
			super(model);
			
			this.setCellRenderer(new ListFileRenderer());
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			this.setVisibleRowCount(-1);
			this.addListSelectionListener(e -> {
				InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
				File file = this.getSelectedValue();
				if (file != null) {
					inspector.setTarget(new TextAsset(file));
				}
			});
			
			this.menu = new JPopupMenu();
			JMenuItem delete = new JMenuItem("Delete");
			delete.addActionListener(e -> {
				File file = getSelectedValue();
				if (file != null) {
					if (!Desktop.getDesktop().moveToTrash(file)) {
						Debug.logError("Unable to move \"{0}\" to the Recycle Bin", file);
					} else {
						model.removeElement(file);
					}
				}
			});			
			this.menu.add(delete);			
		}
		
		@Override
		public JPopupMenu getComponentPopupMenu() {
			File selected = fileList.getSelectedValue();
			return selected != null ? this.menu : super.getComponentPopupMenu();
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

				this.setText(file.getName());

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