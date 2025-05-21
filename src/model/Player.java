package model;

import java.util.ArrayList;
import java.util.Objects;

public class Player {

  private final TokenCollection playerTokens;
  private final String name;
  private final ArrayList<Card> playerCards;

  public Player(String name) {
    Objects.requireNonNull(name);

    this.name = name;
    this.playerTokens = new TokenCollection();
    // this.playerTokens = TokenCollection.createFilledTokenCollection(9); // for test purpose
    this.playerCards = new ArrayList<Card>();
  }

  public TokenCollection buyCard(Card card) {
    Objects.requireNonNull(card);
    
    var newPrice = TokenCollection.discount(card.price(), bonus());    
    playerTokens.subCollection(newPrice);
    playerCards.add(card);
    return newPrice;
  }

  public void addToken(TokenCollection tokenToAdd) {
    Objects.requireNonNull(tokenToAdd);
    playerTokens.addCollection(tokenToAdd);
  }

  public void removeToken(TokenCollection tokenToRemove) {
    Objects.requireNonNull(tokenToRemove);
    playerTokens.subCollection(tokenToRemove);
  }

  public boolean hasExtraTokens() {
    return playerTokens.total() > 10;
  }

  public int extraTokensCount() {
    return playerTokens.total() - 10;
  }

  public String name() {
    return name;
  }

  public TokenCollection bonus() {
    var bonus = new TokenCollection();
    for (var card : playerCards) {
      bonus.add(card.bonus(), 1);
    }
    return bonus;
  }

  public int prestige() {
    return playerCards.stream().mapToInt(Card::prestige).sum();
  }

  public TokenCollection playerTokens() {
    return playerTokens;
  }

  public int playerCardsSize() {
    return playerCards.size();
  }
}
