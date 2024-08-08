package com.codeshaper.jello.editor;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;

import com.codeshaper.jello.editor.GuiLayoutBuilder.OnSubmitListerer;
import com.codeshaper.jello.editor.property.ExposedArrayField;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
import com.codeshaper.jello.editor.property.drawer.FieldDrawer;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.MaxValue;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.editor.swing.JNumberField.EnumNumberType;
import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.database.AssetDatabase;

/**
 * Provides a collection of static methods that create components for UIs.
 * <p>
 * This is a lower level alternative to using {@link GuiLayoutBuilder}.
 */
public class GuiBuilder {

	/**
	 * Combines 2 or more components into the same horizontal space. All components
	 * receive equal space.
	 * 
	 * @param components
	 * @return a {@link JPanel} holding the components.
	 */
	public static JPanel combine(JComponent... components) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, components.length));
		for (JComponent component : components) {
			panel.add(component);
		}
		return panel;
	}

	public static JPanel horizontalArea(JComponent... components) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		for (JComponent component : components) {
			panel.add(component);
		}

		return panel;
	}

	public static JPanel verticalArea(JComponent... components) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (JComponent component : components) {
			panel.add(component);
		}

		return panel;
	}

	public static Component verticalSpace(int size) {
		return Box.createVerticalStrut(size);
	}

	public static Component horizontalSpace(int size) {
		return Box.createHorizontalStrut(size);
	}

	public static JSeparator separator() {
		return new JSeparator();
	}

	public static JLabel label(String label) {
		return new JLabel(label);
	}

	public static JLabel label(String label, Icon icon, int alignment) {
		return new JLabel(label, icon, alignment);
	}

	public static JLabel label(IExposedField field) {
		DisplayAs dispalyAs = field.getAnnotation(DisplayAs.class);
		return new JLabel(dispalyAs != null ? dispalyAs.value() : StringUtils.capitalize(field.getFieldName()));
	}

	public static JTextField textField(String text, OnSubmitListerer<String> listener) {
		JTextField textField = new JTextField(text);
		if (listener != null) {
			textField.addActionListener((e) -> listener.onSubmit(textField.getText()));
			addFocusLostListener(textField, () -> {
				listener.onSubmit(textField.getText());
			});
		}
		return textField;
	}

	public static JScrollPane textArea(String text, int lines, OnSubmitListerer<String> listener) {
		JTextArea textArea = new JTextArea(lines, 0);
		textArea.setText(text);
		JScrollPane scroll = new JScrollPane(textArea);
		if (listener != null) {
			addFocusLostListener(textArea, () -> {
				listener.onSubmit(textArea.getText());
			});
		}
		return scroll;
	}

	public static JScrollPane textBox(String text, int lines) {
		JTextArea textArea = new JTextArea(lines, 0);
		textArea.setEnabled(false);
		textArea.setText(text);
		JScrollPane scroll = new JScrollPane(textArea);
		return scroll;
	}

	public static JButton button(String label, Icon icon, Runnable onClick) {
		JButton btn = new JButton(label, icon);
		if (onClick != null) {
			btn.addActionListener(e -> onClick.run());
		}
		return btn;
	}

	public static JButton button(Method method, Object instance) {
		String label;

		Button buttonAnnotation = method.getAnnotation(Button.class);
		if (StringUtils.isWhitespace(buttonAnnotation.value())) {
			label = method.getName();
		} else {
			label = buttonAnnotation.value();
		}

		JButton button = new JButton(label);
		button.addActionListener((e) -> {
			try {
				method.setAccessible(true);
				method.invoke(instance);
			} catch (SecurityException | IllegalAccessException exception) {
				Debug.log("Button's method can not be accessed.  Try making it public.");
				exception.printStackTrace();
			} catch (IllegalArgumentException exception) {
				Debug.logError("Button's method must have no parameters.");
				exception.printStackTrace();
			} catch (InvocationTargetException exception) {
				Debug.logError("Button's method threw %s exception", exception.getCause().toString());
				exception.printStackTrace();
			}
		});

		return button;
	}

	public static JCheckBox checkBox(boolean isOn, OnSubmitListerer<Boolean> listener) {
		JCheckBox checkBox = new JCheckBox();
		if (listener != null) {
			checkBox.addActionListener((e) -> listener.onSubmit(checkBox.isSelected()));
		}
		checkBox.setSelected(isOn);
		return checkBox;
	}

	public static JNumberField intField(int value, OnSubmitListerer<Integer> listener) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(int.class));
		addNumberFieldListeners(numberField, listener);
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField longField(long value, OnSubmitListerer<Long> listener) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(long.class));
		addNumberFieldListeners(numberField, listener);
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField floatField(float value, OnSubmitListerer<Float> listener) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(float.class));
		addNumberFieldListeners(numberField, listener);
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField doubleField(double value, OnSubmitListerer<Double> listener) {
		JNumberField numberField = new JNumberField(EnumNumberType.func(double.class));
		addNumberFieldListeners(numberField, listener);
		numberField.setValue(value);
		return numberField;
	}

	public static JNumberField numberField(IExposedField field) {
		Range annotaiton = field.getAnnotation(Range.class);
		if (annotaiton == null) {
			// TODO
		} else {
			// TODO
		}

		JNumberField numberField = new JNumberField(EnumNumberType.func(field.getType()));
		MinValue min = field.getAnnotation(MinValue.class);
		if (min != null) {
			numberField.setMin(min.value());
		}
		MaxValue max = field.getAnnotation(MaxValue.class);
		if (max != null) {
			numberField.setMax(max.value());
		}
		numberField.setEnabled(!field.isReadOnly());
		numberField.setValue(field.get());
		numberField.addActionListener(e -> {
			field.set(numberField.getValue());
		});
		numberField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						field.set(numberField.getValue());
					}
				});
			}
		});
		return numberField;
	}

	public static JPanel vector2iField(Vector2i value, OnSubmitListerer<Vector2i> listener) {
		JPanel panel = GuiBuilder.horizontalArea();

		panel.add(new JLabel("X"));
		panel.add(GuiBuilder.intField(value.x, (v) -> {
			value.setComponent(0, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Y"));
		panel.add(GuiBuilder.intField(value.y, (v) -> {
			value.setComponent(1, v);
			listener.onSubmit(value);
		}));

		return panel;
	}

	public static JPanel vector2fField(Vector2f value, OnSubmitListerer<Vector2f> listener) {
		JPanel panel = GuiBuilder.horizontalArea();

		panel.add(new JLabel("X"));
		panel.add(GuiBuilder.floatField(value.x, (v) -> {
			value.setComponent(0, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Y"));
		panel.add(GuiBuilder.floatField(value.y, (v) -> {
			value.setComponent(1, v);
			listener.onSubmit(value);
		}));

		return panel;
	}

	public static JPanel vector3iField(Vector3i value, OnSubmitListerer<Vector3i> listener) {
		JPanel panel = GuiBuilder.horizontalArea();

		panel.add(new JLabel("X"));
		panel.add(GuiBuilder.intField(value.x, (v) -> {
			value.setComponent(0, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Y"));
		panel.add(GuiBuilder.intField(value.y, (v) -> {
			value.setComponent(1, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Z"));
		panel.add(GuiBuilder.intField(value.z, (v) -> {
			value.setComponent(2, v);
			listener.onSubmit(value);
		}));

		return panel;
	}

	public static JPanel vector3fField(Vector3f value, OnSubmitListerer<Vector3f> listener) {
		JPanel panel = GuiBuilder.horizontalArea();

		panel.add(new JLabel("X"));
		panel.add(GuiBuilder.floatField(value.x, (v) -> {
			value.setComponent(0, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Y"));
		panel.add(GuiBuilder.floatField(value.y, (v) -> {
			value.setComponent(1, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Z"));
		panel.add(GuiBuilder.floatField(value.z, (v) -> {
			value.setComponent(2, v);
			listener.onSubmit(value);
		}));

		return panel;
	}

	public static JPanel vector4iField(Vector4i value, OnSubmitListerer<Vector4i> listener) {
		JPanel panel = GuiBuilder.horizontalArea();

		panel.add(new JLabel("X"));
		panel.add(GuiBuilder.intField(value.x, (v) -> {
			value.setComponent(0, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Y"));
		panel.add(GuiBuilder.intField(value.y, (v) -> {
			value.setComponent(1, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Z"));
		panel.add(GuiBuilder.intField(value.z, (v) -> {
			value.setComponent(2, v);
			listener.onSubmit(value);
		}));
		
		panel.add(new JLabel("W"));
		panel.add(GuiBuilder.intField(value.w, (v) -> {
			value.setComponent(3, v);
			listener.onSubmit(value);
		}));

		return panel;
	}

	public static JPanel vector4fField(Vector4f value, OnSubmitListerer<Vector4f> listener) {
		JPanel panel = GuiBuilder.horizontalArea();

		panel.add(new JLabel("X"));
		panel.add(GuiBuilder.floatField(value.x, (v) -> {
			value.setComponent(0, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Y"));
		panel.add(GuiBuilder.floatField(value.y, (v) -> {
			value.setComponent(1, v);
			listener.onSubmit(value);
		}));

		panel.add(new JLabel("Z"));
		panel.add(GuiBuilder.floatField(value.z, (v) -> {
			value.setComponent(2, v);
			listener.onSubmit(value);
		}));
		
		panel.add(new JLabel("W"));
		panel.add(GuiBuilder.floatField(value.w, (v) -> {
			value.setComponent(3, v);
			listener.onSubmit(value);
		}));

		return panel;
	}

	public static JPanel quaternionField(Quaternionf quaternion, OnSubmitListerer<Quaternionf> listener) {
		return null;
	}
	
	public static JButton colorField(Color color, OnSubmitListerer<Color> listener) {
		JButton btn = new JButton();
		btn.setBackground(color.toAwtColor());		
		btn.addActionListener(e -> {
			java.awt.Color newColor = JColorChooser.showDialog(btn, "Choose Color", btn.getBackground());
			if(newColor != null) {
				btn.setBackground(newColor);
				listener.onSubmit(new Color(newColor));
			}
		});

		return btn;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Asset> JComboBox<Path> assetField(T value, Class<T> clazz, OnSubmitListerer<T> listener) {
		AssetDatabase database = AssetDatabase.getInstance();

		List<Path> paths = database.getAllAssetsOfType(clazz, true);

		JComboBox<Path> comboBox = new JComboBox<Path>();
		comboBox.setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList<?> list, Object v, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, v, index, isSelected, cellHasFocus);
				
				if(v == null && index == -1) {
					if(value != null && value.isRuntimeAsset()) {
						this.setText("(Runtime Instance)");
					} else {
						this.setText("(Missing)");
					}
				} else if(v != null) {
					this.setText(FilenameUtils.removeExtension(v.toString()));;
				}
				
				return this;
			}
		});
		
		comboBox.addItem(Path.of("None"));
		for (Path path : paths) {
			comboBox.addItem(path);
		}

		if (value == null) {
			comboBox.setSelectedIndex(0); // Have None selected.
		} else {
			AssetLocation location = ((Asset)value).location;
			if (location != null && paths.contains(location.getPath())) {
				comboBox.setSelectedItem(location.getPath());
			} else {
				comboBox.setSelectedIndex(-1);
			}
		}

		comboBox.addActionListener(e -> {
			if (comboBox.getSelectedIndex() == 0) {
				listener.onSubmit(null);
			} else {
				Path path = (Path) comboBox.getSelectedItem();
				Asset asset = database.getAsset(path);
				listener.onSubmit((T) asset);
			}
		});

		return comboBox;
	}
	
	public static JComponent field(IExposedField field) {
		FieldDrawerRegistry drawerRegistry = JelloEditor.instance.filedDrawers;

		if (field.getType().isArray()) {
			JPanel arrayPanel = GuiBuilder.verticalArea();

			Object arrayInstance = field.get();
			int arrayLength = arrayInstance == null ? 0 : Array.getLength(arrayInstance);

			JNumberField lengthField = GuiBuilder.intField(arrayLength, (newLength) -> {
				// Resize the array.
				Object newArray = Array.newInstance(field.getType().componentType(), newLength);

				if (arrayInstance != null) {
					System.arraycopy(arrayInstance, 0, newArray, 0, Math.min(arrayLength, newLength));
				}

				field.set(newArray);
				JelloEditor.getWindow(InspectorWindow.class).refresh();
			});

			arrayPanel.add(GuiBuilder.combine(GuiBuilder.label(field),
					GuiBuilder.combine(GuiBuilder.label("Length"), lengthField)));

			if (arrayInstance != null) {
				FieldDrawer drawer = drawerRegistry.getDrawer(arrayInstance.getClass().getComponentType());
				if (drawer != null) {
					for (int i = 0; i < arrayLength; i++) {
						JPanel fieldPanel = drawer.draw(new ExposedArrayField(((ExposedField)field).backingField, arrayInstance, i));
						arrayPanel.add(fieldPanel);
					}
				}
			}

			return arrayPanel;
		} else {
			FieldDrawer drawer = drawerRegistry.getDrawer(field.getType());
			if (drawer != null) {
				return drawer.draw(field);
			} else {
				return null;
			}
		}
	}

	private static void addFocusLostListener(JComponent component, Runnable runnable) {
		component.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				EventQueue.invokeLater(runnable);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static <T> void addNumberFieldListeners(JNumberField numberField, OnSubmitListerer<T> onSubmit) {
		if (onSubmit != null) {
			numberField.addActionListener((e) -> onSubmit.onSubmit((T) numberField.getValue()));
			addFocusLostListener(numberField, () -> {
				onSubmit.onSubmit((T) numberField.getValue());
			});
		}
	}
}
