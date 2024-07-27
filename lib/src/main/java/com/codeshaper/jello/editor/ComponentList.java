package com.codeshaper.jello.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.codeshaper.jello.engine.component.JelloComponent;

public class ComponentList implements Iterable<Class<JelloComponent>> {
	
	// Separate lists, only the later has to rebuilt while the application is
	// running.
	private final List<Class<JelloComponent>> builtinComponents;
	private final List<Class<JelloComponent>> projectComponents;
	
	public ComponentList() {
		Reflections scan = new Reflections("com.codeshaper.jello.engine");

		Set<Class<? extends JelloComponent>> classes = scan.getSubTypesOf(JelloComponent.class);
		this.builtinComponents = new ArrayList<Class<JelloComponent>>();
		for(var clazz : classes) {
			@SuppressWarnings("unchecked")
			Class<JelloComponent> c = (Class<JelloComponent>)clazz;
			this.builtinComponents.add(c);
		}		
				
		this.projectComponents = new ArrayList<Class<JelloComponent>>();
	}
	
	public void compileProjectComponents() {
		// TODO not yet implemented or called.
	}
	
	public Iterator<Class<JelloComponent>> iterator() {
        return new ComponentIterator();
    }
	
	public class ComponentIterator implements Iterator<Class<JelloComponent>> {

        private int index = 0;

		public boolean hasNext() {
            return index < this.getCount();
        }

        public Class<JelloComponent> next() {
        	Class<JelloComponent> result;
        	
        	int builtinComponentCount = builtinComponents.size();
        	if(this.index < builtinComponentCount) {
        		result = builtinComponents.get(index);
        	} else {
        		result = projectComponents.get(builtinComponentCount + index);
        	}
        	
        	index++;
            return result;
        }
        
        private int getCount() {
        	return builtinComponents.size() + projectComponents.size();
        }
   }
}
