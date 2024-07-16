package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JToolBar;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.database.AssetDatabase;

public class AssetDatabaseViewer extends EditorWindow {

	private final AssetDatabase database;
	private final JTable table;
	
	public AssetDatabaseViewer() {
		super("Asset Database Viewer", "assetDatabaseViewer");
	
		this.database = JelloEditor.instance.assetDatabase;
		
		this.setLayout(new BorderLayout());
	
		JToolBar toolbar = new JToolBar();
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(e -> this.refresh());
		toolbar.add(btnRefresh);
		
		this.add(toolbar, BorderLayout.NORTH);
		
		this.table = new JTable();

		this.add(this.table, BorderLayout.CENTER);
		
		this.refresh();		
	}

	private void refresh() {
		this.table.setModel(this.database.getTableModel());
		this.table.getColumnModel().getColumn(2).setPreferredWidth(50);
		this.table.getColumnModel().getColumn(2).setPreferredWidth(20);
	}	
}
