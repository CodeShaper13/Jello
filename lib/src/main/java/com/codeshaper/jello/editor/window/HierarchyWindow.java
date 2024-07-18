package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.Scene;

public class HierarchyWindow extends EditorWindow {

	private JTree tree;
	private DefaultMutableTreeNode root;
	private JPopupMenu generalPopup;
	private JPopupMenu nodePopup;

	public HierarchyWindow() {
		super("Hierarchy", "hierarchy");

		this.setLayout(new BorderLayout());

		this.generalPopup = this.createEmptyPopup();
		this.nodePopup = this.createNodePopup();

		this.root = new DefaultMutableTreeNode("Scene");
		this.tree = new JTree(root) {
			@Override
			public Point getPopupLocation(MouseEvent e) {
				if (e != null) {
					TreePath path = this.getPathForLocation(e.getX(), e.getY());
					if (path == null) {
						// Clicked nothing.
						this.setComponentPopupMenu(generalPopup);
					} else {
						// Clicked node.
						this.setComponentPopupMenu(nodePopup);
					}
					return e.getPoint();
				}
				return null;
			}

		};

		this.add(new JScrollPane(this.tree), BorderLayout.CENTER);

		this.tree.setCellRenderer(new HierarchyTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object selectedNode = tree.getLastSelectedPathComponent();
				if (selectedNode instanceof GameObjectTreeNode node) {
					InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
					inspector.setTarget(node.gameObject);
				}
			}
		});

		this.tree.setComponentPopupMenu(this.generalPopup);
		this.tree.setRootVisible(false);

		JTextField textField = new JTextField();
		TreeCellEditor editor = new DefaultCellEditor(textField);
		editor.addCellEditorListener(new CellEditorListener() {
			@Override
			public void editingStopped(ChangeEvent e) {
				Object selectedNode = tree.getLastSelectedPathComponent();
				if (selectedNode instanceof GameObjectTreeNode node) {
					node.gameObject.name = editor.getCellEditorValue().toString();
				}
			}

			@Override
			public void editingCanceled(ChangeEvent e) {
			}
		});
		tree.setEditable(true);
		tree.setCellEditor(editor);

		JelloEditor.instance.addSceneChangeListener((oldScene, newScene) -> {
			rebuildHierarchy();
		});
	}

	public void rebuildHierarchy() {
		this.root.removeAllChildren();

		Scene scene = JelloEditor.instance.getScene();
		for (GameObject rootObject : scene.getRootGameObjects()) {
			this.func(this.root, rootObject);
		}

		((DefaultTreeModel) this.tree.getModel()).reload(root);
	}

	private void func(DefaultMutableTreeNode node, GameObject gameObject) {
		GameObjectTreeNode n = new GameObjectTreeNode(gameObject);
		node.add(new GameObjectTreeNode(gameObject));
		for (GameObject child : gameObject.getChildren()) {
			this.func(n, child);
		}
	}

	private JPopupMenu createEmptyPopup() {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem item = new JMenuItem("New GameObject");
		item.addActionListener(e -> {
			Scene scene = JelloEditor.instance.getScene();
			scene.instantiateGameObject("New Child");

			this.rebuildHierarchy();
		});
		menu.add(item);

		return menu;
	}

	private JPopupMenu createNodePopup() {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem item = new JMenuItem("New Child");
		item.addActionListener(e -> {
			// TODO
		});
		menu.add(item);

		JMenuItem item2 = new JMenuItem("Rename");
		item2.addActionListener(e -> {
			this.tree.startEditingAtPath(tree.getSelectionPath());
		});
		menu.add(item2);

		JMenuItem item3 = new JMenuItem("Delete");
		item2.addActionListener(e -> {
			// TODO
		});
		menu.add(item3);

		return menu;
	}

	private class GameObjectTreeNode extends DefaultMutableTreeNode {

		private final GameObject gameObject;

		public GameObjectTreeNode(GameObject gameObject) {
			super(gameObject);

			this.gameObject = gameObject;
		}

		@Override
		public String toString() {
			return this.gameObject.name;
		}
	}

	private class HierarchyTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			// This fixes the text being cut off with "..." when a GameObjects name changes.
			Dimension size = getPreferredSize();
			component.setMinimumSize(size);
			component.setPreferredSize(size);

			return component;
		}
	}
}
