package view.dto;

import java.util.Objects;

public record PlayerDTO(String name, int prestige, String tokenSummary, String bonusSummary) {
  
  public PlayerDTO {
    Objects.requireNonNull(name);
    Objects.requireNonNull(tokenSummary);
    if (prestige < 0) {
      throw new IllegalArgumentException("Prestige should be positive");
    }
  }
}
