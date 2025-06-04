package model.utils;

public enum Phase {
  ONE, TWO, THREE;
  
  public static Phase fromInt(int value) {
    switch (value) {
      case 1:
        return ONE;
      case 2:
        return TWO;
      case 3:
        return THREE;
      default:
        throw new IllegalArgumentException("Unsupported : " + value);
    }
  }
}
