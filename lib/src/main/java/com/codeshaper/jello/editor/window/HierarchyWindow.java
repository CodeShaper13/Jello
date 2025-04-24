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
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.inspector.ComponentEditor;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloObject;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.SceneManager;
import com.codeshaper.jello.engine.database.AssetDatabase;
import com.codeshaper.jello.engine.database.Serializer;
import com.google.gson.JsonElement;

public class HierarchyWindow extends EditorWindow {

	private static final ImageIcon SCENE_ICON = new ImageIcon(
			ComponentEditor.class.getResource("/editor/icons/hierarchy_scene.png"));
	private static final ImageIcon GAME_OBJECT_ICON = new ImageIcon(
			ComponentEditor.class.getResource("/editor/icons/hierarchy_gameObject.png"));

	private HierarchyTreeModel model;
	private HierarchyTree tree;
	private JsonElement coppiedGameObject;

	public HierarchyWindow() {
		super("Scene Hierarchy", "hierarchy");

		this.setLayout(new BorderLayout());

		this.model = new HierarchyTreeModel(JelloEditor.instance.sceneManager);
		this.tree = new HierarchyTree(this.model);

		this.add(this.tree, BorderLayout.CENTER);

		JelloEditor.instance.addSceneChangeListener((oldScene, newScene) -> {
			this.refreshEntireTree();
		});
	}

	@Override
	public void addMoreOptions(JPopupMenu menu) {
		JMenuItem refresh = new JMenuItem("Refresh");
		refresh.addActionListener(e -> {
			this.refreshEntireTree();
		});
		menu.add(refresh);
	}

	/**
	 * Gets the {@link GameObject} that is selected. If no GameObject is selected,
	 * or a {@link Scene} is selected, {@code null} is returned.
	 * 
	 * @return the selected GameObject
	 */
	public GameObject getSelected() {
		Object selected = this.tree.getLastSelectedPathComponent();
		if (selected instanceof GameObject) {
			return (GameObject) selected;
		}

		return null;
	}

	private void refreshEntireTree() {
		this.model.notifyOfStructureChange(new TreePath(this.tree.getModel().getRoot()));
		
		this.func(new TreePath(this.model.getRoot()));
	}
	
	private void func(TreePath path) {
		Object obj = path.getLastPathComponent();
		if(obj instanceof JelloObject && ((JelloObject)obj).isExpandedInHierarchy()) {
			this.tree.expandPath(path);			
		}
		
		int childCount = this.model.getChildCount(path.getLastPathComponent());
	    for (int i = 0; i < childCount; i++) {
	        Object childNode = this.model.getChild(path.getLastPathComponent(), i);
	        this.func(path.pathByAddingChild(childNode));
	    }
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
					if (selectedNode instanceof JelloObject) {
						InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
						inspector.setTarget((JelloObject) selectedNode);
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


		@Override
		protected void setExpandedState(TreePath path, boolean state) {
			super.setExpandedState(path, state);

			Object obj = path.getLastPathComponent();
			if(obj instanceof JelloObject) {
				((JelloObject) obj).setExpandedInHierarchy(state);
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
				return ((SceneManager) parent).getScene(index);
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
				return ((SceneManager) parent).getSceneCount();
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
				for (int i = 0; i < manager.getSceneCount(); i++) {
					if (manager.getScene(i) == child) {
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

		public void notifyOfNew(TreePath parentPath, GameObject newObj) {
			TreeModelEvent event = this.constructEvent(parentPath, newObj);
			for (TreeModelListener listener : this.listeners) {
				listener.treeNodesInserted(event);
			}
		}

		public void notifyOfRemoval(TreePath parentPath, GameObject removedObj) {
			TreeModelEvent event = this.constructEvent(parentPath, removedObj);
			for (TreeModelListener listener : this.listeners) {
				listener.treeNodesRemoved(event);
			}
		}

		public void notifyOfStructureChange(TreePath path) {
			TreeModelEvent event = new TreeModelEvent(this, path);
			for (TreeModelListener listener : this.listeners) {
				listener.treeStructureChanged(event);
			}
		}

		private TreeModelEvent constructEvent(TreePath parentPath, GameObject obj) {
			return new TreeModelEvent(this, parentPath, new int[] { obj.getIndexOf() }, new Object[] { obj });
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
			// getPreferedSize adds 3 to the width, so we take that away. Otherwise the
			// label expands every time it redraws and creates a horizontal scroll bar that
			// get wider and wider and wider...
			Dimension size = this.getPreferredSize();
			size.width -= 3;
			component.setPreferredSize(size);

			if (value instanceof Scene) {
				this.setIcon(SCENE_ICON);
				this.setText(((Scene) value).getAssetName());
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
				GameObject newObj = new GameObject("New GameObject", scene);
				model.notifyOfNew(tree.getSelectionPath(), newObj);
				tree.setSelectionPath(tree.getSelectionPath().pathByAddingChild(newObj));
			});
			this.add(add);
		}
	}

	private class GameObjectMenu extends JPopupMenu {

		private final GameObject gameObject;

		private void func(GameObject newObj, TreePath pathToParent) {
			model.notifyOfNew(pathToParent, newObj);
			tree.setSelectionPath(pathToParent.pathByAddingChild(newObj));
		}

		public GameObjectMenu(GameObject gameObject) {
			this.gameObject = gameObject;
			Serializer serializer = AssetDatabase.getInstance().serializer;

			JMenuItem copy = new JMenuItem("Copy");
			copy.addActionListener(e -> {
				coppiedGameObject = serializer.serializeToJsonElement(gameObject);
			});
			this.add(copy);

			JMenuItem paste = new JMenuItem("Paste");
			paste.setEnabled(coppiedGameObject != null);
			paste.addActionListener(e -> {
				if (coppiedGameObject != null) {
					GameObject newObj = this.addGameObjFromJson(coppiedGameObject, gameObject.getParent());
					this.func(newObj, tree.getSelectionPath().getParentPath());
				}
			});
			this.add(paste);

			JMenuItem pasteAsChild = new JMenuItem("Paste As Child");
			pasteAsChild.setEnabled(coppiedGameObject != null);
			pasteAsChild.addActionListener(e -> {
				if (coppiedGameObject != null) {
					GameObject newObj = this.addGameObjFromJson(coppiedGameObject, gameObject);
					this.func(newObj, tree.getSelectionPath());
				}
			});
			this.add(pasteAsChild);

			this.addSeparator();

			JMenuItem rename = new JMenuItem("Rename");
			rename.addActionListener(e -> {
				tree.startEditingAtPath(tree.getSelectionPath());
			});
			this.add(rename);

			JMenuItem duplicate = new JMenuItem("Duplicate");
			duplicate.addActionListener(e -> {
				JsonElement json = serializer.serializeToJsonElement(gameObject);
				GameObject newObj = this.addGameObjFromJson(json, gameObject.getParent());
				this.func(newObj, tree.getSelectionPath().getParentPath());
			});
			this.add(duplicate);

			JMenuItem delete = new JMenuItem("Delete");
			delete.addActionListener(e -> {
				InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
				if (inspector.getTarget() == gameObject) {
					inspector.setTarget(null);
				}
				model.notifyOfRemoval(tree.getSelectionPath().getParentPath(), gameObject);
				gameObject.destroy();
			});
			this.add(delete);

			this.addSeparator();

			JMenuItem newChild = new JMenuItem("New Child");
			newChild.addActionListener(e -> {
				GameObject newObj = new GameObject("New GameObject", gameObject);
				this.func(newObj, tree.getSelectionPath());
			});
			this.add(newChild);
		}

		private GameObject addGameObjFromJson(JsonElement json, GameObject parent) {
			GameObject newGameObject = GameObject.fromJson(json, this.gameObject.getScene());
			newGameObject.setName(newGameObject.getName() + "-Copy");
			newGameObject.setParent(parent);

			return newGameObject;
		}
	}
}
