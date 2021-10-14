package edu.yu.cs.com1320.project;

public interface TrackerHelper {
    public Comparable getLastUsedKey();

/**
 * Check if adding X amount of documents goes over the limit
 * @param docsToAdd the amount of documents to add
 * @return
 */
    public boolean isOverDocLimit(int docsToAdd);
    public boolean isOverByteLimit(Object objectToAdd);
    public void writeToDisk(Object key);
    public void readFromDisk(Object key);

    public void removeFromTracker(Object object);


}