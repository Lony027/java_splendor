package model;

import java.util.Objects;

public enum Token {
  GREEN, BLUE, RED, WHITE, BLACK, GOLD;

  public static Token fromString(String s) {
    Objects.requireNonNull(s);
    return switch (s) {
      case "green" -> Token.GREEN;
      case "blue" -> Token.BLUE;
      case "red" -> Token.RED;
      case "white" -> Token.WHITE;
      case "black" -> Token.BLACK;
//      case "gold" -> Token.GOLD;
      default -> throw new IllegalArgumentException("Unsupported: " + s);
    };
  }
}
