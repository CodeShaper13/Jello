package com.codeshaper.jello.engine.database;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MethodSerializer implements JsonSerializer<Method>, JsonDeserializer<Method> {

	// TODO safety checks here for illegal json
	@Override
	public Method deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObj = json.getAsJsonObject();
		JsonElement element;
		
		element = jsonObj.get("class");
		String clsName = element.getAsString();
		
		element = jsonObj.get("method");
		String methodName = element.getAsString();
				
		try {
			Class<?> cls = Class.forName(clsName);
			Method method = cls.getMethod(methodName);
			return method;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public JsonElement serialize(Method src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonObj =new JsonObject();
		jsonObj.addProperty("class", src.getDeclaringClass().getName());
		jsonObj.addProperty("method", src.getName());
		return jsonObj;
	}
}
