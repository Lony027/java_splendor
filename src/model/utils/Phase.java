package model.utils;

public enum Phase {
  ONE, TWO, THREE;

  public static Phase fromInt(int value) {
    return switch (value) {
      case 1 -> ONE;
      case 2 -> TWO;
      case 3 -> THREE;
      default -> throw new IllegalArgumentException("Unsupported : " + value);
    };
  }
}
