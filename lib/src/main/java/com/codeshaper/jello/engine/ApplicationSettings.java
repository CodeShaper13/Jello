package com.codeshaper.jello.engine;

import org.joml.Vector2i;

import com.codeshaper.jello.engine.asset.Texture;

public class ApplicationSettings {

	public boolean compatibleProfile;
    
    public String windowTitle;
    public Texture windowIcon;
    public Vector2i windowSize;
    public boolean useVSync;    
    public int targetFps;
    public int targetUps;
    public Scene startingScene;
    
    public ApplicationSettings() {
    	this.windowTitle = "Jello";
    	this.windowIcon = null;
        this.windowSize = new Vector2i(1920, 1080);
        this.useVSync = true;
        this.targetFps = 30;
        this.targetUps = 20;
    }
}
