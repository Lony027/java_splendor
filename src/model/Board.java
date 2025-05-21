package model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import model.utils.CardReader;
import java.nio.file.Path;
import model.utils.Token;

public class Board {

  // Soutenance : Would an ArrayList be enough ?
  private final LinkedList<Card> cardsStack;
  private final HashMap<Integer, Card> onBoardCards;
  private final TokenCollection tokenBank;

  public Board(int playerCount, Path path) {
    Objects.requireNonNull(path);
    if (playerCount < 2 || playerCount > 4) {
      throw new IllegalArgumentException("Player count must be between 2 and 4 players");
    }

    try {
      var cards = CardReader.loadCards(path);
      cardsStack = cards;
      shuffleCards();
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Failed to load cards for board initialization, try another path");
    }

    onBoardCards = new HashMap<>();
    drawFourCards();
    tokenBank = createTokenBankByPlayerCount(playerCount);
  }

  public void playerTakeTwoGems(Player p, List<Token> tokens) {
    Objects.requireNonNull(p);
    Objects.requireNonNull(tokens);
    if (tokens.size() != 2) {
      throw new IllegalArgumentException("Token list must contain only two tokens");
    }

    if (!tokens.get(0).equals(tokens.get(1))) {
      throw new IllegalArgumentException("If you pick two tokens, they must be the same color");
    }

    Token tokenColor = tokens.get(0);
    if (tokenBank.tokensByColor(tokenColor) < 4) {
      throw new IllegalArgumentException("Not enough token to pick two");
    }
    tokenBank.sub(tokenColor, 2);
    p.addToken(new TokenCollection(tokens));
  }

  public void playerTakeThreeGems(Player p, List<Token> tokens) {
    Objects.requireNonNull(tokens);
    Objects.requireNonNull(p);
    if (tokens.size() != 3) {
      throw new IllegalArgumentException("Tokens list must contain only three tokens");
    }

    var distinctCount = tokens.stream().distinct().count();
    if (distinctCount != 3) {
      throw new IllegalArgumentException("If you pick three tokens, they must be differents");
    }

    var tokenCollection = new TokenCollection(tokens);
    tokenBank.subCollection(tokenCollection);
    p.addToken(tokenCollection);
  }
  
  public void buyCard(Player p, int index) {
    Objects.requireNonNull(p);
    Objects.checkIndex(index, 4);
    var card = onBoardCards.get(index);
    if (card == null) {
      throw new IllegalArgumentException("No card at position " + index);
    }
    var newPrice = p.buyCard(card);
    tokenBank.addCollection(newPrice);
    onBoardCards.remove(index);
    drawOneCard();
  }

  public void drawOneCard() {
    // NB : OptionalInt sans boxed, Optional<Integer> avec boxed
    var nullCardIndex = IntStream.range(0, 4).filter(i -> onBoardCards.get(i) == null).findFirst();
    if (nullCardIndex.isEmpty()) {
      throw new IllegalArgumentException("No null card on board"); // mauvaise exception
    }
    // Soutenance : What to do when the stack is empty ? Is it even possible ?
    var topCard = cardsStack.pop();
    onBoardCards.put(nullCardIndex.getAsInt(), topCard);
  }

  private void drawFourCards() {
    for (int i = 0; i < 4; i++) {
      drawOneCard();
    }
  }

  private void shuffleCards() {
    Collections.shuffle(cardsStack);
  }

  public String tokenBankSummary() {
    return tokenBank.toString();
  }

  public int cardsStackLeft() {
    return cardsStack.size();
  }

  public Map<Integer, Card> onBoardCards() {
    return Map.copyOf(onBoardCards);
  }

  private TokenCollection createTokenBankByPlayerCount(int playerCount) {
    return switch (playerCount) {
      case 2 -> TokenCollection.createFilledTokenCollection(4);
      case 3 -> TokenCollection.createFilledTokenCollection(5);
      case 4 -> TokenCollection.createFilledTokenCollection(7);
      default -> throw new IllegalArgumentException(
          "Not enough, or too much players. This game is supposed to be played by 2 to 4 players ");
    };
  }



}


