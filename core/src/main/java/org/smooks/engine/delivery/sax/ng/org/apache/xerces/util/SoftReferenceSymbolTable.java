/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.util;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.PrimeNumberSequenceGenerator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * This symbol table uses SoftReferences to its String entries, which means that table entries
 * that have no references to them can be garbage collected when memory is needed.  Thus, in
 * documents with very very large numbers of unique strings, using this SymbolTable will prevent
 * an out of memory error from occurring.
 * 
 * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable
 *
 * @author Peter McCracken, IBM
 *
 * @version $Id$
 */
/*
 * This class extends SymbolTable.  Logically, it would make more sense if it and SymbolTable
 * shared a common interface, because despite almost identical logic, SoftReferenceSymbolTable
 * shares almost no code with SymbolTable (because of necessary checks for null table entries
 * in the code).  I've chosen to avoid making the interface because we don't want to slow down
 * the vastly more common case of using the regular SymbolTable.  We also don't want to change
 * SymbolTable, since it's a public class that's probably commonly in use by many applications.
 * -PJM
 */
public class SoftReferenceSymbolTable extends SymbolTable {

    /*
     * This variable masks the fBuckets variable used by SymbolTable.
     */
    protected SREntry[] fBuckets = null;

    private final ReferenceQueue fReferenceQueue;
    
    //
    // Constructors
    //
    
    /**
     * Constructs a new, empty SymbolTable with the specified initial 
     * capacity and the specified load factor.
     *
     * @param      initialCapacity   the initial capacity of the SymbolTable.
     * @param      loadFactor        the load factor of the SymbolTable.
     * @throws     IllegalArgumentException  if the initial capacity is less
     *             than zero, or if the load factor is nonpositive.
     */
    public SoftReferenceSymbolTable(int initialCapacity, float loadFactor) {
        /*
         * The Entry buckets in the base class are not used by this class.
         * We call super() with 1 as the initial capacity to minimize the
         * memory used by the field in the base class.
         */
        super(1, loadFactor);
        
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        }
        
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }
        
        fLoadFactor = loadFactor;
        fTableSize = initialCapacity;
        fBuckets = new SREntry[fTableSize];
        fThreshold = (int)(fTableSize * loadFactor);
        fCount = 0;

        fReferenceQueue = new ReferenceQueue();
    }

    /**
     * Constructs a new, empty SymbolTable with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param     initialCapacity   the initial capacity of the hashtable.
     * @throws    IllegalArgumentException if the initial capacity is less
     *            than zero.
     */
    public SoftReferenceSymbolTable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    
    /**
     * Constructs a new, empty SymbolTable with a default initial capacity (101)
     * and load factor, which is <tt>0.75</tt>. 
     */
    public SoftReferenceSymbolTable() {
        this(TABLE_SIZE, 0.75f);
    }

    //
    // Public methods
    //

    /**
     * Adds the specified symbol to the symbol table and returns a
     * reference to the unique symbol. If the symbol already exists,
     * the previous symbol reference is returned instead, in order
     * guarantee that symbol references remain unique.
     *
     * @param symbol The new symbol.
     */
    public String addSymbol(String symbol) {
        clean();
        // search for identical symbol
        int collisionCount = 0;
        int bucket = hash(symbol) % fTableSize;
        for (SREntry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            SREntryData data = (SREntryData)entry.get();
            if (data == null) {
                continue;
            }
            if (data.symbol.equals(symbol)) {
                return data.symbol;
            }
            ++collisionCount;
        }
        return addSymbol0(symbol, bucket, collisionCount);
    } // addSymbol(String):String
    
    private String addSymbol0(String symbol, int bucket, int collisionCount) {
        if (fCount >= fThreshold) {
            // Rehash the table if the threshold is exceeded
            rehash();
            bucket = hash(symbol) % fTableSize;
        }
        else if (collisionCount >= fCollisionThreshold) {
            // Select a new hash function and rehash the table if
            // the collision threshold is exceeded.
            rebalance();
            bucket = hash(symbol) % fTableSize;
        }
        
        // add new entry
        symbol = symbol.intern();
        SREntry entry = new SREntry(symbol, fBuckets[bucket], bucket, fReferenceQueue);
        fBuckets[bucket] = entry;
        ++fCount;
        return symbol;
    } // addSymbol0(String,int,int):String

    /**
     * Adds the specified symbol to the symbol table and returns a
     * reference to the unique symbol. If the symbol already exists,
     * the previous symbol reference is returned instead, in order
     * guarantee that symbol references remain unique.
     *
     * @param buffer The buffer containing the new symbol.
     * @param offset The offset into the buffer of the new symbol.
     * @param length The length of the new symbol in the buffer.
     */
    public String addSymbol(char[] buffer, int offset, int length) {
        clean();
        // search for identical symbol
        int collisionCount = 0;
        int bucket = hash(buffer, offset, length) % fTableSize;
        OUTER: for (SREntry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            SREntryData data = (SREntryData)entry.get();
            if (data == null) {
                continue;
            }
            if (length == data.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (buffer[offset + i] != data.characters[i]) {
                        ++collisionCount;
                        continue OUTER;
                    }
                }
                return data.symbol;
            }
            ++collisionCount;
        }
        return addSymbol0(buffer, offset, length, bucket, collisionCount);
    } // addSymbol(char[],int,int):String
    
    private String addSymbol0(char[] buffer, int offset, int length, int bucket, int collisionCount) {
        if (fCount >= fThreshold) {
            // Rehash the table if the threshold is exceeded
            rehash();
            bucket = hash(buffer, offset, length) % fTableSize;
        }
        else if (collisionCount >= fCollisionThreshold) {
            // Select a new hash function and rehash the table if
            // the collision threshold is exceeded.
            rebalance();
            bucket = hash(buffer, offset, length) % fTableSize;
        }
        
        // add new entry
        String symbol = new String(buffer, offset, length).intern();
        SREntry entry = new SREntry(symbol, buffer, offset, length, fBuckets[bucket], bucket, fReferenceQueue);
        fBuckets[bucket] = entry;
        ++fCount;
        return symbol;
    } // addSymbol0(char[],int,int,int,int):String

    /**
     * Increases the capacity of and internally reorganizes this 
     * SymbolTable, in order to accommodate and access its entries more 
     * efficiently.  This method is called automatically when the 
     * number of keys in the SymbolTable exceeds this hashtable's capacity 
     * and load factor. 
     */
    protected void rehash() {
        rehashCommon(fBuckets.length * 2 + 1);
    }
    
    /**
     * Reduces the capacity of and internally reorganizes this 
     * SymbolTable, in order to accommodate and access its entries in
     * a more memory efficient way. This method is called automatically when 
     * the number of keys in the SymbolTable drops below 25% of this
     * hashtable's load factor (as a result of SoftReferences which have
     * been cleared).
     */
    protected void compact() {
        rehashCommon(((int) (fCount / fLoadFactor)) * 2 + 1);
    }
    
    /**
     * Randomly selects a new hash function and reorganizes this SymbolTable
     * in order to more evenly distribute its entries across the table. This 
     * method is called automatically when the number keys in one of the 
     * SymbolTable's buckets exceeds the given collision threshold.
     */
    protected void rebalance() {
        if (fHashMultipliers == null) {
            fHashMultipliers = new int[MULTIPLIERS_SIZE];
        }
        PrimeNumberSequenceGenerator.generateSequence(fHashMultipliers);
        rehashCommon(fBuckets.length);
    }
    
    private void rehashCommon(final int newCapacity) {
        
        final int oldCapacity = fBuckets.length;
        final SREntry[] oldTable = fBuckets;
        final SREntry[] newTable = new SREntry[newCapacity];

        fThreshold = (int)(newCapacity * fLoadFactor);
        fBuckets = newTable;
        fTableSize = fBuckets.length;

        for (int i = oldCapacity ; i-- > 0 ;) {
            for (SREntry old = oldTable[i] ; old != null ; ) {
                SREntry e = old;
                old = old.next;

                SREntryData data = (SREntryData)e.get();
                if (data != null) {
                    int index = hash(data.symbol) % newCapacity;
                    if (newTable[index] != null) {
                        newTable[index].prev = e;
                    }
                    e.bucket = index;
                    e.next = newTable[index];
                    newTable[index] = e;
                }
                else {
                    e.bucket = -1;
                    e.next = null;
                    --fCount;
                }
                e.prev = null;
            }
        }
    }

    /**
     * Returns true if the symbol table already contains the specified
     * symbol.
     *
     * @param symbol The symbol to look for.
     */
    public boolean containsSymbol(String symbol) {

        // search for identical symbol
        int bucket = hash(symbol) % fTableSize;
        int length = symbol.length();
        OUTER: for (SREntry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            SREntryData data = (SREntryData)entry.get();
            if (data == null) {
                continue;
            }
            if (length == data.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (symbol.charAt(i) != data.characters[i]) {
                        continue OUTER;
                    }
                }
                return true;
            }
        }

        return false;

    } // containsSymbol(String):boolean

    /**
     * Returns true if the symbol table already contains the specified
     * symbol.
     *
     * @param buffer The buffer containing the symbol to look for.
     * @param offset The offset into the buffer.
     * @param length The length of the symbol in the buffer.
     */
    public boolean containsSymbol(char[] buffer, int offset, int length) {

        // search for identical symbol
        int bucket = hash(buffer, offset, length) % fTableSize;
        OUTER: for (SREntry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            SREntryData data = (SREntryData)entry.get();
            if (data == null) {
                continue;
            }
            if (length == data.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (buffer[offset + i] != data.characters[i]) {
                        continue OUTER;
                    }
                }
                return true;
            }
        }

        return false;

    } // containsSymbol(char[],int,int):boolean

    private void removeEntry(SREntry entry) {
        final int bucket = entry.bucket;
        if (bucket >= 0) {
            if (entry.next != null) {
                entry.next.prev = entry.prev;
            }
            if (entry.prev != null) {
                entry.prev.next = entry.next;
            }
            else {
                fBuckets[bucket] = entry.next;
            }
            --fCount;
        }
    }
    
    /**
     * Removes stale symbols from the table.
     */
    private void clean() {
        SREntry entry = (SREntry)fReferenceQueue.poll();
        if (entry != null) {
            do {
                removeEntry(entry);
                entry = (SREntry)fReferenceQueue.poll();
            }
            while (entry != null);
            // Reduce the number of buckets if the number of items
            // in the table has dropped below 25% of the threshold.
            if (fCount < (fThreshold >> 2)) {
                compact();
            }
        }
    }
        
    //
    // Classes
    //

    /**
     * This class is a symbol table entry. Each entry acts as a node
     * in a doubly-linked list.
     * 
     * The "SR" stands for SoftReference.
     */
    protected static final class SREntry extends SoftReference {

        /** The next entry. */
        public SREntry next;

        /** The previous entry. */
        public SREntry prev;

        /** The bucket this entry is contained in; -1 if it has been removed from the table. */
        public int bucket;
        
        //
        // Constructors
        //

        /**
         * Constructs a new entry from the specified symbol and next entry
         * reference.
         */
        public SREntry(String internedSymbol, SREntry next, int bucket, ReferenceQueue q) {
            super(new SREntryData(internedSymbol), q);
            initialize(next, bucket);
        }

        /**
         * Constructs a new entry from the specified symbol information and
         * next entry reference.
         */
        public SREntry(String internedSymbol, char[] ch, int offset, int length, SREntry next, int bucket, ReferenceQueue q) {
            super(new SREntryData(internedSymbol, ch, offset, length), q);
            initialize(next, bucket);
        }
        
        private void initialize(SREntry next, int bucket) {
            this.next = next;
            if (next != null) {
                next.prev = this;
            }
            this.prev = null;
            this.bucket = bucket;
        }
    } // class Entry

    protected static final class SREntryData {
        public final String symbol;
        public final char[] characters;

        public SREntryData(String internedSymbol) {
            this.symbol = internedSymbol;
            characters = new char[symbol.length()];
            symbol.getChars(0, characters.length, characters, 0);
        }

        public SREntryData(String internedSymbol, char[] ch, int offset, int length) {
            this.symbol = internedSymbol;
            characters = new char[length];
            System.arraycopy(ch, offset, characters, 0, length);
        }
    }
} // class SoftReferenceSymbolTable
