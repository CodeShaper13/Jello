package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
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

	private final FileTreeModel fileTreeModel;
	private final JTree fileTree;
	private final JPanel fileListPanel;

	public FileBrowserWindow() {
		super("Project", "fileViewer");
		
		AssetFolder rootFolder = new AssetFolder(new File(JelloEditor.instance.rootProjectFolder, "assets"));

		this.fileTreeModel = new FileTreeModel(rootFolder);
		this.fileTree = new JTree(this.fileTreeModel) {
			@Override
			public JPopupMenu getComponentPopupMenu() {
				AssetFolder folder = (AssetFolder) fileTree.getLastSelectedPathComponent();
				if (folder != null) {
					return getPopupMenu(folder == rootFolder);
				} else {
					return super.getComponentPopupMenu();
				}
			}
		};
		this.fileTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object selectedNode = fileTree.getLastSelectedPathComponent();
				if (selectedNode instanceof AssetFolder folder) {
					setTargetFolder(folder);
				}
			}
		});
		this.fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.fileTree.setEditable(true);
		JScrollPane fileListScrollBar = new JScrollPane(this.fileTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.fileListPanel = new JPanel(new WrapLayout());
		JScrollPane folderContentsScrollBar = new JScrollPane(this.fileListPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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

	private void setTargetFolder(AssetFolder folder) {
		this.fileListPanel.removeAll();

		if (folder != null) {
			for (File file : folder.getFile().listFiles()) {
				System.out.println(file.toString());
				if (file.isFile()) {
					JPanel panel = new JPanel(new BorderLayout());
					panel.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							if(e.getButton() == MouseEvent.BUTTON1) {
					    		InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
					    		inspector.setTarget(new TextAsset(file));
							}
						}
					});
					panel.add(new JLabel("TODO"), BorderLayout.CENTER);
					panel.add(new JLabel(file.getName()), BorderLayout.SOUTH);
					panel.setBackground(Color.red);
					panel.setPreferredSize(new Dimension(100, 100));
					this.fileListPanel.add(panel);
				}
			}

			this.fileListPanel.revalidate();
		}
	}

	private JPopupMenu getPopupMenu(boolean isRootFolder) {
		JPopupMenu menu = new JPopupMenu();

		JMenu createMenu = new JMenu("Create");
		menu.add(createMenu);

		JMenuItem newFolder = new JMenuItem("New Folder");
		newFolder.addActionListener(e -> {
			AssetFolder selected = this.getSelectedFolder();
			if (selected != null) {
				File newF = new File(selected.getFile(), "New Folder");
				newF.mkdirs();
				this.fileTreeModel.reload();
			}
		});
		createMenu.add(newFolder);

		menu.addSeparator();

		JMenuItem openLocation = new JMenuItem("Open Location");
		openLocation.addActionListener(e -> {
			AssetFolder selected = this.getSelectedFolder();
			try {
				if (selected != null) {
					Desktop.getDesktop().open(selected.getFile().getParentFile());
				}
			} catch (IOException exception) {
				Debug.logError("Unable to open \"{0}\" in the File Explorer", selected.getFile());
			}
		});
		menu.add(openLocation);

		JMenuItem delete = new JMenuItem("Delete");
		delete.setEnabled(!isRootFolder);
		delete.addActionListener(e -> {
			AssetFolder selected = this.getSelectedFolder();
			if (selected != null) {
				if (!Desktop.getDesktop().moveToTrash(selected.getFile())) {
					Debug.logError("Unable to move \"{0}\" to the Recycle Bin", selected.getFile());
				}
			}
			this.fileTreeModel.reload();
		});
		menu.add(delete);

		JMenuItem rename = new JMenuItem("Rename");
		rename.setEnabled(!isRootFolder);
		rename.addActionListener(e -> {
			this.fileTree.startEditingAtPath(this.fileTree.getSelectionPath());
		});
		menu.add(rename);

		JMenuItem copyPath = new JMenuItem("Copy Path");
		copyPath.addActionListener(e -> {
			AssetFolder selected = this.getSelectedFolder();
			if (selected != null) {
				StringSelection selection = new StringSelection(selected.getFile().getPath());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
			}
		});
		menu.add(copyPath);

		return menu;
	}

	private AssetFolder getSelectedFolder() {
		return (AssetFolder) fileTree.getLastSelectedPathComponent();
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
			File oldFile = oldTmp.getFile();
			String newName = (String) newValue;
			File newFile = new File(oldFile.getParentFile(), newName);
			oldFile.renameTo(newFile);
			System.out.println("Renamed '" + oldFile + "' to '" + newFile + "'.");
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

		private final File mFile;

		public AssetFolder(final File file) {
			mFile = file;
		}

		public boolean isDirectory() {
			return mFile.isDirectory();
		}

		public AssetFolder[] getSubFolders() {
			File[] directories = this.mFile.listFiles(new FolderFilter());
			AssetFolder[] folders = new AssetFolder[directories.length];
			for (int i = 0; i < directories.length; i++) {
				File f = directories[i];
				folders[i] = new AssetFolder(f);
			}
			return folders;
		}

		public AssetFolder[] listFiles() {
			File[] files = mFile.listFiles();
			if (files == null) {
				return null;
			}
			if (files.length < 1) {
				return new AssetFolder[0];
			}

			AssetFolder[] ret = new AssetFolder[files.length];
			for (int i = 0; i < ret.length; i++) {
				final File f = files[i];
				ret[i] = new AssetFolder(f);
			}
			return ret;
		}

		public File getFile() {
			return mFile;
		}

		@Override
		public String toString() {
			return mFile.getName();
		}
	}
}