package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import model.Board;
import model.Player;
import model.TokenCollection;
import model.utils.Token;
import view.tui.TUIErrors;
import view.tui.TUITurn;

public class PlayerActions {

  // @to-do: create custom exceptions that will be readable by the view (either GUI or TUI)
  // For now, exceptions are printed here, in the future they'll be in the view

  public static boolean parseAndExecuteAction(Scanner scanner, Board board, Player player) {
    Objects.requireNonNull(scanner);
    Objects.requireNonNull(board);

    var parts = Arrays.asList(scanner.nextLine().split("\\s+"));
    var actionChoosed = parts.get(0);

    switch (actionChoosed) {
      case "b" -> {
        var result = Utils.testIfPositiveNumberAndPrintError(parts.get(1));
        if (result.isEmpty()) {
          return false;
        }
        return PlayerActions.playerBuyCard(result.get(), board, player);
      }
      case "t" -> {
        var tokens = parts.subList(1, parts.size());
        var returnValues = PlayerActions.playerBuysTokens(tokens, board, player);
        if (player.hasExtraTokens()) {
          playerGivesBackExtraTokens(scanner, player);
        }
        return returnValues;
      }
      default -> {
        TUIErrors.printWrongFormat();
        return false;
      }
    }
  }

  private static boolean playerBuyCard(int position, Board board, Player player) {
    Objects.requireNonNull(board);
    Objects.requireNonNull(player);
    if (position < 0 || position > 3) {
      TUIErrors.printWrongFormat();
      return false;
    }

    try {
      board.buyCard(player, position);
      return true;
    } catch (Exception e) {
      // @to-do: Has to be done by the view
      System.out.println(e.getMessage());
      return false;
    }
  }

  private static boolean playerBuysTokens(List<String> stringTokenList, Board board,
      Player player) {
    var optionalTokenList = getTokenListFromString(stringTokenList);
    if (optionalTokenList.isEmpty()) {
      return false;
    }
    var tokenList = optionalTokenList.get();
    try {
      switch (tokenList.size()) {
        case 2 -> board.playerTakeTwoGems(player, tokenList);
        case 3 -> board.playerTakeThreeGems(player, tokenList);
        default -> {
          TUIErrors.printWrongFormat();
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      System.out.println(e.getMessage()); // should be in TUI
      return false;
    }
  }

  private static void playerGivesBackExtraTokens(Scanner scanner, Player player) {
    var extras = player.extraTokensCount();
    TUITurn.printAskPlayerToRemoveTokens(extras);

    List<String> userTypedTokens;
    Optional<List<Token>> tokenList;
    do {
      userTypedTokens = Arrays.asList(scanner.nextLine().split("\\s+"));
      tokenList = getTokenListFromString(userTypedTokens);
    } while (userTypedTokens.size() != extras || tokenList.isEmpty());

    player.removeToken(new TokenCollection(tokenList.get()));
  }

  private static Optional<Token> getTokenFromString(String s) {
    Objects.requireNonNull(s);
    return switch (s) {
      case "green" -> Optional.of(Token.GREEN);
      case "blue" -> Optional.of(Token.BLUE);
      case "red" -> Optional.of(Token.RED);
      case "white" -> Optional.of(Token.WHITE);
      case "black" -> Optional.of(Token.BLACK);
      default -> Optional.empty();
    };
  }

  private static Optional<List<Token>> getTokenListFromString(List<String> stringTokenList) {
    Objects.requireNonNull(stringTokenList);
    var chosenTokens = new ArrayList<Token>();
    for (var tokenName : stringTokenList) {
      var chosenToken = getTokenFromString(tokenName);
      if (chosenToken.isEmpty()) {
        TUIErrors.printWrongTokenFormat();
        return Optional.empty();
      }
      chosenTokens.add(chosenToken.get());
    }
    return Optional.of(chosenTokens);
  }
}


