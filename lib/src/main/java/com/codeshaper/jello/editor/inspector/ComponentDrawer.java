package com.codeshaper.jello.editor.inspector;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.component.JelloComponent;

public class ComponentDrawer<T extends JelloComponent> {

	protected final T component;

	public ComponentDrawer(T component) {
		this.component = component;
	}

	public void makeGui(JPanel panel) {
		panel.add(this.getHeader());

		panel.add(new JSeparator());

		GuiDrawer drawer = new GuiDrawer();
		this.drawComponent(drawer);

		this.drawButtons(drawer);
		
		panel.add(drawer.getPanel());
	}

	/**
	 * Gets the header for the component.
	 * 
	 * @return
	 */
	public ComponentHeader getHeader() {
		return new ComponentHeader(this.component);
	}

	/**
	 * Draws the component. By default, {@link GuiDrawer#drawObject(Object)} is
	 * called to draw all exposed fields.
	 * 
	 * @param builder
	 */
	protected void drawComponent(GuiDrawer drawer) {
		drawer.drawObject(this.component);
	}

	protected void drawButtons(GuiDrawer drawer) {
		// Draw buttons.
		for (Method method : this.component.getClass().getDeclaredMethods()) {
			Button buttonAnnotation = method.getAnnotation(Button.class);
			if (buttonAnnotation != null) {
				if (method.getParameterCount() != 0) {
					Debug.logError("Methods with the Button annotation must have 0 parameters.");
					continue;
				}

				String buttonText = buttonAnnotation.value();
				drawer.drawButton(
						buttonText.isBlank() ? method.getName() : buttonText,
						null,
						() -> {
							try {
								if (method.trySetAccessible()) {
									method.invoke(this.component);
								} else {
									// TODO should a message be logged here?
								}
							} catch (SecurityException | IllegalAccessException | IllegalArgumentException
									| InvocationTargetException e1) {
								e1.printStackTrace();
							}
						});
			}
		}
	}

	/**
	 * Gets a help URL to online documentation for the component. If null is
	 * returned, the help button in the header will be disabled.
	 * 
	 * @return
	 */
	public String getHelpUrl() {
		return "https://www.google.com/search?q=help"; // TODO
	}

	public class ComponentHeader extends JPanel {

		public static ImageIcon helpIcon = new ImageIcon(
				ComponentDrawer.class.getResource("/editorIcons/component_help.png"));
		public static ImageIcon editIcon = new ImageIcon(
				ComponentDrawer.class.getResource("/editorIcons/component_edit.png"));
		public static ImageIcon removeIcon = new ImageIcon(
				ComponentDrawer.class.getResource("/editorIcons/component_remove.png"));

		public ComponentHeader(T component) {
			this.setLayout(new GridBagLayout());

			JCheckBox toggle = new JCheckBox();
			toggle.setSelected(component.isEnabled());
			toggle.addActionListener(e -> {
				component.setEnabled(toggle.isSelected());
			});

			JLabel componentName = new JLabel(component.getClass().getSimpleName(), SwingConstants.LEFT);

			GridBagConstraints labelConstraint = new GridBagConstraints();
			labelConstraint.weightx = 1;

			this.add(toggle);
			this.add(componentName, labelConstraint);

			String url = getHelpUrl();
			boolean hasHelpLink = !StringUtils.isWhitespace(url);
			JButton btn = this.addButton(helpIcon, "Open Online Help", e -> {
				if (hasHelpLink) {
					try {
						Desktop.getDesktop().browse(new URI(url));
					} catch (Exception exception) {
					}
				}
			});
			btn.setEnabled(hasHelpLink);

			this.addButton(editIcon, "Edit Component in IDE", e -> {
				// TODO open in IDE.
			});

			this.addButton(removeIcon, "Remove Component", e -> {
				component.gameObject.removeComponent(component);
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
		public JButton addButton(Icon icon, String tooltip, ActionListener l) {
			JButton btn = new JButton(icon);
			btn.addActionListener(l);
			btn.setToolTipText(tooltip);

			this.add(btn);
			this.add(Box.createHorizontalStrut(5));

			return btn;
		}
	}
}
