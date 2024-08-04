package com.codeshaper.jello.engine;

import org.joml.Vector2i;

import com.codeshaper.jello.engine.asset.Texture;

public class ApplicationSettings {

	public boolean compatibleProfile;
    
    public String windowTitle;
    public Texture windowIcon;
    public boolean fullscreen;
    public Vector2i windowSize;
    public boolean useVSync;    
    public int targetFps;
    public int targetUps;
    public Scene startingScene;
    
    public ApplicationSettings() {
    	this.windowTitle = "Jello";
    	this.windowIcon = null;
    	this.fullscreen = false;
        this.windowSize = new Vector2i(800, 800);
        this.useVSync = true;
        this.targetFps = 30;
        this.targetUps = 20;
    }
}
