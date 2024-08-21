package com.codeshaper.jello.engine.rendering;

import java.util.HashMap;

import org.joml.Vector2i;
import org.joml.Vector3i;

import com.codeshaper.jello.engine.ComponentName;
import com.codeshaper.jello.engine.JelloComponent;
import com.codeshaper.jello.engine.asset.Tile;

@ComponentName("Rendering/Tilemap Renderer")
public final class TilemapRenderer extends JelloComponent {

	public Vector2i cellSize = new Vector2i(1, 1);
	public EnumCellShape cellShape = EnumCellShape.SQUARE;
	
	private HashMap<Vector3i, Tile> tiles = new HashMap<Vector3i, Tile>();
	
	public void setTile(Tile tile, int x, int y) {
		this.setTile(tile, x, y, 0);
	}
	
	public void setTile(Tile tile, int x, int y, int z) {
		this.setTile(tile, new Vector3i(x, y, z));
	}
	
	public void setTile(Tile tile, Vector2i pos) {
		this.setTile(tile, pos.x, pos.y, 0);
	}
	
	public void setTile(Tile tile, Vector3i pos) {
		this.tiles.put(pos, tile);
		
		// TODO
	}
	
	public Tile getTile(int x, int y) {
		return this.tiles.get(new Vector3i(x, y, 0));
	}
	
	public Tile getTile(int x, int y, int z) {
		return this.tiles.get(new Vector3i(x, y, z));
	}
	
	public Tile getTile(Vector2i pos) {
		return this.tiles.get(new Vector3i(pos.x, pos.y, 0));
	}
	
	public Tile getTile(Vector3i pos) {
		return this.tiles.get(pos);
	}
	
	/**
	 * Gets the number of tiles in the map.
	 * @return the number of tiles on the map. 
	 */
	public int getTileCount() {
		return this.tiles.size();
	}
	
	public enum EnumCellShape {
		SQUARE,
		ISOMETIC,
		HEX,
	}
}
