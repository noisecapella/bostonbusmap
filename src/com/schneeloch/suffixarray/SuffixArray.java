package com.schneeloch.suffixarray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import cern.colt.Sorting;
import cern.colt.function.IntComparator;

/**
 * A data structure which stores suffixes in an array and sorts them for easy lookup.
 * 
 * First the user calls add() for each object they want to index.
 * Then they call sort() to populate indexes and sort them according to their suffix
 * Then they can call search() with a keyword and receive a list of results.
 * 
 * Memory:
 * - compressedList has one ArrayList of each ObjectWithString added,
 * and one IntArrayList of each index for those ObjectWithString's
 * - indexes is populated, one index for each suffix, when sort() is called
 * 
 */
public class SuffixArray<T extends ObjectWithString> implements IntComparator {
	private final CompressedList<T> compressedList = new CompressedList<T>();
    private int[] indexes;

    public final boolean ignoreCase;
    
    public SuffixArray(boolean ignoreCase) {
    	this.ignoreCase = ignoreCase;
    }
    
    public void add(T objectWithString) {
    	int len = objectWithString.getString().length();
    	compressedList.add(objectWithString, len);
    	
    	// make sure to resort this before using it
    	indexes = null;
	}
    
    /**
     * In case we already have the result from sorting
     */
    public void setIndexes(int[] indexes) {
    	this.indexes = indexes;
    }
    
    /**
     * must be done right after adding, before anything else. If not done,
     * this may be triggered first by methods which use indexes
     */
    public void sort() {
    	if (indexes == null)
    	{
    		indexes = new int[compressedList.size()];
			for (int i = 0; i < indexes.length; i++)
			{
				indexes[i] = i;
			}
    	}
    	
    	Sorting.mergeSort(indexes, 0, indexes.length - 1, this);
    }
    
    private T getObjectWithString(int i) {
    	if (indexes == null) {
    		sort();
    	}
    	int index = indexes[i];
    	
    	return compressedList.get(index);
    }
    
    /**
     * Get suffix at index i, then truncate to strLen
     * @param i
     * @param strLen
     * @return
     */
    private String getSuffix(int i, int strLen) {
    	if (indexes == null) {
    		sort();
    	}
    	int index = indexes[i];

    	ObjectWithString objectWithString = compressedList.get(index);
    	int startIndex = compressedList.getStartIndex(index);
    	int position = index - startIndex; 

    	String string = objectWithString.getString();
    	int end = Math.min(string.length(), strLen + position);
    	return string.substring(position, end);
    }

    /**
     * Compares indexes based on the suffix at compressedList.get(index)
     */
	@Override
	public int compare(int arg0, int arg1) {
		String str0 = compressedList.get(arg0).getString();
		int p0 = arg0 - compressedList.getStartIndex(arg0);
		String sub0 = str0.substring(p0);

		String str1 = compressedList.get(arg1).getString();
		int p1 = arg1 - compressedList.getStartIndex(arg1);
		String sub1 = str1.substring(p1);

		if (ignoreCase)
		{
			return sub0.compareToIgnoreCase(sub1);
		}
		else
		{
			return sub0.compareTo(sub1);
		}
	}

	public int size() {
		return compressedList.size();
	}

    public Iterable<T> search(String search) {
    	int start = 0;
    	int end = size() - 1;
    	
    	while (start < end) {
    		int mid = start + (end - start)/2;
    		String s = getSuffix(mid, search.length());
    		int comparison;
    		if (ignoreCase)
    		{
    			comparison = s.compareToIgnoreCase(search);
    		}
    		else
    		{
    			comparison = s.compareTo(search);
    		}
    		
    		if (comparison == 0) {
    			return getResults(mid, search);
    		} else if (comparison < 0) {
    			start = mid + 1;
    		} else {
    			end = mid - 1;
    		}
    	}
    	
    	return Collections.emptyList();
    }

    /**
     * Iterate left and right from an index, returning all objects that
     * share the same suffix
     * @param mid
     * @param search
     * @return
     */
	private Iterable<T> getResults(final int mid, final String search) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					private final static int MID = 1;
					private final static int BACKWARD = 2;
					private final static int FORWARD = 3;
					private final static int DONE = 4;
					
					private int mode = MID;
					private int i = -1;
					private final int strLen = search.length();
					private final String s = getSuffix(mid, strLen);
					private T current = yield();

					@Override
					public boolean hasNext() {
						return mode != 4 && current != null;
					}

					@Override
					public T next() {
						if (current == null) {
							throw new NoSuchElementException();
						}
						else
						{
							T ret = current;
							current = yield();
							return ret;
						}
					}
					private T yield() {
						while (mode != DONE) {
							final int oldI = i;
							switch (mode) {
							case MID:
								mode++;
								i = mid - 1;
								return getObjectWithString(mid);
							case BACKWARD:
							{
								String other = getSuffix(oldI, strLen);
								i--;
								boolean equality;
								if (ignoreCase) {
									equality = other.equalsIgnoreCase(s);
								}
								else
								{
									equality = other.equals(s);
								}
								if (!equality) {
									i = mid + 1;
									mode++;
								}
								else
								{
									return getObjectWithString(oldI);
								}
							}
								break;
							case FORWARD:
							{
								i++;
								String other = getSuffix(oldI, strLen);
								boolean equality;
								if (ignoreCase) {
									equality = other.equalsIgnoreCase(s);
								}
								else
								{
									equality = other.equals(s);
								}
								if (!equality) {
									mode++;
									return null;
								}
								else
								{
									return getObjectWithString(oldI);
								}
							}
							default:
								return null;
							}
						}
						return null;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
			
		};
	}
}