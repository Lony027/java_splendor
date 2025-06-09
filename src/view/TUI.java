package view;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import controller.Action;
import model.Board;
import model.Card;
import model.CardLevel;
import model.Noble;
import model.Phase;
import model.Player;
import model.Token;
import model.TokenCollection;

public record TUI(Scanner scanner, Phase phase) implements View {

  public TUI {
    Objects.requireNonNull(scanner);
  }

  public TUI(Phase phase) {
    this(new Scanner(System.in), phase);
  }

  /*
   * INTERFACE METHODS
   */

  @Override
  public Action promptPlayerAction() {
    var action = readLine().toLowerCase();
    return switch (action) {
      case "buy" -> Action.BUY;
      case "take" -> Action.TAKE;
      case "res" -> Action.RESERVE;
      case "buy_res" -> Action.BUY_RESERVED;
      default -> throw new NumberFormatException();
    };
  }

  @Override
  public Map.Entry<CardLevel, Integer> promptCardLevelIndex() {
    var level = promptCardLevel();
    var index = promptCardIndex();
    return new AbstractMap.SimpleEntry<CardLevel, Integer>(level, index);
  }

  @Override
  public Entry<CardLevel, Integer> promptCardLevelIndexReserve() {
    var level = promptCardLevel();
    var index = promptForValidInt(
        () -> System.out.println("Choose the card position (0-2), (-1) for card stack"),
        i -> i >= -1 || i <= 2);
    return new AbstractMap.SimpleEntry<CardLevel, Integer>(level, index);
  }

  @Override
  public int promptReservedCardIndex() {
    return promptForValidInt(() -> System.out.println("Choose the card position (0-2)"),
        i -> i >= 0 || i <= 2);
  }

  @Override
  public List<Token> promptTakeTokens() {
    System.out.println("Choose two tokens of the same color or three different");
    var stringTokenList = readWords();
    return stringTokenList.stream().map(Token::fromString).toList();
  }

  @Override
  public List<String> promptPlayerNames() {
    var playerCount = promptForValidInt(TUI::printAskHowMuchPlayer, i -> i >= 2 && i <= 4);

    var players = new ArrayList<String>();
    for (int i = 1; i <= playerCount; i++) {
      String name;
      do {
        TUI.printAskPlayerName(i);
        name = readLine();
      } while (name.isBlank());
      players.add(name);
    }
    return players;
  }

  @Override
  public List<Token> promptGiveBackExtraTokens(Player p, int extra) {
    Objects.requireNonNull(p);
    if (extra < 0) {
      throw new IllegalArgumentException("extra must be positive");
    }

    System.out.println("You have " + extra
        + " extra tokens, type the one you want to remove.\n Example: (green blue blue)");
    var userTypedTokens = readWords();
    return userTypedTokens.stream().map(Token::fromString).toList();
  }

  @Override
  public Noble promptPlayerToChooseNoble(List<Noble> nobles) {
    Objects.requireNonNull(nobles);
    var prompt =
        IntStream.range(0, nobles.size()).mapToObj(i -> i + ": " + nobles.get(i)).collect(Collectors
            .joining("\n", "Choose between theses nobles the one you want (type number) :\n", ""));
    var nobleIndex =
        promptForValidInt(() -> System.out.println(prompt), i -> i >= 0 || i < nobles.size());
    return nobles.get(nobleIndex);
  }

  @Override
  public void printWinner(String playerName) {
    Objects.requireNonNull(playerName);
    System.out.println(playerName + " has won!");
  }

  @Override
  public void printException(Exception e) {
    Objects.requireNonNull(e);
    System.out.println(e.getMessage());
  }

  @Override
  public void printTurn(Player activePlayer, Board board) {
    Objects.requireNonNull(activePlayer);
    Objects.requireNonNull(board);

    System.out.println(cardsLeft(board.cardDecksSizes()));
    System.out.println(onBoardCards(board.cards()));
    System.out.println(tokenBank(board.tokenBank()));
    if (phase == Phase.COMPLETE) {
      System.out.println("On Board Nobles :\n" + nobleList(board.nobles()));
    }
    System.out.println(separator());
    System.out.println(playerInfos(board.playerList(), activePlayer));
    System.out.println(separator());
    System.out.println(playerAction(activePlayer));
  }

  /*
   * HELPER
   */

  private CardLevel promptCardLevel() {
    if (phase == Phase.BASE) {
      return CardLevel.ONE;
    } else {
      var cardLevel = promptForValidInt(() -> System.out.println("Choose card level [1-3]"),
          i -> i >= 1 || i <= 3);
      return CardLevel.fromInt(cardLevel);
    }
  }

  private int promptCardIndex() {
    return promptForValidInt(() -> System.out.println("Choose the card position (0-3)"),
        i -> i >= 0 || i <= 3);
  }

  private String playerAction(Player player) {
    Objects.requireNonNull(player);
    return "! " + player.name() + " Turn" + """

        ? What will you do :
        \t- Buy a card\t: buy
        \t- Take tokens\t: take
        """ + (phase == Phase.COMPLETE ? """
        \t- Reserve a card\t: res
        \t- Buy a reserved card\t: buy_res
        """ : "");
  }

  private static String cardsLeft(Map<CardLevel, Integer> cardsLeft) {
    return cardsLeft.entrySet().stream()
        .sorted(Comparator.comparing((Map.Entry<CardLevel, Integer> e) -> e.getKey().ordinal()).reversed())
        .map(e -> "Cards left in deck " + e.getKey().name() + ": " + e.getValue())
        .collect(Collectors.joining("\n"));
  }

  private static String onBoardCards(Map<CardLevel, Map<Integer, Card>> onBoard) {
    return onBoard.entrySet().stream()
        .sorted(Comparator.comparing((Map.Entry<CardLevel, Map<Integer, Card>> e) -> e.getKey().ordinal()).reversed())
        .map(el -> "Level : " + el.getKey().name() + "\n" + oneLevelCard(el.getValue()))
        .collect(Collectors.joining("\n"));
  }

  private static String oneLevelCard(Map<Integer, Card> onBoard) {
    return IntStream.range(0, 4).mapToObj(i -> "\tpos " + i + ": " + onBoard.get(i))
        .collect(Collectors.joining("\n"));
  }

  private static String tokenBank(TokenCollection tokenBank) {
    return "Gem Bank : " + tokenBank;
  }

  private static String separator() {
    return "\n+===========================+\n\n";
  }

  private String playerInfos(List<Player> playersList, Player activePlayer) {
    var sb = new StringBuilder();
    for (var player : playersList) {
      sb.append(player.name()).append(" : \n");
      sb.append("\t - Prestige : ").append(player.prestige()).append("\n");
      sb.append("\t - Gems : ").append(player.tokens()).append("\n");
      sb.append("\t - Bonus : ").append(player.bonus()).append("\n");
      if (phase == Phase.COMPLETE) {
        sb.append("\t - Claimed Nobles :\n").append(nobleList(player.playerNobles()))
            .append("\n");
        if (player.equals(activePlayer)) {
          sb.append("\t - Reserved Cards :\n").append(cardList(player.reservedCards()))
              .append("\n");
        }
      }
    }
    return sb.toString();
  }

  private static String nobleList(List<Noble> nobles) {
    return nobles.stream().map(Noble::toString).collect(Collectors.joining("\n"));
  }

  private static String cardList(List<Card> nobles) {
    return nobles.stream().map(Card::toString).collect(Collectors.joining("\n"));
  }

  private static void printWrongFormat() {
    System.out.println("Wrong format, please try again");
  }

  /*
   * UTILS
   */

  private List<String> readWords() {
    return Arrays.asList(scanner.nextLine().split("\\s+"));
  }

  private String readLine() {
    return scanner.nextLine().trim();
  }

  private int promptForValidInt(Runnable prompt, Predicate<Integer> validInput) {
    Optional<Integer> result;
    do {
      prompt.run();
      var input = readLine();
      result = parseAndValidateInt(input, validInput);
    } while (result.isEmpty());
    return result.get();
  }

  private static Optional<Integer> parseAndValidateInt(String s, Predicate<Integer> validInt) {
    try {
      var result = Integer.parseInt(s);
      if (!validInt.test(result)) {
        TUI.printWrongFormat();
        return Optional.empty();
      }
      return Optional.of(result);
    } catch (NumberFormatException e) {
      TUI.printWrongFormat();
      return Optional.empty();
    }
  }

  // PUBLIC STATIC METHOD

  public static void printAskPlayerName(int playerNumber) {
    if (playerNumber < 0) {
      throw new IllegalArgumentException("playerNumber must be positive");
    }
    System.out.println("Type player " + playerNumber + " name : ");
  }

  public static void printAskHowMuchPlayer() {
    System.out.println("How much player there is ? (between 2 and 4) ");
  }

}
