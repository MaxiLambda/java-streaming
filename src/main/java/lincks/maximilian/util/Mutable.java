package lincks.maximilian.util;

public class Mutable<T> {
  private T val;

  public Mutable(T val) {
    assert val != null;
    this.val = val;
  }

  public T get() {
    return val;
  }

  public void set(T val) {
    this.val = val;
  }
}
