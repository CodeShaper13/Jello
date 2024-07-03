package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.logging.ILogHandler;
import com.codeshaper.jello.engine.logging.LogEntry;
import com.codeshaper.jello.engine.logging.LogType;

import ModernDocking.Dockable;
import ModernDocking.app.Docking;

public class ConsoleWindow extends EditorWindow implements ILogHandler {

	private JScrollPane logScrollPane;
	private DefaultListModel<LogEntry> listModel;
	private JList<LogEntry> console;

	private JScrollPane traceScrollPane;
	private DefaultListModel<StackTraceElement> traceModel;
	private JList<StackTraceElement> traceTextArea;
	
	public ConsoleWindow() {
		super("Console", "console");
		
		this.setLayout(new BorderLayout());

		// Toolbar.
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setAlignmentX(-1);

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> {
			this.clear();
		});
		toolbar.add(clearButton);

		toolbar.addSeparator();

		JToggleButton toggleClearOnPlay = new JToggleButton("Clear on Play");
		toolbar.add(toggleClearOnPlay);

		toolbar.addSeparator();

		JToggleButton toggleShowMsg = new JToggleButton("Show Msgs");
		toolbar.add(toggleShowMsg);

		JToggleButton toggleShowWarnings = new JToggleButton("Show Warnings");
		toolbar.add(toggleShowWarnings);

		JToggleButton toggleShowErrors = new JToggleButton("Show Errors");
		toolbar.add(toggleShowErrors);

		this.add(toolbar, BorderLayout.NORTH);

		
		// Top.
		JPanel top = new JPanel(new BorderLayout());

		this.listModel = new DefaultListModel<LogEntry>();
		this.console = new JList<LogEntry>(this.listModel);
		this.console.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.console.setLayoutOrientation(JList.VERTICAL);
		this.console.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				LogEntry entry = console.getSelectedValue();
				if(entry != null) {
					setVisibleTrace(entry);
				}
			}
		});

		this.logScrollPane = new JScrollPane(this.console);
		top.add(this.logScrollPane, BorderLayout.CENTER);

		
		// Bottom.
		this.traceModel = new DefaultListModel<StackTraceElement>();
		this.traceTextArea = new JList<StackTraceElement>(this.traceModel);// new JTextArea(10, 10);
		this.traceScrollPane = new JScrollPane(this.traceTextArea);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.logScrollPane, this.traceScrollPane);

		this.add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation(100);

		//System.setOut(new PrintStream(new LoggerOutputStream(this, LogType.NORMAL)));
		//System.setErr(new PrintStream(new LoggerOutputStream(this, LogType.ERROR)));
	}

	public void log(LogType type, GameObject context, String text, StackTraceElement[] trace) {
		this.listModel.addElement(new LogEntry(type, context, text, trace));

		// Scroll to the bottom.
		this.console.ensureIndexIsVisible(this.listModel.getSize() - 1);
	}

	/**
	 * Clears the console.
	 */
	public void clear() {
		this.listModel.clear();
		this.setVisibleTrace(null);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(300, 50);
	}

	@Override
	public boolean isWrappableInScrollpane() {
		return false;
	}

	private void setVisibleTrace(LogEntry entry) {
		this.traceModel.clear();

		if(entry != null) {
			if(entry.trace != null) {
				for(StackTraceElement element : entry.trace) {
					this.traceModel.addElement(element);
				}				
			}
		}
	}
}