package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.logging.ILogHandler;
import com.codeshaper.jello.engine.logging.LogEntry;
import com.codeshaper.jello.engine.logging.LogType;

public class ConsoleWindow extends EditorWindow implements ILogHandler {

	private static final int MAX_CONSOLE_ENTRY_COUNT = 1000;
	
	private final Icon messageIcon;
	private final Icon warningIcon;
	private final Icon errorIcon;
	private final SimpleDateFormat dateFormat;
	private final JToggleButton toggleClearOnPlay;
	private final JToggleButton toggleShowMessage;
	private final JToggleButton toggleShowWarning;
	private final JToggleButton toggleShowError;
	private final JScrollPane logScrollPane;
	private final LogEntryModel listModel;
	private final ListLogEntry logEntryList;
	private final JScrollPane traceScrollPane;
	private final DefaultListModel<String> traceModel;
	private final JList<String> traceTextArea;

	private LogEntry selectedEntry;

	public ConsoleWindow() {
		super("Console", "console");

		this.setLayout(new BorderLayout());

		this.messageIcon = UIManager.getIcon("OptionPane.informationIcon");
		this.warningIcon = UIManager.getIcon("OptionPane.warningIcon");
		this.errorIcon = UIManager.getIcon("OptionPane.errorIcon");
		this.dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

		// Top.
		this.listModel = new LogEntryModel(MAX_CONSOLE_ENTRY_COUNT);
		this.logEntryList = new ListLogEntry(this.listModel);
		this.logScrollPane = new JScrollPane(this.logEntryList);

		// Bottom.
		this.traceModel = new DefaultListModel<String>();
		this.traceTextArea = new JList<String>(this.traceModel);
		this.traceScrollPane = new JScrollPane(this.traceTextArea);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.logScrollPane, this.traceScrollPane);
		this.add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation(100);

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
		toolbar.add(this.toggleClearOnPlay = new JToggleButton("Clear on Play"));
		this.toggleClearOnPlay.setSelected(true);
		toolbar.addSeparator();
		toolbar.add(this.toggleShowMessage = new JToggleButton(this.messageIcon));
		this.toggleShowMessage.addActionListener(e -> this.logEntryList.repaint());
		this.toggleShowMessage.setSelected(true);
		toolbar.add(this.toggleShowWarning = new JToggleButton(this.warningIcon));
		this.toggleShowWarning.addActionListener(e -> this.logEntryList.repaint());
		this.toggleShowWarning.setSelected(true);
		toolbar.add(this.toggleShowError = new JToggleButton(this.errorIcon));
		this.toggleShowError.setSelected(true);
		this.toggleShowError.addActionListener(e -> this.logEntryList.repaint());

		this.add(toolbar, BorderLayout.NORTH);

		// System.setOut(new PrintStream(new LoggerOutputStream(this, LogType.NORMAL)));
		// System.setErr(new PrintStream(new LoggerOutputStream(this, LogType.ERROR)));
	}

	/**
	 * Logs a message to the console.
	 */
	public void log(LogEntry entry) {
		this.listModel.addElement(entry);

		if (this.selectedEntry == null) {
			this.logEntryList.scrollLogToBottom();
		}
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
		this.selectedEntry = entry;

		this.traceModel.clear();
		if (entry != null ) {
			if (entry.trace != null) {
				for (String element : entry.trace) {
					this.traceModel.addElement(element);
				}
			}
			
			if(entry.context instanceof GameObject) {
				// TODO highlight in hierarchy.
			} else if(entry.context instanceof Component) {
				// TODO highlight in hierarchy.
			} else if(entry.context instanceof Asset) {
				// TODO highlight in file browser.
			}
		}
	}

	private class LogEntryModel extends AbstractListModel<LogEntry> {

		private final int maxSize;
		private final LinkedList<LogEntry> entries;

		public LogEntryModel(int maxSize) {
			this.maxSize = maxSize;
			this.entries = new LinkedList<LogEntry>();
		}

		public void addElement(LogEntry entry) {
			if(this.entries.size() >= this.maxSize) {
				// Remove the oldest entry.
		        this.entries.removeFirst();
		        fireIntervalRemoved(this, 0, 0);
			}			
			
			int index = this.entries.size();
			this.entries.add(entry);
			this.fireIntervalAdded(this, index, index);
		}

		public void clear() {
			int index1 = this.entries.size() - 1;
			this.entries.clear();
			if (index1 >= 0) {
				this.fireIntervalRemoved(this, 0, index1);
			}
		}

		@Override
		public int getSize() {
			int size = 0;

			for (int i = 0; i < this.entries.size(); i++) {
				LogEntry entry = this.entries.get(i);
				boolean flag1 = entry.logType == LogType.NORMAL && toggleShowMessage.isSelected();
				boolean flag2 = entry.logType == LogType.WARNING && toggleShowWarning.isSelected();
				boolean flag3 = entry.logType == LogType.ERROR && toggleShowError.isSelected();
				if (flag1 || flag2 || flag3) {
					size++;
				}
			}
			return size;
		}

		@Override
		public LogEntry getElementAt(int index) {
			for (int i = 0; i <= index; i++) {
				LogEntry entry = this.entries.get(i);
				boolean flag1 = entry.logType == LogType.NORMAL && !toggleShowMessage.isSelected();
				boolean flag2 = entry.logType == LogType.WARNING && !toggleShowWarning.isSelected();
				boolean flag3 = entry.logType == LogType.ERROR && !toggleShowError.isSelected();
				if (flag1 || flag2 || flag3) {
					index++;
				}
			}
			return this.entries.get(index);
		}
	}

	private class ListLogEntry extends JList<LogEntry> {

		private ListModel<LogEntry> model;

		public ListLogEntry(ListModel<LogEntry> model) {
			super(model);

			this.model = model;

			this.setCellRenderer(new LogEntryRenderer());
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.setLayoutOrientation(JList.VERTICAL);
			this.addListSelectionListener(e -> {
				LogEntry entry = logEntryList.getSelectedValue();
				if (entry != null) {
					setVisibleTrace(entry);
				}
			});
		}

		/**
		 * Scrolls the list to the bottom.
		 */
		public void scrollLogToBottom() {
			this.ensureIndexIsVisible(this.model.getSize() - 1);
		}

		private class LogEntryRenderer extends JLabel implements ListCellRenderer<LogEntry> {

			public LogEntryRenderer() {
				this.setPreferredSize(new Dimension(this.getPreferredSize().width, 20));
				this.setOpaque(true);
			}

			@Override
			public Component getListCellRendererComponent(JList<? extends LogEntry> list, LogEntry entry, int index,
					boolean isSelected, boolean cellHasFocus) {
				switch (entry.logType) {
				case NORMAL:
					this.setIcon(messageIcon);
					break;
				case WARNING:
					this.setIcon(warningIcon);
					break;
				case ERROR:
					this.setIcon(errorIcon);
					break;
				default:
					this.setIcon(null);
					break;
				}

				this.setText(String.format("[%s] %s", dateFormat.format(entry.time), entry.text));

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