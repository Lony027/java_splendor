package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.utils.CardLevel;
import model.utils.CardReader;
import model.utils.Phase;
import model.utils.Token;
import java.nio.file.Path;

public class Board {

  // TODO: if no more cards end game

  private final HashMap<CardLevel, LinkedList<Card>> cardDecks;
  private final HashMap<CardLevel, HashMap<Integer, Card>> cards;
  private final TokenCollection bank;
  private final ArrayList<Noble> nobles;
  // TODO: Maybe a Set<> instead
  private final List<Player> playerList;


  public Board(HashMap<CardLevel, LinkedList<Card>> cardDecks,
      HashMap<CardLevel, HashMap<Integer, Card>> cards, TokenCollection bank,
      ArrayList<Noble> nobles, List<Player> playerList) {
    Objects.requireNonNull(cardDecks);
    Objects.requireNonNull(cards);
    Objects.requireNonNull(bank);
    Objects.requireNonNull(nobles);
    Objects.requireNonNull(playerList);
    if (playerList.size() < 2 || playerList.size() > 4) {
      throw new IllegalArgumentException("Player list must contain from 2 to 4 players");
    }
    this.cardDecks = cardDecks;
    this.cards = cards;
    this.bank = bank;
    this.nobles = nobles;
    this.playerList = playerList;
    // TODO: Do this in the factory ??
    for (var cardLevel : cardDecks.keySet()) {
      drawFourCards(cardLevel);
    }
  }

  private static List<Noble> shuffledGoodNumberNobles(List<Noble> nobles, int playerCount) {
    Collections.shuffle(nobles);
    return nobles.subList(0, playerCount + 1);
  }

  public static Board factory(int playerCount, Path cardPath, Path noblePath, Phase phase,
      List<Player> playersList) {
    Objects.requireNonNull(cardPath);
    Objects.requireNonNull(noblePath);
    if (playerCount < 2 || playerCount > 4) {
      throw new IllegalArgumentException("Player count must be between 2 and 4 players");
    }

    try {
      var cardStacks = CardReader.loadCards(cardPath);
      shuffle(cardStacks);

      var onBoardNobles = new ArrayList<Noble>();
      if (phase != Phase.ONE) {
        var allNobles = CardReader.loadNobles(noblePath);
        onBoardNobles.addAll(shuffledGoodNumberNobles(allNobles, playerCount));
      }

      var onBoardCards = new HashMap<CardLevel, HashMap<Integer, Card>>();
      for (var availableLevel : cardStacks.keySet()) {
        onBoardCards.put(availableLevel, new HashMap<Integer, Card>());
      }

      var tokenBank = createTokenBank(playerCount);

      return new Board(cardStacks, onBoardCards, tokenBank, onBoardNobles, playersList);

    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Failed to load cards for board initialization, try another path");
    }
  }

  public void playerReserveCard(Player p, Map.Entry<CardLevel, Integer> entry) {
    Objects.requireNonNull(p);
    Objects.requireNonNull(entry);

    var index = entry.getValue();
    var cardLevel = entry.getKey();

    if (index > 3 || index < -1) {
      throw new IllegalArgumentException();
    }

    Card card;
    if (index >= 0) {
      card = getCard(cards, index, cardLevel);
    } else {
      if (cardDecks.get(cardLevel).size() == 0) {
        throw new GameRuleException("Stack is empty");
      }
      card = cardDecks.get(cardLevel).pop();
    }

    p.reserve(card);
    var bankHasGold = bank.tokensByColor(Token.GOLD) > 0;
    if (bankHasGold) {
      bank.sub(Token.GOLD, 1);
      p.tokens().add(Token.GOLD, 1);
    }
    if (index >= 0) {
      removeCard(cards, index, cardLevel);
      drawOneCard(cardLevel);
    }

  }

  public List<Noble> noblesPlayerCanClaim(Player player) {
    Objects.requireNonNull(player);
    return nobles.stream().filter(n -> player.canClaimNoble(n)).toList();
  }

  // TODO: Do we need to check if there's any gold token in the list ?
  public void playerTakeTwoTokens(Player p, List<Token> tokens) {
    Objects.requireNonNull(p);
    Objects.requireNonNull(tokens);
    if (tokens.size() != 2) {
      throw new IllegalArgumentException("Token list must contain only two tokens");
    }

    if (!tokens.get(0).equals(tokens.get(1))) {
      throw new IllegalArgumentException("If you pick two tokens, they must be the same color");
    }

    Token tokenColor = tokens.get(0);
    if (bank.tokensByColor(tokenColor) < 4) {
      throw new IllegalArgumentException("Not enough token to pick two");
    }
    bank.sub(tokenColor, 2);
    p.tokens().addCollection(TokenCollection.fromList(tokens));
  }

  public void playerTakeThreeTokens(Player p, List<Token> tokens) {
    Objects.requireNonNull(tokens);
    Objects.requireNonNull(p);
    if (tokens.size() != 3) {
      throw new IllegalArgumentException("Tokens list must contain only three tokens");
    }

    var distinctCount = tokens.stream().distinct().count();
    if (distinctCount != 3) {
      throw new IllegalArgumentException("If you pick three tokens, they must be differents");
    }

    var tokenCollection = TokenCollection.fromList(tokens);
    bank.subCollection(tokenCollection);
    p.tokens().addCollection(tokenCollection);
  }

  public void playerGivesBackToken(Player player, TokenCollection tokens) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(tokens);

    // TODO: Is this check overkill ?
    // if (player.tokens().total() + bank.total() > createTokenBank(playerList.size()).total()) {
    // throw new IllegalArgumentException("Player gave back too much tokens");
    // }

    player.tokens().subCollection(tokens);
    bank.addCollection(tokens);
  }

  public void buyCard(Player player, Map.Entry<CardLevel, Integer> entry) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(entry);

    var index = entry.getValue();
    var cardLevel = entry.getKey();
    Objects.requireNonNull(index);
    Objects.requireNonNull(cardLevel);

    var card = getCard(cards, index, cardLevel);
    var priceAfterDiscount = player.buy(card);
    bank.addCollection(priceAfterDiscount);
    removeCard(cards, index, cardLevel);
    drawOneCard(cardLevel);
  }

  public void buyReserved(Player player, int index) {
    Objects.requireNonNull(player);
    Objects.checkIndex(index, 3);

    var priceAfterDiscount = player.buyReserved(index);
    bank.addCollection(priceAfterDiscount);
  }
  
  public void playerClaimNoble(Player player, Noble noble) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(noble);
    
    if (!nobles.contains(noble)) {
      throw new IllegalArgumentException("Noble not in board");
    }
    
    player.claimNoble(noble);
    nobles.remove(noble);
  }

  public TokenCollection tokenBank() {
    return bank;
  }

  public Map<CardLevel, Integer> cardDecksSizes() {
    return cardDecks.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
  }

  public Map<CardLevel, Map<Integer, Card>> cards() {
    return Map.copyOf(cards.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> Map.copyOf(entry.getValue()))));
  }

  public List<Noble> nobles() {
    return List.copyOf(nobles);
  }

  public List<Player> playerList() {
    return List.copyOf(playerList);
  }

  public Optional<Player> winner() {
    var winners = playerList.stream().filter(p -> p.prestige() >= 15).toList();
    if (winners.isEmpty()) {
      return Optional.empty();
    }
    var maxPrestige = winners.stream().mapToInt(Player::prestige).max();
    winners = playerList.stream().filter(p -> p.prestige() == maxPrestige.getAsInt()).toList();
    if (winners.size() == 1) {
      return Optional.of(winners.get(0));
    } else {
      // TODO: What happens when there's a tie ?
      return winners.stream().min(Comparator.comparingInt(Player::playerCardsSize));
    }

    // TODO:
    // return playersList.stream().filter(p -> p.prestige() >= 15)
    // .collect(Collectors.groupingBy(Player::prestige, Collectors.toList())).entrySet().stream()
    // .max(Map.Entry.comparingByKey()).map(Map.Entry::getValue)
    // .flatMap(winners -> winners.size() == 1 ? Optional.of(winners.get(0))
    // : winners.stream().min(Comparator.comparingInt(Player::playerCardsSize)));
  }

  private void drawOneCard(CardLevel cardLevel) {
    var nullCardIndex =
        IntStream.range(0, 4).filter(i -> cards.get(cardLevel).get(i) == null).findFirst();
    if (nullCardIndex.isEmpty()) {
      throw new IllegalArgumentException("Cannot draw a card, the level is already filled");
    }
    // TODO: Manage when empty !
    var topCard = cardDecks.get(cardLevel).pop();
    cards.get(cardLevel).put(nullCardIndex.getAsInt(), topCard);
  }

  private void drawFourCards(CardLevel cardLevel) {
    for (int i = 0; i < 4; i++) {
      drawOneCard(cardLevel);
    }
  }

  private static void shuffle(HashMap<CardLevel, LinkedList<Card>> cardDecks) {
    for (var deck : cardDecks.values()) {
      Collections.shuffle(deck);
    }
  }

  private static Card removeCard(HashMap<CardLevel, HashMap<Integer, Card>> onBoardCards, int index,
      CardLevel cardLevel) {
    Objects.checkIndex(index, 4);
    return onBoardCards.get(cardLevel).remove(index);
  }

  private static Card getCard(HashMap<CardLevel, HashMap<Integer, Card>> onBoardCards, int index,
      CardLevel cardLevel) {
    Objects.checkIndex(index, 4);
    var cardStack = onBoardCards.get(cardLevel);
    if (cardStack == null) {
      throw new IllegalArgumentException("No card at level : " + cardLevel.name());
    }
    var card = cardStack.get(index);
    if (card == null) {
      throw new IllegalArgumentException(
          "No card at level : " + cardLevel.name() + " and position " + index);
    }
    return card;
  }

  private static TokenCollection createTokenBank(int playerCount) {
    return switch (playerCount) {
      case 2 -> TokenCollection.createFilled(4);
      case 3 -> TokenCollection.createFilled(5);
      case 4 -> TokenCollection.createFilled(7);
      default -> throw new IllegalArgumentException(
          "Not enough, or too much players. This game is supposed to be played by 2 to 4 players ");
    };
  }



}


