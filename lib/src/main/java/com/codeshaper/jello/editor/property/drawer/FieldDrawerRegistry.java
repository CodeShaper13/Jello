package com.codeshaper.jello.editor.property.drawer;

import java.util.HashMap;

import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.joml.Vector4i;

import com.codeshaper.jello.editor.GuiUtil;
import com.codeshaper.jello.engine.Color;
import com.codeshaper.jello.engine.asset.Asset;

public class FieldDrawerRegistry {

	private boolean areBuiltinDrawersRegistered;
	private HashMap<Class<?>, IFieldDrawer> drawers;

	public FieldDrawerRegistry() {
		this.drawers = new HashMap<Class<?>, IFieldDrawer>();
	}

	/**
	 * Registers all of the builtin field drawers. If this method has already been
	 * called, nothing happens.
	 */
	public void registerBuiltinDrawers() {
		if (areBuiltinDrawersRegistered) {
			return;
		}

		final String w = "w";
		final String x = "x";
		final String y = "y";
		final String z = "z";

		this.registerDrawer(byte.class, new NumberDrawer());
		this.registerDrawer(Byte.class, new NumberDrawer());
		this.registerDrawer(int.class, new NumberDrawer());
		this.registerDrawer(Integer.class, new NumberDrawer());
		this.registerDrawer(long.class, new NumberDrawer());
		this.registerDrawer(Long.class, new NumberDrawer());
		this.registerDrawer(short.class, new NumberDrawer());
		this.registerDrawer(Short.class, new NumberDrawer());
		this.registerDrawer(float.class, new NumberDrawer());
		this.registerDrawer(Float.class, new NumberDrawer());
		this.registerDrawer(double.class, new NumberDrawer());
		this.registerDrawer(Double.class, new NumberDrawer());
		this.registerDrawer(boolean.class, new BooleanDrawer());
		this.registerDrawer(Boolean.class, new BooleanDrawer());
		this.registerDrawer(String.class, new StringDrawer());
		this.registerDrawer(Enum.class, new EnumDrawer());
		this.registerDrawer(Vector2i.class, new VectorDrawer(x, y));
		this.registerDrawer(Vector2f.class, new VectorDrawer(x, y));
		this.registerDrawer(Vector2d.class, new VectorDrawer(x, y));
		this.registerDrawer(Vector3i.class, new VectorDrawer(x, y, z));
		this.registerDrawer(Vector3f.class, new VectorDrawer(x, y, z));
		this.registerDrawer(Vector3d.class, new VectorDrawer(x, y, z));
		this.registerDrawer(Vector4i.class, new VectorDrawer(w, x, y, z));
		this.registerDrawer(Vector4f.class, new VectorDrawer(w, x, y, z));
		this.registerDrawer(Vector4d.class, new VectorDrawer(w, x, y, z));
		this.registerDrawer(Quaternionf.class, new QuaternionfDrawer());
		this.registerDrawer(Color.class, new ColorDrawer());
		this.registerDrawer(Asset.class, new AssetDrawer());

		areBuiltinDrawersRegistered = true;
	}

	public void registerDrawer(Class<?> type, IFieldDrawer drawer) {
		if (this.drawers.containsKey(type)) {
			System.out.println("There is already a Property Drawer registered");
			return;
		}

		this.drawers.put(type, drawer);
	}

	public IFieldDrawer getDrawer(Class<?> type) {
		for (var entry : this.drawers.entrySet()) {
			if (entry.getKey().isAssignableFrom(type)) {
				return entry.getValue();
			}
		}

		return null;
	}
}
