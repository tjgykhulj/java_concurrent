package org.tjg.concurrent;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Arnold Tang
 * @see java.util.Map
 */
public class HashMap<K,V> implements Map<K,V> {

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_INITIAL_CAPACITY = 1<<4;
    private static final int MAX_CAPACITY = 1<<30;

    private class Node<K,V> implements Map.Entry<K,V> {
        private K key;
        private V value;
        private Node<K,V> next;

        Node(K key, V value, Node<K,V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return this.key;
        }
        @Override
        public V getValue() {
            return this.value;
        }
        @Override
        public V setValue(V value) {
            V oldVal = this.value;
            this.value = value;
            return oldVal;
        }
    }

    private int size;
    private final int initialCapacity;
    private final float loadFactor;
    private Node<K,V>[] table;

    public HashMap(int initialCapacity, float loadFactor) {
        this.size = 0;
        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
    }

    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if (size == 0) {
            return false;
        }
        for (Node<K, V> node : table) {
            for (Node<K, V> i = node; i != null; i = i.next) {
                if (Objects.equals(value, i.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if (size == 0) {
            return null;
        }
        int idx = hash(key) & (table.length - 1);
        for (Node<K, V> i = table[idx]; i != null; i = i.next) {
            if (Objects.equals(key, i.key)) {
                return i.value;
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        expandTableIfNeed();
        int idx = hash(key) & (table.length - 1);
        for (Node<K, V> i = table[idx]; i != null; i = i.next) {
            if (Objects.equals(key, i.key)) {
                V oldVal = i.value;
                i.value = value;
                return oldVal;
            }
        }
        table[idx] = new Node<>(key, value, table[idx]);
        size++;
        return null;
    }

    @Override
    public V remove(Object key) {
        int idx = hash(key) & (table.length - 1);
        for (Node<K, V> i = table[idx], prev = null; i != null; prev=i, i = i.next) {
            if (Objects.equals(key, i.key)) {
                if (prev == null) {
                    table[idx] = table[idx].next;
                } else {
                    prev.next = i.next;
                }
                size--;
                return i.value;
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.table = null;
        this.size = 0;
    }

    @Override
    public Set<K> keySet() {
        // TODO need to create a hashtable iterator & keySet class
        return null;
    }

    @Override
    public Collection<V> values() {
        // TODO
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // TODO
        return null;
    }

    private void expandTableIfNeed() {
        if (table == null) {
            expandTable(initialCapacity);
        } else if (table.length * loadFactor > size) {
            expandTable(table.length << 1);
        }
    }

    private void expandTable(int newCap) {
        if (newCap > MAX_CAPACITY) {
            return;
        }
        Node<K,V>[] old = table;

        table = new Node[newCap];
        /*
         * TODO can be designed as zero-memory-alloc version
         * @see java.util.HashMap#resize()
          */
        for (Node<K,V> node: old) {
            for (Node<K,V> i = node; i != null; i = i.next) {
                int index = hash(i.key) & (newCap - 1);
                table[index] = new Node<>(i.key, i.value, table[index]);
            }
        }

    }

    private int hash(Object key) {
        return Objects.hashCode(key);
    }
}
