package model;

import java.util.Objects;
import model.utils.Token;

public record Card(int prestige, TokenCollection price, Token bonus) {

  public Card {
    Objects.requireNonNull(price);
    if (prestige < 0) {
      throw new IllegalArgumentException("Prestige must be positive");
    }
  }

  @Override
  public String toString() {
    return "Prestige: " + prestige + ", Color: " + bonus.name() + ", " + price.toString();
  }

}
