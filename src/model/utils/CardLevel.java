package model.utils;

public enum CardLevel {
  ONE, TWO, THREE;

  public static CardLevel fromInt(int value) {
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
