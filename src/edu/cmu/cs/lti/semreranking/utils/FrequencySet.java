package edu.cmu.cs.lti.semreranking.utils;

import java.util.Map;
import java.util.Set;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Maps;

public class FrequencySet {

    private Map<String, Integer> freqMap;
    private boolean frozen;

    private final int leastAllowedFreq;
    private final static int defaultFreq = 2;
    private final static String UNK = "UNK";

    public FrequencySet(int leastAllowedFreq) {
        this.leastAllowedFreq = leastAllowedFreq;
        freqMap = Maps.newHashMap();
        frozen = false;
    }

    public FrequencySet() {
        this(defaultFreq);
    }

    public void addKeyIfNotFrozen(String key) {
        if (frozen) {
            return;
        }
        if (freqMap.containsKey(key)) {
            freqMap.put(key, freqMap.get(key) + 1);
        } else {
            freqMap.put(key, 1);
        }
    }

    /**
     * no more keys can be added any more. Smoothes out the less seen tokens and introduces UNK
     * token
     */
    public void freeze() {
        frozen = true;
        int countUnks = 0;
        Set<String> keysToBeRemoved = Sets.newHashSet();
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() <= leastAllowedFreq) {
                countUnks += entry.getValue();
                keysToBeRemoved.add(entry.getKey());
            }
        }
        for (String key : keysToBeRemoved) {
            freqMap.remove(key);
        }
        freqMap.put(UNK, countUnks);
    }

    public String returnKeyAfterFreezing(String key) {
        if (!frozen) {
            throw new IllegalArgumentException("Not frozen yet!");
        }
        if (freqMap.containsKey(key)) {
            return key;
        } else {
            return UNK;
        }
    }

    public int size() {
        if (!frozen) {
            throw new IllegalArgumentException("Not frozen yet!");
        }
        return freqMap.keySet().size();
    }

    public Set<String> keySet() {
        if (!frozen) {
            throw new IllegalArgumentException("Not frozen yet!");
        }
        return freqMap.keySet();
    }

    public boolean isFrozen() {
        return frozen;
    }
}
