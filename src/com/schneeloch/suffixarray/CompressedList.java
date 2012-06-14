package com.schneeloch.suffixarray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cern.colt.list.IntArrayList;

/**
 * A list optimized for repeated items. It should be usable as if it were a regular
 * list.
 * 
 *  TODO: go through all the work in implementing List<T>
 * 
 * @author schneg
 *
 * @param <T>
 */
public class CompressedList<T> {
    private final ArrayList<T> items = new ArrayList<T>();
    /**
     * This should have the same number of elements as items.
     * Each number here is the starting index of the corresponding item in items 
     */
    private final IntArrayList indexes = new IntArrayList();

    /**
     * number of items in this list
     */
    private int count;
    
    // these two fields are optimizations for getCompressedIndex
    private int lastResult = -1;
    private int lastQuery = -1;
    
    public void add(T item, int numOfItems) {
    	indexes.add(count);
    	items.add(item);
    	count += numOfItems;
    }
    
    /**
     * Given an index, find the place in this.items or this.indexes where it belongs
     * 
     * This is a binary search, so its runtime is O(log n)
     * @param i
     * @return
     */
    private int getCompressedIndex(int i) {
    	if (lastQuery == i) {
    		return lastResult;
    	}

    	// perform binary search for 
    	int start = 0;
    	int end = items.size() - 1;
    	
    	int currentIndex = 0;
    	while (start <= end) {
    		int mid = start + (end - start)/2;
    		int stopIndex = indexes.get(mid);
    		if (stopIndex <= i) {
    			currentIndex = mid;
    			start = mid + 1;
    		}
    		else
    		{
    			end = mid - 1;
    		}
    	}
    	
    	lastQuery = i;
    	lastResult = currentIndex;
    	return currentIndex;
    }
    
    public T get(int i) {
    	int compressedIndex = getCompressedIndex(i);
    	return items.get(compressedIndex);
    }

	public int size() {
		return count;
	}

	/**
	 * Get the starting index for the run of items at index i
	 * @param i
	 * @return
	 */
	public int getStartIndex(int i) {
		int compressedIndex = getCompressedIndex(i);
		return indexes.get(compressedIndex);
	}
}
