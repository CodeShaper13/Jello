package com.codeshaper.jello.engine.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Path;

import com.codeshaper.jello.engine.AssetLocation;
import com.codeshaper.jello.engine.Debug;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.asset.Asset;
import com.codeshaper.jello.engine.asset.SerializedJelloObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

/**
 * Provides methods for serializing and deserializing objects to and from Json.
 * All of the methods will handle saving/restoring fields that reference
 * {@link Asset}s.
 */
public class Serializer {

	private final AssetDatabase database;
	private final AssetTypeAdapterFactory assetAdapterFactory;

	public Serializer(AssetDatabase database) {
		this.database = database;
		this.assetAdapterFactory = new AssetTypeAdapterFactory();
	}

	/**
	 * Serializes an object to a {@link JsonElement}. If {@code object} is null,
	 * {@link JsonNull#INSTANCE} is returned.
	 * 
	 * @param object the object to serialize.
	 * @return a {@link JsonElement} representing the object.
	 */
	public JsonElement serializeToJsonElement(Object object) {
		Gson gson = this.createGsonBuilder().create();
		this.setupAssetAdapter(object.getClass());

		return gson.toJsonTree(object);
	}

	/**
	 * Serializes an object to a {@link File}. To serialize
	 * {@link SerializedJelloObject}s,
	 * {@link Serializer#serializeScriptableJelloObject(SerializedJelloObject)}
	 * should be used instead. That way the
	 * {@link SerializedJelloObject#onSerialize()} callback will be invoked and the
	 * file will be prefixed with the object's type.
	 * 
	 * @param object the object to write to the file
	 * @param file   the file to write the object to
	 * @return {@code true} if there were no errors
	 * @throws IOException     if the file exists but is a directory rather than a
	 *                         regular file, does not exist but cannot be created,
	 *                         or cannot be opened for any other reason
	 * @throws JsonIOException if there was a problem writing to file
	 */
	public boolean serializeToFile(Object object, File file) throws IOException, JsonIOException {
		Gson gson = this.createGsonBuilder().create();
		this.setupAssetAdapter(object.getClass());

		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(object, writer);
			return true;
		}
	}

	/**
	 * Serializes the current state of a {@link SerializedJelloObject} to it's
	 * providing file. If the file does not exist, it will be created.
	 * 
	 * @param object the {@link SerializedJelloObject} to save
	 * @return {@code true} if there were no errors
	 * @throws IOException       If an I/O error occurred
	 * @throws SecurityException
	 */
	public boolean serializeScriptableJelloObject(SerializedJelloObject object) throws IOException {
		File file = object.location.getFullPath().toFile();
		if (!file.exists()) {
			file.createNewFile();
		}

		try (FileWriter writer = new FileWriter(file)) {
			// Write the class name as the first line.
			String fullClassName = object.getClass().getName();
			writer.write(fullClassName + "\n");

			Gson gson = this.createGsonBuilder().create();
			this.setupAssetAdapter(object.getClass());

			this.safelyInvokeOnSerialize(object);
			gson.toJson(object, writer);

			return true;
		} catch (IOException e) {
			e.printStackTrace();

			return false;
		}
	}

	/**
	 * Deserializes a Json file into an object and returns it.
	 * 
	 * @param <T>  the type of the object to create.
	 * @param file the file to read from
	 * @param cls  the class of {@code T}
	 * @return the new object, or {@code null} on error.
	 * @throws JsonIOException       if there was a problem reading from the file
	 * @throws JsonSyntaxException   if the file does not have a valid Json
	 *                               representation for {@code cls}
	 * @throws FileNotFoundException if the file does not exist, is a directory
	 *                               rather than a regular file, or for some other
	 *                               reason cannot be opened for reading.
	 * @throws IOException           if an I/O error occurs
	 */
	public <T> T deserialize(File file, Class<T> cls)
			throws JsonIOException, JsonSyntaxException, FileNotFoundException, IOException {
		Gson gson = this.createGsonBuilder().create();

		try (FileReader reader = new FileReader(file)) {
			return gson.fromJson(reader, cls);
		}
	}

	/**
	 * Deserializes a Json file into an object. If the file has references to
	 * {@link Asset}s, these references will be pointed to the respective Asset. If
	 * {@code element} is null or empty, null is returned.
	 * 
	 * @param <T>
	 * @param element
	 * @param cls
	 * @return
	 * @throws JsonSyntaxException if json is not a valid representation for an
	 *                             object of the of {@code cls}
	 */
	public <T> T deserialize(JsonElement element, Class<T> cls) throws JsonSyntaxException {
		Gson gson = this.createGsonBuilder().create();
		return gson.fromJson(element, cls);
	}

	/**
	 * Deserializes a {@link SerializedJelloObject} from a file. After deserializing
	 * the object, {@link SerializedJelloObject#onDeserialize()} is invoked. If the
	 * call throws an exception, the exception is swallowed and an error is logged.
	 * 
	 * @param location the location of the Asset to deserialize.
	 * @param cls      the type to deserialize the Json to.
	 * @return the {@link SerializedJelloObject} that was created, of {@code null}
	 *         if there was an error.
	 * @throws JsonSyntaxException if json is not a valid representation for an
	 *                             object of type cls
	 * @throws IOException         if an I/O error occurs
	 */
	public <T extends SerializedJelloObject> T deserialize(AssetLocation location, Class<T> cls)
			throws JsonSyntaxException, IOException {
		InputStream stream = location.getInputSteam();
		if (stream == null) {
			return null;
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			br.readLine(); // Skip the first line, it states the providing class and is not valid JSON.

			Gson gson = this.createGsonBuilder()
					.registerTypeAdapter(cls, new SerializedJelloObjectInstanceCreator(cls, location))
					.create();

			T newInstance = gson.fromJson(br, cls);
			if (newInstance != null) {
				this.safelyInvokeOnDeserialize(newInstance);
			}

			return newInstance;
		}
	}

	private void setupAssetAdapter(Class<?> cls) {
		if (Asset.class.isAssignableFrom(cls)) {
			this.assetAdapterFactory.wroteRoot = false;
		} else {
			this.assetAdapterFactory.wroteRoot = true;
		}
	}

	private GsonBuilder createGsonBuilder() {
		GsonBuilder builder = new GsonBuilder();

		builder.setPrettyPrinting();
		builder.serializeNulls();

		RuntimeTypeAdapterFactory<JelloComponent> componentAdapterFactory = RuntimeTypeAdapterFactory
				.of(JelloComponent.class);
		for (Class<JelloComponent> component : this.database.getallComponents()) {
			componentAdapterFactory.registerSubtype(component, component.getName());
		}
		builder.registerTypeAdapterFactory(componentAdapterFactory);

		this.assetAdapterFactory.wroteRoot = true;
		builder.registerTypeAdapterFactory(this.assetAdapterFactory);

		return builder;
	}

	private boolean safelyInvokeOnSerialize(SerializedJelloObject object) {
		try {
			object.onDeserialize();
			return true;
		} catch (Exception e) {
			Debug.log(e, this);
			return false;
		}
	}

	private boolean safelyInvokeOnDeserialize(SerializedJelloObject object) {
		try {
			object.onDeserialize();
			return true;
		} catch (Exception e) {
			Debug.log(e, this);
			return false;
		}
	}

	private class SerializedJelloObjectInstanceCreator implements InstanceCreator<Asset> {

		private final Class<? extends Asset> clazz;
		private final AssetLocation location;

		public SerializedJelloObjectInstanceCreator(Class<? extends Asset> clazz, AssetLocation location) {
			this.clazz = clazz;
			this.location = location;
		}

		@Override
		public Asset createInstance(Type type) {
			return database.invokeConstructor(this.clazz, this.location);
		}
	}

	private class AssetTypeAdapterFactory implements TypeAdapterFactory {

		public boolean wroteRoot;

		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!Asset.class.isAssignableFrom(type.getRawType())) {
				return null;
			}

			TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

			return new TypeAdapter<T>() {

				public void write(JsonWriter out, T value) throws IOException {
					if (!wroteRoot) {
						wroteRoot = true;
						delegate.write(out, value);
					} else {
						Asset asset = (Asset) value;
						if (asset == null) {
							out.nullValue();
						} else {
							if (asset.isRuntimeAsset()) {
								out.nullValue();
							} else {
								Path path = ((Asset) value).location.getPath();
								out.value(path.toString());
							}
						}
					}
				}

				@SuppressWarnings("unchecked")
				public T read(JsonReader in) throws IOException {
					JsonToken token = in.peek();
					if (token == JsonToken.STRING) {
						String relativePath = in.nextString();
						Asset asset = database.getAsset(relativePath);
						return (T) asset;
					} else if (token == JsonToken.NULL) {
						in.nextNull();
						return null;
					} else {
						return delegate.read(in);
					}
				}
			};
		}
	}
}
