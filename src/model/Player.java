package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player {

  private final String name;
  private final TokenCollection tokens;
  private final ArrayList<Card> buyedCards;
  private final ArrayList<Card> reservedCards;
  private final ArrayList<Noble> nobles;

  public Player(String name, boolean hasGold) {
    Objects.requireNonNull(name);

    this.name = name;
    this.tokens = TokenCollection.createEmpty(hasGold);    
    this.buyedCards = new ArrayList<Card>();
    this.reservedCards = new ArrayList<Card>();
    this.nobles = new ArrayList<Noble>();
  }

  public TokenCollection buy(Card card) {
    Objects.requireNonNull(card);
    var priceAfterBonus = TokenCollection.discount(card.price(), bonus());

    var playerGoldTokens = tokens.tokensByColor(Token.GOLD);
    var goldTokensUsed = tokens.subCollectionJoker(priceAfterBonus, playerGoldTokens);

    if (goldTokensUsed > 0) {
      tokens.sub(Token.GOLD, goldTokensUsed);
      priceAfterBonus.add(Token.GOLD, goldTokensUsed);
    }
    reservedCards.remove(card);
    buyedCards.add(card);
    return priceAfterBonus;
  }

  public int prestige() {
    var cardPrestige = buyedCards.stream().mapToInt(Card::prestige).sum();
    var noblePrestige = nobles.stream().mapToInt(Noble::prestige).sum();
    return cardPrestige + noblePrestige;
  }

  public void reserve(Card card) {
    Objects.requireNonNull(card);
    if (reservedCards.size() > 2) {
      throw new GameRuleException("You can't have more than 3 reserved cards");
    }
    reservedCards.add(card);
  }

  public TokenCollection buyReserved(int index) {
//    Objects.checkIndex(index, reservedCards.size());
    if (index < 0 || index >= reservedCards.size()) {
      throw new GameRuleException("Choose a correct index");
    }
    
    var card = reservedCards.get(index);
    var cardPrice = buy(card);
    reservedCards.remove(index);
    return cardPrice;
  }

  public void claimNoble(Noble noble) {
    Objects.requireNonNull(noble);
    if (!canClaimNoble(noble)) {
      throw new IllegalArgumentException(name + " cannot claim noble : " + noble.name());
    }
    nobles.add(noble);
  }

  public boolean canClaimNoble(Noble noble) {
    Objects.requireNonNull(noble);
    return this.bonus().canSubCollection(noble.bonusPrice());
  }

  public TokenCollection tokens() {
    return tokens;
  }

  public boolean hasExtraTokens() {
    return tokens.total() > 10;
  }

  public int extraTokensCount() {
    return tokens.total() - 10;
  }

  public String name() {
    return name;
  }

  public TokenCollection bonus() {
    var bonus = TokenCollection.createEmpty(false);
    for (var card : buyedCards) {
      bonus.add(card.bonus(), 1);
    }
    return bonus;
  }

  public List<Noble> playerNobles() {
    return List.copyOf(nobles);
  }

  public List<Card> reservedCards() {
    return List.copyOf(reservedCards);
  }

  public int playerCardsSize() {
    return buyedCards.size();
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, tokens, buyedCards, reservedCards, nobles);
  }
}
