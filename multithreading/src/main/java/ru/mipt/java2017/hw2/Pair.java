package ru.mipt.java2017.hw2;

public class Pair<Key, Value> {

    private final Key key;
    private final Value value;

    public static <Key, Value> Pair<Key, Value> createPair(Key key, Value value) {
        return new Pair<Key, Value>(key, value);
    }

    public Pair(Key key, Value value) {
        this.key = key;
        this.value = value;
    }

    public Key getKey() {
        return key;
    }

    public Value getValue() {
        return value;
    }
}
