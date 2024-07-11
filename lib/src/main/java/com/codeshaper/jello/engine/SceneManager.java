package com.codeshaper.jello.engine;

import java.util.ArrayList;

import com.codeshaper.jello.engine.asset.Scene;

public class SceneManager implements ISceneProvider {
		
    protected ArrayList<Scene> loadedScenes;
    
    protected SceneManager() {
    	this.loadedScenes = new ArrayList<Scene>();
    }
    
    public void loadScene(Scene scene) {
		this.loadedScenes.add(scene);
    }
	
	public void unloadScene(Scene scene) {
		this.loadedScenes.remove(scene);
	}
	
	public void unloadAllScenes() {
		for (int i = this.loadedScenes.size() - 1; i >= 0; i--) {
			this.unloadScene(this.loadedScenes.get(i));
		}
	}
	
	public boolean isSceneLoaded(Scene scene) {
		String sceneName = scene.GetSceneName();
		
		for(Scene s : this.loadedScenes) {
			if(s == null) {
				continue; // Should never happen.
			}
			
			if(s.GetSceneName() == sceneName) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Iterable<Scene> getScenes() {
		return this.loadedScenes;
	}
}
