package com.codeshaper.jello.editor.inspector;

import java.awt.Component;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;

import com.codeshaper.jello.editor.EditorUtils;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.event.JelloEvent1;
import com.codeshaper.jello.engine.event.JelloEvent1.IListener;

public class AddComponentDialog extends JDialog {

	private JelloEvent1<Class<JelloComponent>> onAddComponenetEvent;

	/**
	 * Creates the Add Component Dialog screen.
	 * <p>
	 * The created dialog is not automatically shown, use
	 * {@link JDialog#setVisible(boolean)} to show it.
	 * 
	 * @param onAdd a callback to invoke when a Component is selected in the dialog
	 */
	public AddComponentDialog(IListener<Class<JelloComponent>> onAdd) {
		super(JelloEditor.instance.window, "Add Component");

		this.onAddComponenetEvent = new JelloEvent1<Class<JelloComponent>>();
		this.onAddComponenetEvent.addListener(onAdd);

		DefaultListModel<Class<JelloComponent>> model = new DefaultListModel<Class<JelloComponent>>();

		Stream<Class<JelloComponent>> stream = StreamSupport
				.stream(JelloEditor.instance.assetDatabase.getallComponents().spliterator(), false)
				.sorted((object1, object2) -> {
					return this.getComponentDisplayName(object1)
							.compareToIgnoreCase(this.getComponentDisplayName(object2));
				});
		stream.forEach((e) -> model.addElement(e));

		JList<Class<JelloComponent>> list = new JList<Class<JelloComponent>>(model);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				onAddComponenetEvent.invoke(list.getSelectedValue());
				dispose();
			}
		});
		list.setCellRenderer(new CellRenderer());
		list.setFixedCellHeight(20);

		JScrollPane scrollPane = new JScrollPane(list);
		this.add(scrollPane);

		this.setSize(400, 500);
		this.setLocationRelativeTo(JelloEditor.instance.window);
	}

	private String getComponentDisplayName(Class<JelloComponent> componentClass) {
		ComponentName annotation = componentClass.getAnnotation(ComponentName.class);
		if (annotation != null && !StringUtils.isWhitespace(annotation.value())) {
			return annotation.value();
		} else {
			return componentClass.getName();
		}
	}

	private class CellRenderer extends JLabel implements ListCellRenderer<Class<JelloComponent>> {

		@Override
		public Component getListCellRendererComponent(JList<? extends Class<JelloComponent>> list,
				Class<JelloComponent> value, int index, boolean isSelected, boolean cellHasFocus) {
			this.setText(getComponentDisplayName(value));
			this.setIcon(EditorUtils.getComponentIcon(value));

			return this;
		}
	}
}
