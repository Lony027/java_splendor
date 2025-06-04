package model;

import java.util.Objects;
import model.utils.CardLevel;
import model.utils.Token;

// TODO: Is level really useful here ?
public record Card(int prestige, TokenCollection price, Token bonus, CardLevel level) {

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
