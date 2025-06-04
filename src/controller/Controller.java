package controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BooleanSupplier;
import model.Board;
import model.Player;
import model.TokenCollection;
import model.utils.Phase;
import model.utils.Token;
import view.GUI;
import view.TUI;
import view.View;

public record Controller(Phase phase, Board board, View view) {

  // Pattern matching quand y'aura GUI/TUI

  public static Controller controllerFactory() {
    var scanner = new Scanner(System.in);
    var phase = TUI.promptPhaseSelection(scanner);
    var playersList = TUI.promptPlayerNames(scanner);
    var cardPath = cardsPathByPhase(phase);
    var noblePath = Paths.get("src", "model", "data", "nobles_phase_2.csv");
    var board = Board.factory(playersList.size(), cardPath, noblePath, phase, List.copyOf(playersList));

    View view = switch (phase) {
      case Phase.ONE, Phase.TWO -> new TUI(scanner, phase, List.copyOf(playersList), board);
      case Phase.THREE -> new GUI();
    };
    return new Controller(phase, board, view);
  }

  public void gameLoop() {
    var playerList = board.playerList();

    Optional<Player> winner;
    do {
      for (var player : playerList) {
        view.printTurn(player);
        repeatUntilTrue(() -> playTurn(player));
        repeatUntilTrue(() -> checkAfterTurn(player));
      }
      winner = board.winner();
    } while (winner.isEmpty());

    view.printWinner(winner.get().name());
  }

  private void repeatUntilTrue(BooleanSupplier action) {
    while (!action.getAsBoolean()) {
    }
  }


  // TODO: Custom exception and catch only the custom one
  // Because we catch exception that should'nt be
  private boolean playTurn(Player player) {
    try {
      parseAndExecuteAction(player);
      return true;
    } catch (NumberFormatException e) {
      TUI.printWrongFormat();
      return false;
    } catch (Exception e) {
      view.printException(e);
      return false;
    }
  }

  private boolean checkAfterTurn(Player player) {
    try {
      if (player.hasExtraTokens()) {
        promptGiveBackExtraToken(player);
      }
      if (phase != Phase.ONE) {
        promptClaimNoble(player);
      }
      return true;
    } catch (Exception e) {
      view.printException(e);
      return false;
    }
  }

  // TODO: Create custom exceptions (here and in the model) that will be readable by the view
  // (either GUI or TUI)

  // TODO: handle phases in a better way
  private void parseAndExecuteAction(Player player) {
    // var parts = Arrays.asList(scanner.nextLine().split("\\s+"));
    // var actionChoosed = parts.get(0);

    var actionChoosed = view.promptPlayerAction();
    switch (actionChoosed) {
      case Action.BUY -> {
        var cardLevel = view.promptCardLevel();
        var cardIndex = view.promptCardIndex();
        board.buyCard(player, cardIndex, cardLevel);
      }
      // TAKE TOKEN
      case Action.TAKE -> {
        var tokens = view.promptTakeTokens();
        playerTakeToken(tokens, player);
      }
      // RESERVE CARD
      case Action.RESERVE -> {
        if (phase == Phase.ONE) {
          throw new NumberFormatException();
        }
        var cardLevel = view.promptCardLevel();
        var cardIndex = view.promptCardIndex();
        board.playerReserveCard(player, cardIndex, cardLevel);

      }
      // BUY RESERVED CARD
      case Action.BUY_RESERVED -> {
        if (phase == Phase.ONE) {
          throw new NumberFormatException();
        }
        var reservedCardIndex = view.promptReservedCardIndex();
        board.buyReserved(player, reservedCardIndex);
      }
    };
  }

  private void promptGiveBackExtraToken(Player player) {
    var extraTokenCount = player.extraTokensCount();
    var tokenList = view.promptGiveBackExtraTokens(extraTokenCount);
    board.playerGivesBackToken(player, TokenCollection.fromList(tokenList));
  }

  private void promptClaimNoble(Player player) {
    var nobles = board.noblesPlayerCanClaim(player);
    if (nobles.isEmpty()) {
      return;
    }

    var chosenNoble = nobles.get(0);
    if (nobles.size() > 1) {
      chosenNoble = view.promptPlayerToChooseNoble(nobles);

    }
    player.claimNoble(chosenNoble);
  }

  // Should be separated and string part in tui, here should be token colleciton
  private void playerTakeToken(List<Token> tokenList, Player player) {
    switch (tokenList.size()) {
      case 2 -> board.playerTakeTwoTokens(player, tokenList);
      case 3 -> board.playerTakeThreeTokens(player, tokenList);
      default -> throw new NumberFormatException();
    }
  }


  private static Path cardsPathByPhase(Phase phase) {
    var fileName = switch (phase) {
      case Phase.ONE -> "cards_phase_1.csv";
      case Phase.TWO -> "cards_phase_2.csv";
      case Phase.THREE -> "cards_phase_3.csv";
    };
    // TODO: Only works in eclipse !!!!
    return Path.of("src", "model", "data", fileName);
  }



}


