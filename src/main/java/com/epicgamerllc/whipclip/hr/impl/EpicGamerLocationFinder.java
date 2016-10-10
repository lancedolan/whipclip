package com.epicgamerllc.whipclip.hr.impl;

import com.whipclip.hr.Location;
import com.whipclip.hr.LocationFinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Note about "memory" / space complexity: 
 * My solution is polynomial space complexity, and possibly infeasible.
 * I think a proper Big O notation for this solution would be a little complex to 
 * calculate, and not as practical for actually predicting memory usage as 
 * an approach I'm about to describe.
 * More practical than a general BIG O notation for space complexity, I would 
 * point out that my solution generates a number of Strings, in absolute worst
 * case scenario, equal to (26 ^ (c/5)), where c is the number of characters provided.
 * The String size in each case is then (c/5) * 16 bits.
 * The worst case scenario for memory usage is a String of nothing but alpha ranges
 * (eg. "[a-z][a-z][a-z][a-z]" etc etc), in which each 5 characters is instruction.
 * The example above creates a half million Strings, each 4 characters long,
 * which is 8 bytes (2 bytes per character for unicode). That means my example above
 * would chew up ~4 Megabytes of heap space (8 bytes * 455,000). That also doesn't 
 * include heap space cost of String wrapper class.
 * 
 * When you step away from the math and pay attention to what that regex means,
 * it becomes clear that this probably isn't usable regex for the code cracking team,
 * and won't be realized.
 * 
 * Instead, I would work with the code cracking team to try to realize a realistic 
 * worst-case scenario String, and then decide if we should compromise on our O(1)
 * time complexity, and instead move the data into a Tree Structure, dramatically cutting
 * Space complexity, but bumping findLocation() time complexity to O(Log(n)).
 * 
 * @author lance
 */
public class EpicGamerLocationFinder implements LocationFinder {

    private int shortestLength = Integer.MAX_VALUE;
    private int longestLength;
    private final Map<String, Location> locations = new HashMap<String, Location>();
    
    public EpicGamerLocationFinder(){}

    @Override
    public void addLocation(final String regex, final Location location) {
        if (location==null) {
            //Here we have two design decicions:
                //1) It is not considered a failure to receive a null location,
                    //and so we merely skip doing anything.
                //2) It IS not considered a failure to receive a null location,
                    //and we cannot fail silently,
                    //we should fail loudly as possible to be detected early as possible
                    //in testing
            //Both solutions are below, comment/uncomment whichever you desire.   
        
//            return;
          throw new IllegalArgumentException("location must not be null");
        }
        
        //Unlike my implementation of findLocation() where performance is a concern,
        //I'm not putting focus on readability/maintainability over performance.
        
        List<StringBuilder> possibleStrings = new ArrayList<>();
        possibleStrings.add(new StringBuilder());
        
        List<Object> regexParts = parseRegexIntoParts(regex);
            
        //Prepare for a nice polynomial-time solution :p
        for (Object part : regexParts) {
            boolean isString = part instanceof String;
            if (isString) {
                for (StringBuilder possibleString : possibleStrings) {
                    possibleString.append(part);
                }
            } else {
                Set<Character> rangeChars = ((Range) part).getChars();
                final int startingSize = possibleStrings.size();
                for (int i = 0 ; i < startingSize; i++) {
                    StringBuilder possibleString = possibleStrings.get(i);
                    boolean firstCharProcessed = false;
                    final String unappended = possibleString.toString();
                    for (Character rangeChar : rangeChars) {
                        if (!firstCharProcessed) {
                            possibleString.append(rangeChar);
                            firstCharProcessed = true;
                        } else {
                            possibleStrings.add(new StringBuilder(unappended).append(rangeChar));
                        }
                    }
                }
            }
        }
        
        //Add all possibleStrings to the locations map
        for (StringBuilder possiibleString : possibleStrings) {
            Location previousLocation = this.locations.put(possiibleString.toString(), location);
            if (previousLocation!=null) {
                //Mapping already existed, there is overlap between regexs provided
                throw new RuntimeException("Overlap when processing regex " + regex 
                        + ". It produced possible string " + possiibleString
                        + " which already exists from a previous regex.");
            }
        }
        
        //Performance optimization opportunity: track sizes while iterating through above,
        //instead of calling the following method.
        //Guava has some slick syntax for doing this without mixing the separate code/concerns up
        //but while still handling them in a single for loop.
        setLongestAndShortest();
                
    }

    /**
     * Runs in "constant time," notated by "big O" conventions as O(1). 
     * There is a for loop, but it is constrained by the 
     * shortest and longest possible matching Keys in this.locations, and thus does
     * not depend on the number of characters in the message. Also we use the 
     * Java HashMap.get() to achieve O(1) when searching for the possible matches.
     * 
     * @param message String of alphanumeric message, ostensibly prefixed with encoded location information
     * @return The Location found, or null if not found.
     */
    @Override
    public Location findLocation(final String message) {
        if (this.locations.size() < 1) {
            return null;
        }
        
        int maxSearchChars = message.length()<this.longestLength?message.length():this.longestLength;
        
        //Build searhString for matching on locations hashmap.
        //Start with the portion of the message that we know is too short to match on locations map
        StringBuilder searchString = new StringBuilder(message.substring(0, maxSearchChars));
        
        Location foundLocation = null;
        for (int i = maxSearchChars - 1; i >= this.shortestLength - 1; i--) {
            searchString.setLength(i + 1);
            foundLocation = this.locations.get(searchString.toString());
            if (foundLocation!=null) {
                break;
            }
        }
        
        return foundLocation;
    }
    
    /**
     * 
     * @param regex
     * @return An ordered list of Objects which represents all possible Strings generated
     * by the regex. Each Object in the list is either a String or a set of 
     * possible characters that could take up one character in an overall String.
     * For example, a[0-2]b will create a List with 3 Objects, in the order String, Set, String.
     * The first String will contain "a", the Set will contain 0,1,2, 
     * and the second String will contain "b". 
     * 
     * NOTE: I'm very aware that a tree structure most accurately models the possibilities
     * described by regex. However, the performance requirements of findLocation() means
     * the Tree Structure is going to be flattened into a Map anyhow. Now I'm 
     * more interested in developing a structure that is intuitively processed in
     * addLocation().
     */
    private List<Object> parseRegexIntoParts(final String regex) {
        List<Object> regexParts = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean processingRange = false;
        for (char c : regex.toCharArray()) {
            //if processing literals,
                //check for beginning of range
                //put character and current StringBuilder as a Part into regexParts
            //if processing in range
                //check for end of range
                //put character and current StringBuilder as a Part into regexParts
            if (!processingRange) {
                if(c == '[' && builder.length() > 0) {
                    regexParts.add(builder.toString());
                    builder.setLength(0);
                    processingRange = true;
                }
                builder.append(c);
            } else {
                builder.append(c);
                if (c == ']') {
                    regexParts.add(new Range(builder.toString()));
                    builder.setLength(0);
                    processingRange = false;
                }
            }
        }
        
        //Add last bit, which is always literals and not a range
        regexParts.add(builder.toString());
        
        return regexParts;
    }
    
    private void setLongestAndShortest() {
        
        int length;
        for (String key : this.locations.keySet()) {
            length = key.length();
            if ( length > this.longestLength ) {
                this.longestLength = length;
            } else {
                if (length < this.shortestLength) {
                    this.shortestLength = length;
                }
            }
        }
        
    }
    
}
