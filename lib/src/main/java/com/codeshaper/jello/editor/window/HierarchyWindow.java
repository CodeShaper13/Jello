package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
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

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.ComponentDrawer;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.asset.Scene;

public class HierarchyWindow extends EditorWindow {

	private static final ImageIcon SCENE_ICON = new ImageIcon(
			ComponentDrawer.class.getResource("/editor/icons/hierarchyScene.png"));
	private static final ImageIcon GAME_OBJECT_ICON = new ImageIcon(
			ComponentDrawer.class.getResource("/editor/icons/hierarchyGameObject.png"));

	private HierarchyTreeModel model;
	private HierarchyTree tree;

	public HierarchyWindow() {
		super("Scene Hierarchy", "hierarchy");

		this.setLayout(new BorderLayout());

		this.model = new HierarchyTreeModel(JelloEditor.instance.sceneManager);
		this.tree = new HierarchyTree(this.model);

		this.add(new JScrollPane(this.tree), BorderLayout.CENTER);

		JelloEditor.instance.addSceneChangeListener((oldScene, newScene) -> {
			this.model.reload();
		});
	}

	@Override
	public void addMoreOptions(JPopupMenu menu) {
		JMenuItem refresh = new JMenuItem("Refresh");
		refresh.addActionListener(e -> {
			this.model.reload();
		});
		menu.add(refresh);
	}

	private class HierarchyTree extends JTree {

		public HierarchyTree(TreeModel model) {
			super(model);

			this.setEditable(true);
			this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "startEditing");
			this.setRootVisible(false);
			this.setShowsRootHandles(true);

			DefaultTreeCellRenderer renderer = new HierarchyTreeCellRenderer();
			this.setCellEditor(new HierarchyTreeEditor(this, renderer));
			this.setCellRenderer(renderer);

			TreeSelectionModel selectionModel = this.getSelectionModel();
			selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			selectionModel.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					Object selectedNode = tree.getLastSelectedPathComponent();
					if (selectedNode instanceof GameObject gameObject) {
						InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
						inspector.setTarget(gameObject);
					}
				}
			});
		}

		@Override
		public JPopupMenu getComponentPopupMenu() {
			Object node = this.getLastSelectedPathComponent();
			if (node instanceof Scene) {
				return new SceneMenu((Scene) node);
			} else if (node instanceof GameObject) {
				return new GameObjectMenu((GameObject) node);
			} else {
				return null;
			}
		}
	}

	private class HierarchyTreeModel implements TreeModel {

		private final ArrayList<TreeModelListener> listeners;
		private SceneManager sceneManager;

		public HierarchyTreeModel(SceneManager sceneManager) {
			this.sceneManager = sceneManager;
			this.listeners = new ArrayList<>();
		}

		@Override
		public Object getRoot() {
			return this.sceneManager;
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof SceneManager) {
				return ((SceneManager) parent).getLoadedScene(index);
			} else if (parent instanceof Scene) {
				return ((Scene) parent).getRootGameObject(index);
			} else if (parent instanceof GameObject) {
				return ((GameObject) parent).getChild(index);
			} else {
				return null;
			}
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent instanceof SceneManager) {
				return ((SceneManager) parent).getLoadedSceneCount();
			} else if (parent instanceof Scene) {
				return ((Scene) parent).getRootGameObjectCount();
			} else if (parent instanceof GameObject) {
				return ((GameObject) parent).getChildCount();
			} else {
				return 0;
			}
		}

		@Override
		public boolean isLeaf(Object node) {
			return this.getChildCount(node) == 0;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			Object node = path.getLastPathComponent();
			if (node instanceof GameObject) {
				((GameObject) node).setName((String) newValue);
			}
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent instanceof SceneManager) {
				SceneManager manager = (SceneManager) parent;
				for (int i = 0; i < manager.getLoadedSceneCount(); i++) {
					if (manager.getLoadedScene(i) == child) {
						return i;
					}
				}
			} else if (parent instanceof Scene) {
				Scene scene = (Scene) parent;
				for (int i = 0; i < scene.getRootGameObjectCount(); i++) {
					if (scene.getRootGameObject(i) == child) {
						return i;
					}
				}
			} else if (parent instanceof GameObject) {
				GameObject gameObject = (GameObject) parent;
				for (int i = 0; i < gameObject.getChildCount(); i++) {
					if (gameObject.getChild(i) == child) {
						return i;
					}
				}
			}

			return -1;
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			this.listeners.add(l);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			this.listeners.remove(l);
		}

		public void reloadSelected() {
			this.raiseEvent(new TreeModelEvent(this, tree.getSelectionPath()));
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
			// Need to duplicate the code because the root can formally be
			// no an instance of the TreeNode.
			int n = this.getChildCount(getRoot());
			int[] childIdx = new int[n];
			Object[] children = new Object[n];

			for (int i = 0; i < n; i++) {
				childIdx[i] = i;
				children[i] = getChild(getRoot(), i);
			}

			this.fireTreeStructureChanged(this, new Object[] { getRoot() }, childIdx, children);
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
			this.raiseEvent(event);
		}
		
		private void raiseEvent(TreeModelEvent event) {
			for (TreeModelListener listener : this.listeners) {
				listener.treeStructureChanged(event);
			}
		}
	}

	private class HierarchyTreeEditor extends DefaultTreeCellEditor {

		public HierarchyTreeEditor(JTree tree, DefaultTreeCellRenderer renderer) {
			super(tree, renderer);
		}

		@Override
		public boolean isCellEditable(EventObject e) {
			return super.isCellEditable(e) && this.lastPath.getLastPathComponent() instanceof GameObject;
		}
	}

	private class HierarchyTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			// This fixes the text being cut off with "..." when a GameObjects name changes.
			Dimension size = this.getPreferredSize();
			component.setMinimumSize(size);
			component.setPreferredSize(size);

			if (value instanceof Scene) {
				this.setIcon(SCENE_ICON);
				this.setText(((Scene) value).getSceneName());
			} else if (value instanceof GameObject) {
				this.setIcon(GAME_OBJECT_ICON);
				this.setText(((GameObject) value).getName());
			}

			return component;
		}
	}

	private class SceneMenu extends JPopupMenu {

		public SceneMenu(Scene scene) {
			JMenuItem save = new JMenuItem("Save");
			save.addActionListener(e -> {
				JelloEditor.instance.sceneManager.saveScene(scene);
			});
			this.add(save);
			JMenuItem remove = new JMenuItem("Remove");
			remove.addActionListener(e -> {
				JelloEditor.instance.sceneManager.unloadScene(scene);
			});
			this.add(remove);

			this.addSeparator();

			JMenuItem add = new JMenuItem("New GameObject");
			add.addActionListener(e -> {
				scene.instantiateGameObject("New GameObject");
				model.reloadSelected();
			});
			this.add(add);
		}
	}
	

	private class GameObjectMenu extends JPopupMenu {

		public GameObjectMenu(GameObject gameObject) {
			JMenuItem newChild = new JMenuItem("New Child");
			newChild.addActionListener(e -> {
				// TODO
			});
			this.add(newChild);

			JMenuItem rename = new JMenuItem("Rename");
			rename.addActionListener(e -> {
				tree.startEditingAtPath(tree.getSelectionPath());
			});
			this.add(rename);

			JMenuItem delete = new JMenuItem("Delete");
			delete.addActionListener(e -> {
				// TODO
			});
			this.add(delete);
		}
	}
}
