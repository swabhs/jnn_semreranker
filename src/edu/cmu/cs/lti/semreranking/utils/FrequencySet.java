package edu.cmu.cs.lti.semreranking.utils;

import java.util.Map;
import java.util.Set;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Maps;

public class FrequencySet {

    private Map<String, Integer> freqMap;
    private boolean frozen;

    private static final int LEAST_FREQ = 2;
    private static final String UNK = "UNK";

    public FrequencySet() {
        freqMap = Maps.newHashMap();
        frozen = false;
    }

    public void add(String key) {
        if (frozen) {
            System.err.println("Set frozen,cannot add");
            return;
        }
        if (freqMap.containsKey(key)) {
            freqMap.put(key, freqMap.get(key) + 1);
        } else {
            freqMap.put(key, 1);
        }
    }

    public void freeze() {
        frozen = true;
        int countUnks = 0;
        Set<String> keysToBeRemoved = Sets.newHashSet();
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() <= LEAST_FREQ) {
                countUnks += entry.getValue();
                keysToBeRemoved.add(entry.getKey());
            }
        }
        for (String key : keysToBeRemoved) {
            freqMap.remove(key);
        }
        freqMap.put(UNK, countUnks);
    }

    private boolean contains(String key) {
        if (frozen == false) {
            throw new IllegalArgumentException("Set not frozen yet");
        }
        return freqMap.containsKey(key);
    }

    public String returnKeyIfPresent(String key) {
        if (contains(key)) {
            return key;
        } else {
            return UNK;
        }
    }

    public int size() {
        return freqMap.keySet().size();
    }

    public Set<String> keySet() {
        return freqMap.keySet();
    }

}
