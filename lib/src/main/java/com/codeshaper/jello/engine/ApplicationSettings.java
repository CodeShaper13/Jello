package com.codeshaper.jello.engine;

import org.joml.Vector2i;

import com.codeshaper.jello.engine.asset.Texture;

public class ApplicationSettings {

	public boolean compatibleProfile;
    
    public String windowTitle = "Jello";
    public Texture windowIcon;
    public Vector2i windowSize;
    public boolean useVSync = true;    
    public int targetFps = 30;
    public int targetUps = 30;
    public Scene startingScene;
}
