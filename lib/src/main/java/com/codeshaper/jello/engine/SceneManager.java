package com.codeshaper.jello.engine;

import java.util.ArrayList;

import com.codeshaper.jello.engine.asset.Scene;

public class SceneManager implements ISceneProvider {
	
	public static final SceneManager instance = new SceneManager();
	
    private ArrayList<Scene> loadedScenes;
    
    SceneManager() {
    	loadedScenes = new ArrayList<Scene>();
    }
    
    public static void loadScene(Scene scene) {
		instance.loadedScenes.add(scene);
    }
	
	public static void loadScene(String sceneName) {
		if(isSceneLoaded(sceneName)) {
			return;
		}

		Scene scene = getSceneFromName(sceneName);
		if(scene == null) {
			return;
		}
		
		SceneManager.loadScene(scene);
	}
	
	public static void unloadScene(Scene scene) {
		instance.loadedScenes.remove(scene);
	}
	
	public static void unloadScene(String sceneName) {
		if(!isSceneLoaded(sceneName)) {
			return;
		}
		
		Scene scene = getSceneFromName(sceneName);
		if(scene != null) {
			SceneManager.unloadScene(scene);
		}
	}
	
	public static void unloadAllScenes() {
		for (int i = instance.loadedScenes.size() - 1; i >= 0; i--) {
			SceneManager.unloadScene(instance.loadedScenes.get(i));
		}
	}
	
	public static boolean isSceneLoaded(String sceneName) {
		for(Scene scene : instance.loadedScenes) {
			if(scene == null) {
				continue; // Should never happen.
			}
			
			if(scene.GetSceneName() == sceneName) {
				return true;
			}
		}
		
		return false;
	}
	
	public static Iterable<Scene> getAllScenes() {
		return instance.loadedScenes;
	}
	
	private static Scene getSceneFromName(String sceneName) {
		//System.err.println("Unable to find Scene asset with the name \"" + sceneName + "\"");

		return null; // TODO
	}

	@Override
	public Iterable<Scene> getScenes() {
		return SceneManager.getAllScenes();
	}
}
