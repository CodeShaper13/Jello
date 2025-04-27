package com.codeshaper.jello.editor.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;

import com.codeshaper.jello.editor.EditorUtils;
import com.codeshaper.jello.editor.JelloEditor;
import com.codeshaper.jello.editor.gui.GuiLayoutBuilder.OnSubmitListerer;
import com.codeshaper.jello.editor.property.ExposedArrayField;
import com.codeshaper.jello.editor.property.ExposedField;
import com.codeshaper.jello.editor.property.IExposedField;
import com.codeshaper.jello.editor.property.drawer.FieldDrawerRegistry;
import com.codeshaper.jello.editor.property.drawer.FieldDrawer;
import com.codeshaper.jello.editor.property.modifier.Button;
import com.codeshaper.jello.editor.property.modifier.DisplayAs;
import com.codeshaper.jello.editor.property.modifier.HideIf;
import com.codeshaper.jello.editor.property.modifier.MaxValue;
import com.codeshaper.jello.editor.property.modifier.MinValue;
import com.codeshaper.jello.editor.property.modifier.Range;
import com.codeshaper.jello.editor.property.modifier.ToolTip;
import com.codeshaper.jello.editor.scene.EditorSceneManager;
import com.codeshaper.jello.editor.swing.JNumberField;
import com.codeshaper.jello.editor.swing.JNumberField.EnumNumberType;
import com.codeshaper.jello.editor.window.InspectorWindow;
import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.GameObject;
import com.codeshaper.jello.engine.MathHelper;
import com.codeshaper.jello.engine.Scene;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.database.AssetDatabase;

/**
 * Provides a collection of static methods that create components for UIs.
 * <p>
 * This is a lower level alternative to using {@link GuiLayoutBuilder}.
 */
public final class GuiBuilder {

	private static final int SLIDER_DECIMAL_PLACES = 1000;
	private static FieldDrawerRegistry fieldDrawerRegistry;

	public static void init(JelloEditor editor) {
		fieldDrawerRegistry = new FieldDrawerRegistry(editor);
	}

	private GuiBuilder() {
	}

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
		DisplayAs displayAs = field.getAnnotation(DisplayAs.class);
		ToolTip tooltip = field.getAnnotation(ToolTip.class);

		JLabel label = new JLabel(displayAs != null ? displayAs.value() : EditorUtils.formatName(field.getFieldName()));
		if (tooltip != null) {
			label.setToolTipText(tooltip.value());
		}
		return label;
	}
	
	public static JLabel image(ImageIcon icon, int width, int height) {
		if(icon.getIconWidth() != width && icon.getIconHeight() != height) {
			icon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));			
		}
		
		JLabel label = new JLabel(icon);
		label.setSize(width, height);
		return label;
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

	/**
	 * 
	 * @param text
	 * @param lines
	 * @param listener
	 * @return
	 */
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

	/**
	 * Creates a text box that can't be edited.
	 * 
	 * @param text  the text to display.
	 * @param lines the number of lines to show. If {@code 0}, every line is shown.
	 * @return
	 */
	public static JComponent textBox(String text, int lines) {
		JTextArea textArea = new JTextArea(lines, 0);
		textArea.setEnabled(false);
		textArea.setText(text);

		if (lines == 0) {
			return new JScrollPane(
					textArea,
					JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		} else {
			return new JScrollPane(textArea);
		}
	}

	public static GuiElement button(String label, Icon icon, Runnable onClick) {
		JButton btn = new JButton(icon) {
			@Override
			public Dimension getMaximumSize() {
				if(label != null) {
					return new Dimension(Integer.MAX_VALUE, this.getPreferredSize().height);
				} else {
					return super.getMaximumSize();
				}
			}
		};
		btn.setAlignmentX(SwingConstants.LEFT);
		if (onClick != null) {
			btn.addActionListener(e -> onClick.run());
		}
		return new GuiElement(btn);
	}

	public static JButton button(Method method, Object instance) {
		String label;

		Button buttonAnnotation = method.getAnnotation(Button.class);
		if (StringUtils.isWhitespace(buttonAnnotation.value())) {
			label = EditorUtils.formatName(method.getName());
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

	public static JComponent numberField(IExposedField field) {
		EnumNumberType numberType = EnumNumberType.func(field.getType());

		Range range = field.getAnnotation(Range.class);
		if (range != null) {
			Object obj = field.get();
			if (numberType == EnumNumberType.INT) {
				return intSlider((int) range.min(), (int) range.max(), obj == null ? 0 : (int) obj, (v) -> {
					field.set(v);
				});
			} else if (numberType == EnumNumberType.FLOAT) {
				return floatSlider((float) range.min(), (float) range.max(), obj == null ? 0 : (float) obj, (v) -> {
					field.set(v);
				});
			} else if (numberType == EnumNumberType.DOUBLE) {
				return doubleSlider(range.min(), range.max(), obj == null ? 0 : (double) obj, (v) -> {
					field.set(v);
				});
			} else {
				Debug.logWarning(
						"Range annotation can only be on fields of type int, float, double and their wrapper classes.");
				return null;
			}
		} else {
			JNumberField numberField = new JNumberField(numberType);

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
	}

	public static JPanel intSlider(int min, int max, int value, OnSubmitListerer<Integer> listener) {
		JSlider slider = new JSlider(min, max, value);
		JNumberField field = intField(value, (v) -> {
			listener.onSubmit(v);
			slider.setValue(v);
		});

		if (listener != null) {
			slider.addChangeListener((e) -> {
				int i = slider.getValue();
				listener.onSubmit(i);
				field.setValue(i);
			});
			addFocusLostListener(slider, () -> {
				int i = slider.getValue();
				listener.onSubmit(i);
				field.setValue(i);
			});
		}

		return horizontalArea(slider, field);
	}

	public static JPanel floatSlider(float min, float max, float value, OnSubmitListerer<Float> listener) {
		return doubleSlider(min, max, value, (v) -> {
			listener.onSubmit(v.floatValue());
		});
	}

	public static JPanel doubleSlider(double min, double max, double value, OnSubmitListerer<Double> listener) {
		JSlider slider = new JSlider(
				(int) Math.round(min * SLIDER_DECIMAL_PLACES),
				(int) Math.round(max * SLIDER_DECIMAL_PLACES),
				(int) Math.round(value * SLIDER_DECIMAL_PLACES));
		JNumberField field = doubleField(value, (v) -> {
			listener.onSubmit(v);
			slider.setValue((int) Math.round(v * SLIDER_DECIMAL_PLACES));
		});

		if (listener != null) {
			slider.addChangeListener((e) -> {
				double d = (double) slider.getValue() / SLIDER_DECIMAL_PLACES;
				listener.onSubmit(d);
				field.setValue(d);
			});
			addFocusLostListener(slider, () -> {
				double d = (double) slider.getValue() / SLIDER_DECIMAL_PLACES;
				listener.onSubmit(d);
				field.setValue(d);
			});
		}

		return horizontalArea(slider, field);
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
		JPanel panel = GuiBuilder.horizontalArea();

		Vector3f eu = MathHelper.quaternionToEulerAnglesDegrees(quaternion);

		JNumberField xField = GuiBuilder.floatField(eu.x, null);
		JNumberField yField = GuiBuilder.floatField(eu.y, null);
		JNumberField zField = GuiBuilder.floatField(eu.z, null);

		OnSubmitListerer<Float> l = (v) -> {
			Vector3f eulerDegrees = new Vector3f(
					(float) xField.getValue(),
					(float) yField.getValue(),
					(float) zField.getValue());
			listener.onSubmit(MathHelper.quaternionFromEulerAnglesDegrees(eulerDegrees));
		};

		addNumberFieldListeners(xField, l);
		addNumberFieldListeners(yField, l);
		addNumberFieldListeners(zField, l);

		panel.add(new JLabel("X"));
		panel.add(xField);
		panel.add(new JLabel("Y"));
		panel.add(yField);
		panel.add(new JLabel("Z"));
		panel.add(zField);

		return panel;
	}

	public static JButton colorField(Color color, OnSubmitListerer<Color> listener) {
		JButton btn = new JButton();
		btn.setBackground(color != null ? color.toAwtColor() : java.awt.Color.black);
		btn.addActionListener(e -> {
			java.awt.Color newColor = JColorChooser.showDialog(btn, "Choose Color", btn.getBackground());
			if (newColor != null) {
				btn.setBackground(newColor);
				listener.onSubmit(new Color(newColor));
			}
		});

		return btn;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Asset> JComboBox<Path> assetField(T value, Class<T> clazz, OnSubmitListerer<T> listener) {
		AssetDatabase database = AssetDatabase.getInstance();

		List<AssetLocation> locations = database.getAllAssets(clazz, true);

		JComboBox<Path> comboBox = new JComboBox<Path>();
		comboBox.setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList<?> list, Object v, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, v, index, isSelected, cellHasFocus);

				if (v == null && index == -1) {
					if (value != null && value.isRuntimeAsset()) {
						this.setText("(Runtime Instance)");
					} else {
						this.setText("(Missing)");
					}
				} else if (v != null) {
					this.setText(FilenameUtils.removeExtension(v.toString()));;
				}

				return this;
			}
		});

		comboBox.addItem(Path.of("None"));
		for (AssetLocation location : locations) {
			comboBox.addItem(location.getRelativePath());
		}

		if (value == null) {
			comboBox.setSelectedIndex(0); // Have None selected.
		} else {
			AssetLocation location = ((Asset) value).location;
			if (location != null && locations.contains(location)) {
				comboBox.setSelectedItem(location.getRelativePath());
			} else {
				comboBox.setSelectedIndex(-1);
			}
		}

		if (listener != null) {
			comboBox.addActionListener(e -> {
				if (comboBox.getSelectedIndex() == 0) {
					listener.onSubmit(null);
				} else {
					Path path = (Path) comboBox.getSelectedItem();
					Asset asset = database.getAsset(new AssetLocation(path));
					listener.onSubmit((T) asset);
				}
			});
		}

		comboBox.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					InspectorWindow inspector = JelloEditor.getWindow(InspectorWindow.class);
					// Dont show builtin or runtime assets.
					if (value != null && value.location != null && !value.location.isBuiltin()) {
						inspector.setTarget(value);
					}
				}
			}
		});

		return comboBox;
	}

	private static void func(JComboBox<GameObject> list, GameObject obj) {
		list.addItem(obj);
		for (GameObject child : obj.getChildren()) {
			func(list, child);
		}
	}

	public static JComboBox<GameObject> gameObjectField(GameObject value, OnSubmitListerer<GameObject> listener) {
		JComboBox<GameObject> comboBox = new JComboBox<GameObject>();
		comboBox.setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList<?> list, Object v, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, v, index, isSelected, cellHasFocus);

				if (v != null) {
					GameObject obj = (GameObject) v;
					this.setText(String.format("(%s) %s", obj.getScene().getAssetName(), obj.getPath(false)));
				} else {
					this.setText("None");
				}

				return this;
			}
		});

		comboBox.addItem(null);
		EditorSceneManager sceneManager = JelloEditor.instance.sceneManager;
		for (int i = 0; i < sceneManager.getSceneCount(); i++) {
			Scene scene = sceneManager.getScene(i);
			for (GameObject obj : scene.getRootGameObjects()) {
				func(comboBox, obj);
			}
		}

		comboBox.setSelectedItem(value != null ? value : 0);

		if (listener != null) {
			comboBox.addActionListener(e -> {
				if (comboBox.getSelectedIndex() == 0) {
					listener.onSubmit(null);
				} else {
					listener.onSubmit((GameObject) comboBox.getSelectedItem());
				}
			});
		}

		return comboBox;
	}

	public static JComponent field(IExposedField field) {
		if (fieldDrawerRegistry == null) {
			System.out.println("GuiBuilder#init() must be called before using GuiBuilder#field()");
			return GuiBuilder.label("ERROR"); // PRevent possible Null Pointer Exception.
		}

		HideIf hideIf = field.getAnnotation(HideIf.class);
		if (hideIf != null) {
			if (EditorUtils.evaluteLine((ExposedField) field, hideIf.value())) {
				return null;
			}
		}

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
				FieldDrawer drawer = fieldDrawerRegistry.getDrawer(arrayInstance.getClass().getComponentType());
				if (drawer != null) {
					for (int i = 0; i < arrayLength; i++) {
						JPanel fieldPanel = drawer
								.draw(new ExposedArrayField(((ExposedField) field).backingField, arrayInstance, i));
						arrayPanel.add(fieldPanel);
					}
				}
			}

			return arrayPanel;
		} else {
			FieldDrawer drawer = fieldDrawerRegistry.getDrawer(field.getType());
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
