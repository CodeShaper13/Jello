package com.codeshaper.jello.editor.inspector;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.ComponentHelpUrl;
import com.codeshaper.jello.engine.ComponentIcon;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;

public class ComponentEditor<T extends JelloComponent> extends Editor<T> {


	public ComponentEditor(T component, JPanel panel) {
		super(component, panel);
	}
	
	@Override
	protected void onDraw(boolean isInitialDraw) {
		this.panel.add(this.getHeader());
		this.panel.add(new JSeparator());
		
		GuiLayoutBuilder drawer = new GuiLayoutBuilder();
		this.drawComponent(drawer);
		this.panel.add(drawer.getPanel());
	}
	
	@Override
	protected void onRefresh() {
		super.onRefresh();
		
		this.panel.removeAll();
	}
	
	/**
	 * Gets the header for the component. Override to provide a custom header.
	 * 
	 * @return
	 */
	public ComponentHeader getHeader() {
		return new ComponentHeader(this.target);
	}

	/**
	 * Draws the component. By default, {@link GuiLayoutBuilder#addAll(Object)} is
	 * called to draw all exposed fields. Override to customize how the component is
	 * drawn.
	 * 
	 * @param builder
	 */
	protected void drawComponent(GuiLayoutBuilder builder) {
		builder.addAll(this.target);
	}

	public class ComponentHeader extends JPanel {

		public static ImageIcon moveUpIcon = new ImageIcon(
				ComponentEditor.class.getResource("/editor/icons/component_moveUp.png"));
		public static ImageIcon moveDownIcon = new ImageIcon(
				ComponentEditor.class.getResource("/editor/icons/component_moveDown.png"));
		public static ImageIcon helpIcon = new ImageIcon(
				ComponentEditor.class.getResource("/editor/icons/component_help.png"));
		public static ImageIcon editIcon = new ImageIcon(
				ComponentEditor.class.getResource("/editor/icons/component_edit.png"));
		public static ImageIcon removeIcon = new ImageIcon(
				ComponentEditor.class.getResource("/editor/icons/component_remove.png"));
		public static ImageIcon defaultComponentIcon = new ImageIcon(
				ComponentEditor.class.getResource(ComponentIcon.DEFAULT_ICON_PATH));

		public ComponentHeader(T component) {
			this.setLayout(new GridBagLayout());

			JCheckBox toggle = new JCheckBox();
			toggle.setSelected(component.isEnabled());
			toggle.addActionListener(e -> {
				component.setEnabled(toggle.isSelected());
			});

			String label = component.getClass().getSimpleName();
			Icon icon = this.getComponentIcon(component);
			JLabel componentName = new JLabel(label, icon, SwingConstants.RIGHT);

			GridBagConstraints labelConstraint = new GridBagConstraints();
			labelConstraint.weightx = 1;

			this.add(toggle);
			this.add(componentName, labelConstraint);

			this.addButton(moveUpIcon, "Move component up", e -> {
				if (component.gameObject().moveComponent(component, -1)) {
					JelloEditor.getWindow(InspectorWindow.class).refresh();
				}
			});
			this.addButton(moveDownIcon, "Move component down", e -> {
				if(component.gameObject().moveComponent(component, 1)) {
					JelloEditor.getWindow(InspectorWindow.class).refresh();
				}
			});

			this.add(Box.createHorizontalStrut(10));

			ComponentHelpUrl help = target.getClass().getAnnotation(ComponentHelpUrl.class);			
			JButton btn = this.addButton(helpIcon, "Open Online Help", e -> {
				if (help != null && help.value() != null) {
					try {
						Desktop.getDesktop().browse(new URI(help.value()));
					} catch (Exception exception) {
					}
				}
			});
			btn.setEnabled(help != null && help.value() != null);

			this.addButton(editIcon, "Edit Component in IDE", e -> {
				// TODO open in IDE.
			});

			this.addButton(removeIcon, "Remove Component", e -> {
				component.gameObject().removeComponent(component);
				JelloEditor.getWindow(InspectorWindow.class).refresh();
			});
		}

		/**
		 * Adds a button to the header.
		 * 
		 * @param icon    the buttons icon.
		 * @param tooltip the buttons tooltip.
		 * @param l       the event callback for when the button is clicked.
		 * @return
		 */
		protected JButton addButton(Icon icon, String tooltip, ActionListener l) {
			JButton btn = new JButton(icon);
			btn.addActionListener(l);
			btn.setToolTipText(tooltip);

			this.add(btn);
			this.add(Box.createHorizontalStrut(5));

			return btn;
		}

		private Icon getComponentIcon(JelloComponent component) {
			ComponentIcon annotation = component.getClass().getAnnotation(ComponentIcon.class);
			if (annotation != null) {
				String path = annotation.value();
				URL url = ComponentEditor.class.getResource(path);
				if (url != null) {
					return new ImageIcon(url);
				} else {
					Debug.logError("Couldn't load component icon at ", path);
					return defaultComponentIcon;
				}
			} else {
				return defaultComponentIcon;
			}
		}
	}
}
