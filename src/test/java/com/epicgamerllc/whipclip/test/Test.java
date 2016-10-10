package com.epicgamerllc.whipclip.test;

import com.epicgamerllc.whipclip.hr.impl.EpicGamerLocation;
import com.epicgamerllc.whipclip.hr.impl.EpicGamerLocationFinder;
import com.whipclip.hr.LocationFinder;
import com.whipclip.hr.Location;
import junit.framework.TestCase;

/**
 *
 * @author lance
 */
public class Test extends TestCase {
    
    public  void testBasic() {
        LocationFinder finder = new EpicGamerLocationFinder();
        
        Location l1 = new EpicGamerLocation("L 1");
        Location l2 = new EpicGamerLocation("L 2");
        Location l3 = new EpicGamerLocation("L 3");
        
        finder.addLocation("abc[d-f]g" , l1);
        finder.addLocation("abc[e-f]gx" , l2);
        finder.addLocation("a[b-d]c[l-p]g" , l3);
        
        assert l1.equals(finder.findLocation("abceg")) ;
        assert l2.equals(finder.findLocation("abcegx")) ;
        assert l2.equals(finder.findLocation("abcegxqfdwef")) ;
        assert l3.equals(finder.findLocation("abcmg")) ;
        assert finder.findLocation("accfgx") == null ;
    }
    
    public static void testConflict() {
        LocationFinder finder = new EpicGamerLocationFinder();
        
        Location l1 = new EpicGamerLocation("L 1");
        Location l2 = new EpicGamerLocation("L 2");
        Location l3 = new EpicGamerLocation("L 3");
        Location l4 = new EpicGamerLocation("L 4");
        
        finder.addLocation("abc[d-f]g" , l1);
        finder.addLocation("abc[e-f]gx" , l2);
        finder.addLocation("a[b-d]c[l-p]g" , l3);
        try {
            finder.addLocation("abc[f-m]g[a-z]" , l4);
            assert false;//we should never get here.
        } catch (RuntimeException e) {
            //successfully handled
        }
    }
    
}
