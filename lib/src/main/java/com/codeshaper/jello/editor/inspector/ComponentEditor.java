package com.codeshaper.jello.editor.inspector;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.codeshaper.jello.editor.EditorUtils;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.gui.GuiLayoutBuilder;
import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.ComponentHelpUrl;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;

public class ComponentEditor<T extends JelloComponent> extends Editor<T> {

	public static final ImageIcon moveUpIcon = new ImageIcon(
			ComponentEditor.class.getResource("/editor/icons/component_moveUp.png"));
	public static final ImageIcon moveDownIcon = new ImageIcon(
			ComponentEditor.class.getResource("/editor/icons/component_moveDown.png"));
	public static final ImageIcon helpIcon = new ImageIcon(
			ComponentEditor.class.getResource("/editor/icons/component_help.png"));
	public static final ImageIcon editIcon = new ImageIcon(
			ComponentEditor.class.getResource("/editor/icons/component_edit.png"));
	public static final ImageIcon removeIcon = new ImageIcon(
			ComponentEditor.class.getResource("/editor/icons/component_remove.png"));
	
	public ComponentEditor(T component, JPanel panel) {
		super(component, panel);
	}
	
	@Override
	protected void onDraw() {
		this.panel.add(this.createHeader(target));
		this.panel.add(new JSeparator());
		
		GuiLayoutBuilder drawer = new GuiLayoutBuilder();
		this.drawComponent(drawer);
		this.panel.add(drawer.getPanel());
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
		
	private JPanel createHeader(T component) {
		GuiLayoutBuilder builder = new GuiLayoutBuilder();
		builder.startHorizontal();
		
		builder.checkbox(
				null,
				component.isEnabled(),
				(b) -> {component.setEnabled(component.isEnabled());});
		builder.glue();
		builder.label(component.getClass().getSimpleName(),
				 EditorUtils.getComponentIcon(component),
				SwingConstants.RIGHT);
		builder.glue();
		
		builder.button(null, moveUpIcon, () -> {
			if (component.gameObject().moveComponent(component, -1)) {
				JelloEditor.getWindow(InspectorWindow.class).refresh();
			}
		}).setTooltip("Move Component Up").setDisabled(component.gameObject().getComponentAtIndex(0) == component);
		
		builder.space(5);
		
		builder.button(null, moveDownIcon, () -> {
			if(component.gameObject().moveComponent(component, 1)) {
				JelloEditor.getWindow(InspectorWindow.class).refresh();
			}
		}).setTooltip("Move Component Down").setDisabled(component.gameObject().getComponentAtIndex(component.gameObject().getComponentCount() - 1) == component);

		builder.space(10);

		ComponentHelpUrl help = target.getClass().getAnnotation(ComponentHelpUrl.class);			
		builder.button(null, helpIcon, () -> {
			if (help != null && help.value() != null) {
				try {
					Desktop.getDesktop().browse(new URI(help.value()));
				} catch (Exception exception) {
					Debug.logError("Unable to open online help");
				}
			}
		}).setTooltip("Open Online Help").setDisabled(!(help != null && help.value() != null));

		builder.space(5);
		
		File file = JelloEditor.instance.assetDatabase.compiler.getSourceFile(component.getClass());
		builder.button(null, editIcon, () -> {
			if(file != null) {
				String ideLocation = JelloEditor.instance.settings.ideLocation;					
				try {						
					Runtime.getRuntime().exec(new String[] {
						    ideLocation,
						    "--launcher.openFile",
						    file.getPath()
						});
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}).setTooltip("Edit Component in IDE").setDisabled(file == null);
		
		builder.space(5);
		
		builder.button(null, removeIcon, () -> {
			component.destroy();
			JelloEditor.getWindow(InspectorWindow.class).refresh();
		}).setTooltip("Remove Componenet");
					
		builder.endHorizontal();
		
		return builder.getPanel();
	}
}
