package com.github.ianrae.dnalparse.impl;

import org.dval.DTypeRegistry;
import org.dval.repository.MyWorld;

public class Internals {
    private DTypeRegistry registry;
    private MyWorld world;
    
    public Internals(DTypeRegistry registry, MyWorld world) {
        this.world = world;
        this.registry = registry;
    }

    public DTypeRegistry getRegistry() {
        return registry;
    }

    public MyWorld getWorld() {
        return world;
    }
    
}
