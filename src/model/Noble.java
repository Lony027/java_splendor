package model;

import java.util.Objects;

public record Noble(int prestige, TokenCollection bonusPrice, String name) {

  public Noble {
    Objects.requireNonNull(bonusPrice);
    if (prestige < 0) {
      throw new IllegalArgumentException("prestige must be positive");
    }
  }

  @Override
  public String toString() {
    return name + " (Prestige: " + prestige + ", Price (Bonus) : " + bonusPrice.toString() + ")";
  }
}
