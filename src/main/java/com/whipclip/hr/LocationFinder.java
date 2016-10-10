package com.whipclip.hr;

/**
 *
 * @author lance
 */
public interface LocationFinder {
    
    void addLocation(final String regex, final Location location);
    
    Location findLocation(final String message);
    
}
