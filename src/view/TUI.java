package view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import controller.Action;
import model.Board;
import model.Card;
import model.Noble;
import model.Player;
import model.TokenCollection;
import model.utils.CardLevel;
import model.utils.Phase;
import model.utils.Token;

public record TUI(Scanner scanner, Phase phase)
    implements View {
  
  
  
  //
  // TODO: CLEAN ICI!!!!!!!!!!!!!!!
  // USE SUFFIX ET PREFIX DANS COLLECTORS.JOIN !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  //

  public List<Token> promptGiveBackExtraTokens(int extra) {
    System.out.println("You have " + extra
        + " extra tokens, type the one you want to remove.\n Example: (green blue blue)");
    var userTypedTokens = readWords();
    var tokenList = userTypedTokens.stream().map(Token::fromString).toList();
    if (tokenList.size() != extra) {
      throw new IllegalArgumentException("Too much or not enough tokens");
    }
    return tokenList;
  }


  public List<Token> promptTakeTokens() {
    System.out.println("Choose two tokens of the same color or three different");
    var stringTokenList = readWords();
    return stringTokenList.stream().map(Token::fromString).toList();
  }

  public CardLevel promptCardLevel() {
    if (phase == Phase.ONE) {
      return CardLevel.ONE;
    } else {
      System.out.println("Choose card level [1-3]");
      return CardLevel.fromInt(readInteger());
    }
  }

  public int promptCardIndex() {
    System.out.println("Choose the card position (0-3)");
    return readInteger();
  }

  public int promptReservedCardIndex() {
    System.out.println("Choose the card position (0-2)");
    return readInteger();
  }



  public Action promptPlayerAction() {
    var action = scanner.nextLine().trim().toLowerCase();
    return switch (action) {
      case "buy" -> Action.BUY;
      case "take" -> Action.TAKE;
      case "res" -> Action.RESERVE;
      case "buy_res" -> Action.BUY_RESERVED;
      default -> throw new NumberFormatException();
    };
  }


  public static Phase promptPhaseSelection(Scanner scanner) {
    var phase = promptForValidInt(scanner, TUI::printChoosePhase, i -> i >= 1 || i <= 3);
    return Phase.fromInt(phase);
  }

  // List<Strin> puis on converti ??,
  @Override
  public List<Player> promptPlayerNames() {
    var playerCount = promptForValidInt(scanner, TUI::printAskHowMuchPlayer, i -> i >= 2 && i <= 4);

    // TODO: Check if there's already a player named like this, use a set ?
    var players = new ArrayList<Player>();
    for (int i = 1; i <= playerCount; i++) {
      String name;
      do {
        TUI.printAskPlayerName(i);
        name = scanner.nextLine().trim();
      } while (name.isBlank());
      players.add(new Player(name));
    }
    return players;
  }


  /////////////////////////////////////////////////////
  ///
  ///UTILS UTILS UTILS
  /////////////////////////////////////////////////////

  private int readInteger() {
    try {
      return Integer.parseInt(scanner.nextLine());
    } catch (NumberFormatException e) {
      // TODO: another exception type
      throw new IllegalArgumentException("Wrong format, please try again");
    }
  }

  private List<String> readWords() {
    return Arrays.asList(scanner.nextLine().split("\\s+"));
  }


  public static int promptForValidInt(Scanner scanner, Runnable prompt,
      Predicate<Integer> validInput) {
    Objects.requireNonNull(scanner);
    Objects.requireNonNull(prompt);
    Objects.requireNonNull(validInput);

    Optional<Integer> result;
    do {
      prompt.run();
      String input = scanner.nextLine().trim();
      result = parseAndValidateInt(input, validInput);
    } while (result.isEmpty());
    return result.get();
  }

  public static String promptForNonBlankString(Scanner scanner, Runnable prompt) {
    Objects.requireNonNull(scanner);
    Objects.requireNonNull(prompt);
    String input;
    do {
      prompt.run();
      input = scanner.nextLine().trim();
    } while (input.isBlank());
    return input;
  }

  private static Optional<Integer> parseAndValidateInt(String s, Predicate<Integer> validInt) {
    try {
      var result = Integer.parseInt(s);
      if (!validInt.test(result)) {
        TUI.printPositiveNumber();
        return Optional.empty();
      }
      return Optional.of(result);
    } catch (NumberFormatException e) {
      TUI.printWrongFormat();
      return Optional.empty();
    }
  }



  /////////////////////////////////////////////////////
  ///
  /// /////////////////////////////////////////////////////
  /// /////////////////////////////////////////////////////



  // Enlever les truc pas statiques pour rien après
  public TUI {
    if (phase == Phase.THREE) {
      throw new IllegalArgumentException("TUI is meant for phase ONE and TWO");
    }
  }

  public static void printChoosePhase() {
    System.out.println("Choose phase (1, 2 or 3)");
  }

  public static void printAskPlayerName(int playerNumber) {
    System.out.println("Type player " + playerNumber + " name : ");
  }

  public static void printAskHowMuchPlayer() {
    System.out.println("How much player there is ? (between 2 and 4) ");
  }

  public void printWinner(String playerName) {
    Objects.requireNonNull(playerName);
    System.out.println(playerName + " has won!");
  }


  public void printException(Exception e) {
    System.out.println(e.getMessage());
  }

  public void printWrongTokenFormat() {
    System.out.println(
        "Wrong tokens formats, should be either : 'green', 'blue', 'red', 'white' or 'black'");
  }

  public void twoDifferentTokens() {
    System.out.println("If you pick two tokens, they should be the same color");
  }


  public static void printWrongFormat() {
    System.out.println("Wrong format, please try again");
  }

  public static void printPositiveNumber() {
    System.out.println("Wrong format, please type a positive number");
  }


  // Maybe faire un <E>
  private String nobleList(List<Noble> nobles) {
    return nobles.stream().map(Noble::toString).collect(Collectors.joining("\n"));
  }
  private String cardList(List<Card> nobles) {
    return nobles.stream().map(Card::toString).collect(Collectors.joining("\n"));
  }

  public void printTurn(Player activePlayer, Board board) {
    Objects.requireNonNull(activePlayer);

    System.out.println(cardsLeft(board.cardDecksSizes()));
    System.out.println(onBoardCards(board.cards()));
    System.out.println(tokenBank(board.tokenBank()));
    if (phase == Phase.TWO) {
      System.out.println("On Board Nobles :\n" + nobleList(board.nobles()));
    }
    System.out.println(separator());
    System.out.println(playerInfos(board.playerList(), activePlayer));
    System.out.println(separator());
    System.out.println(playerAction(activePlayer));
  }

  // ToO DO CHANGE TO NOBLE DTO!!!
  public Noble promptPlayerToChooseNoble(List<Noble> nobles) {
    Objects.requireNonNull(nobles);
    var prompt = "Choose between theses nobles the one you want (type number) :\n";
    var nobleList = IntStream.range(0, nobles.size()).mapToObj(i -> i + ": " + nobles.get(i))
        .collect(Collectors.joining("\n"));
    System.out.println(prompt + nobleList);
    var nobleIndex =
        promptForValidInt(scanner, TUI::printChoosePhase, i -> i >= 0 || i < nobles.size());
    return nobles.get(nobleIndex);
  }

  private String cardsLeft(Map<CardLevel, Integer> cardsLeft) {
    return cardsLeft.entrySet().stream()
        .map(e -> "Cards left in deck " + e.getKey().name() + ": " + e.getValue())
        .collect(Collectors.joining("\n"));
  }


  private String oneLevelCard(Map<Integer, Card> onBoard) {
    // .boxed to convert to Stream<Integer> and be able to map it to String
    return IntStream.range(0, 4).boxed().map(i -> "\tpos " + i + ": " + onBoard.get(i))
        .collect(Collectors.joining("\n"));
  }


  private String onBoardCards(Map<CardLevel, Map<Integer, Card>> onBoard) {
    Objects.requireNonNull(onBoard);
    return onBoard.entrySet().stream()
        .map(el -> "Level : " + el.getKey().name() + "\n" + oneLevelCard(el.getValue()))
        .collect(Collectors.joining("\n"));
  }

  private String tokenBank(TokenCollection tokenBank) {
    return "Gem Bank : " + tokenBank;
  }

  private String separator() {
    return "\n+===========================+\n\n";
  }

  // goldToken only if

  private String playerInfos(List<Player> playersList, Player activePlayer) {
    var sb = new StringBuilder();

    for (var p : playersList) {
      sb.append(p.name()).append(" : \n");
      sb.append("\t - Prestige : ").append(p.prestige()).append("\n");
      sb.append("\t - Gems : ").append(p.tokens()).append("\n");
      sb.append("\t - Bonus : ").append(p.bonus()).append("\n");
      if (phase == Phase.TWO) {
        sb.append("\t - Claimed Nobles :\n").append(nobleList(p.playerNobles())).append("\n\t");
        if (p.equals(activePlayer)) {
          sb.append("\t - Reserved Cards :\n").append(cardList(p.reservedCards())).append("\n\t");
        }
      }
    }
    return sb.toString();
  }

  public String playerAction(Player player) {
    Objects.requireNonNull(player);
    return "! " + player.name() + " Turn" + """

        ? What will you do :
        \t- Buy a card\t: buy
        \t- Take tokens\t: take
        """ + (phase == Phase.TWO ? """
        \t- Reserve a card\t: res
        \t- Buy a reserved card\t: buy_res
        """ : "");
  }

}
