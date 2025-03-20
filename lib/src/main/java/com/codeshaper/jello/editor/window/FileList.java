package com.codeshaper.jello.editor.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.asset.Asset;

public class FileList extends JPanel {

	private final DefaultListModel<File> model;
	private final JList<File> fileList;
	private final FileBrowserPopupMenu popup;

	private boolean showExtensions;
	private ITarget target;

	public FileList(FileBrowserPopupMenu popup) {
		this.setLayout(new BorderLayout());

		this.popup = popup;
		this.model = new DefaultListModel<File>();
		this.fileList = new ListFile(this.model);

		this.add(this.fileList, BorderLayout.CENTER);
	}

	public void refresh() {
		this.setTarget(this.target);
	}

	public void setTarget(File directory) {
		this.setTarget(new ITarget() {

			@Override
			public File getTagetDirectory() {
				return directory;
			}

			@Override
			public Collection<File> getFiles() {
				return FileUtils.listFiles(directory, FileFileFilter.INSTANCE, null);
			}
		});
	}

	public void setTarget(File directory, String search) {
		this.setTarget(new ITarget() {

			@Override
			public File getTagetDirectory() {
				return null;
			}

			@Override
			public Collection<File> getFiles() {
				WildcardFileFilter.Builder builder = WildcardFileFilter.builder();

				builder.setIoCase(IOCase.INSENSITIVE);
				builder.setWildcards("*" + search + "*");

				return FileUtils.listFiles(directory, builder.get(), TrueFileFilter.INSTANCE);
			}
		});
	}

	/**
	 * Checks if file extensions are being shown.
	 * 
	 * @return {@code true} if file extensions are shown
	 */
	public boolean isShowingExtensions() {
		return this.showExtensions;
	}

	/**
	 * Sets if file extensions are shown in the list.
	 * 
	 * @param show should file extensions be shown
	 */
	public void setShowExtensions(boolean show) {
		if (show == this.showExtensions) {
			return;
		}

		this.showExtensions = show;
		this.fileList.updateUI(); // Causes a redraw.
	}

	private void setTarget(ITarget target) {
		this.target = target;

		this.model.removeAllElements();

		if (this.target != null) {
			for (File file : this.target.getFiles()) {
				this.model.addElement(file);
			}
		}
	}

	private interface ITarget {

		Collection<File> getFiles();

		File getTagetDirectory();
	}

	private class ListFile extends JList<File> {

		public ListFile(DefaultListModel<File> model) {
			super(model);

			this.setCellRenderer(new ListFileRenderer());
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			this.setVisibleRowCount(-1);
			this.addListSelectionListener(e -> {
				InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
				File file = this.getSelectedValue();
				if (file != null) {
					Asset asset = JelloEditor.instance.assetDatabase.getAsset(new AssetLocation(file));
					inspector.setTarget(asset);
				}
			});
		}

		@Override
		public JPopupMenu getComponentPopupMenu() {
			File selected = this.getSelectedValue();
			if (selected != null) {
				popup.setTargetFile(selected);
				return popup;
			} else {
				File selectedDirecotry = target.getTagetDirectory();
				if (selectedDirecotry != null) {
					popup.setTargetFile(selectedDirecotry);
					return popup;
				} else {
					return super.getComponentPopupMenu();
				}
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

			String fileName = file.getName();
			this.setText(showExtensions ? fileName : FilenameUtils.removeExtension(fileName));

			if (isSelected) {
				this.setBackground(list.getSelectionBackground());
				this.setForeground(list.getSelectionForeground());
			} else {
				this.setBackground(list.getBackground());
				this.setForeground(list.getForeground());
			}

			FileSystemView fileSystemView = FileSystemView.getFileSystemView();
		    Icon icon = fileSystemView.getSystemIcon(file);
	        this.setIcon(icon);
			
			return this;
		}
	}
}
