package model.utils;

public enum CardLevel {
  ONE, TWO, THREE;

  public static CardLevel fromInt(int value) {
    return switch (value) {
      case 1 -> ONE;
      case 2 -> TWO;
      case 3 -> THREE;
      default -> throw new IllegalArgumentException("Unsupported : " + value);
    };
  }
  
  public static int toInt(CardLevel cardLevel) {
    return switch (cardLevel) {
      case ONE -> 1;
      case TWO -> 2;
      case THREE-> 3;
    };
  }
}
