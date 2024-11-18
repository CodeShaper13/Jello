package com.codeshaper.jello.editor.property.drawer;

import java.awt.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.codeshaper.jello.editor.GuiBuilder;
import com.codeshaper.jello.editor.GuiLayoutBuilder;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.event.JelloEvent;
import com.codeshaper.jello.engine.event.ListenerJavaMethod;

@FieldDrawerType(JelloEvent.class)
public class JelloEventDrawer extends FieldDrawer {
	
	@Override
	public JPanel draw(IExposedField field) {
		GuiLayoutBuilder builder = new GuiLayoutBuilder();
		
		boolean isEnabled = !field.isReadOnly();
		
		JLabel label = GuiBuilder.label(field);
		builder.setBorder(label.getText() + "()");
		builder.getPanel().setToolTipText(label.getToolTipText());
		builder.getPanel().setEnabled(isEnabled);
				
		@SuppressWarnings("unchecked")
		List<ListenerJavaMethod> callbacks = (List<ListenerJavaMethod>) field.getSubProperty("javaMethods").get();
		
		for(int i = 0; i < callbacks.size(); i++) {
			ListenerJavaMethod callback = callbacks.get(i);
			JPopupMenu menu = new JPopupMenu();
			JMenuItem item = new JMenuItem("Remove Listener");
			item.addActionListener((e1) -> {
				callbacks.remove(callback);
				this.refreshInspector();
			});
			menu.add(item);
			
			builder.startHorizontal().setComponentPopupMenu(menu);
			JComboBox<GameObject> c = GuiBuilder.gameObjectField(callback.getGameObject(), (v) -> {
				callback.setGameObject(v);
				this.refreshInspector();
			});
			c.setComponentPopupMenu(menu);
			builder.add(c);
			
			JComboBox<Method> methodListComboBox = this.createMethodComboBox();
						
			if(callback.getGameObject() != null) {
				for(JelloComponent component : callback.getGameObject().getAllComponents()) {
					for(Method method : component.getClass().getMethods()) {
						if(method.getParameterCount() == 0) {
							methodListComboBox.addItem(method);
						}
					}
				}
			}

			methodListComboBox.setSelectedItem(callback.method);
			methodListComboBox.setComponentPopupMenu(menu);
			methodListComboBox.addActionListener(e -> {
				if (methodListComboBox.getSelectedIndex() == 0) {
					callback.method = null;
				} else {
					callback.method = (Method) methodListComboBox.getSelectedItem();
				}
			});
			
			builder.add(methodListComboBox);
			builder.endHorizontal();
			
			if(i < (callbacks.size() - 1)) {
				builder.space(8);
			}
		}
		
		builder.startHorizontal();
		builder.space();
		builder.button("Add Listener", null, () -> {
			callbacks.add(new ListenerJavaMethod());
			this.refreshInspector();
			
			System.out.println(callbacks.size());
		});
		builder.space();
		builder.endHorizontal();
		
		return (JPanel) builder.getPanel();
	}
	
	public JComboBox<Method> createMethodComboBox() {
		JComboBox<Method> methodComboBox = new JComboBox<Method>();
		methodComboBox.setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList<?> list, Object v, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, v, index, isSelected, cellHasFocus);

				if(v == null) {
					this.setText("None");
				} else if(v instanceof Method) {
					Method method = (Method)v;
					String s = "";
					Parameter[] params = method.getParameters();						
					for(int i = 0; i < params.length; i++) {
						s += params[i].getType().getSimpleName();
						if(i != params.length - 1) {
							s += ", ";
						}
					}
					method.toString();
					this.setText(String.format(
							"%s.%s(%s)",
							method.getDeclaringClass().getSimpleName(),
							method.getName(),
							s));
				}
				return this;
			}
		});
		
		methodComboBox.addItem(null);
		
		return methodComboBox;
	}
	
	private void refreshInspector() {
		JelloEditor.getWindow(InspectorWindow.class).refresh();
	}
}
