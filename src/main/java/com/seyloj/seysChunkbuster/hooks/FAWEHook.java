package com.seyloj.seysChunkbuster.hooks;

public class FAWEHook {

    public boolean isFAWEAvailable() {
        try {
            Class.forName("com.fastasyncworldedit.core.FaweAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
