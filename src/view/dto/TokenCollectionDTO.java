package view.dto;

import java.util.Objects;

public record TokenCollectionDTO(String tokenSummary) {

  public TokenCollectionDTO {
    Objects.requireNonNull(tokenSummary);
  }
}
