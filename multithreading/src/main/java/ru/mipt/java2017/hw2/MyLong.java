package ru.mipt.java2017.hw2;

//long wrapper
public class MyLong {

  private long value;

  public MyLong(int value) {
    this.value = value;
  }

  public void minus(int value) {
    synchronized (this) {
      this.value -= value;
    }
  }

  public void plus(int value) {
    synchronized (this) {
      this.value += value;
    }
  }

  public long getValue() {
    return value;
  }

}
