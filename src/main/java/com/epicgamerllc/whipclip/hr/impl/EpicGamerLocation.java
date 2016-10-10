package com.epicgamerllc.whipclip.hr.impl;

import com.whipclip.hr.Location;

/**
 *
 * @author lance
 */
public class EpicGamerLocation implements Location {

    private String label;
    
    public EpicGamerLocation(final String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
    
}
