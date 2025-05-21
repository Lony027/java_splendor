package view.dto;

import java.util.Objects;

public record CardDTO(String cardSummary) {
  
  public CardDTO {
    Objects.requireNonNull(cardSummary);
  }

}
