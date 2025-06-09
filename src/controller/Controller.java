package controller;


import java.nio.file.Path;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import model.Board;
import model.GameRuleException;
import model.Phase;
import model.Player;
import model.Token;
import view.PlayerCancelException;
import view.View;

public record Controller(Phase phase, Board board, View view) {

  public static Controller controllerFactory(View view, Phase phase) {
    var playersNameList = view.promptPlayerNames();
    var playersList =
        playersNameList.stream().map(s -> new Player(s, phase == Phase.COMPLETE)).toList();
    var cardPath = cardsPathByPhase(phase);
    var noblePath = Path.of("src", "data", "nobles_phase_2.csv");
    var board =
        Board.create(playersList.size(), cardPath, noblePath, phase, List.copyOf(playersList));
    return new Controller(phase, board, view);
  }

  public void gameLoop() {
    var playerList = board.playerList();

    Optional<Player> winner;
    do {
      for (var player : playerList) {
        view.printTurn(player, board);
        repeatUntilTrue(() -> playTurn(player));
        repeatUntilTrue(() -> checkAfterTurn(player));
      }
      winner = board.winner();
    } while (winner.isEmpty());

    view.printWinner(winner.get().name());
  }

  private boolean playTurn(Player player) {
    try {
      parseAndExecuteAction(player);
      return true;
    } catch (PlayerCancelException p) {
      view.printTurn(player, board);
      return false;
    } catch (GameRuleException e) {
      view.printException(e);
      return false;
    }
  }

  private boolean checkAfterTurn(Player player) {
    try {
      if (player.hasExtraTokens()) {
        promptGiveBackExtraToken(player);
      }
      if (phase != Phase.BASE) {
        promptClaimNoble(player);
      }
      return true;
    } catch (GameRuleException e) {
      view.printException(e);
      return false;
    }
  }

  private void parseAndExecuteAction(Player player) {
    var actionChoosed = view.promptPlayerAction();
    switch (actionChoosed) {
      case Action.BUY -> {
        var cardLevelIndex = view.promptCardLevelIndex();
        board.buyCard(player, cardLevelIndex);
      }
      // TAKE TOKEN
      case Action.TAKE -> {
        var tokens = view.promptTakeTokens();
        playerTakeToken(tokens, player);
      }
      // RESERVE CARD
      case Action.RESERVE -> {
        if (phase == Phase.BASE) {
          throw new IllegalStateException();
        }
        var cardLevelIndex = view.promptCardLevelIndexReserve();
        board.playerReserveCard(player, cardLevelIndex);

      }
      // BUY RESERVED CARD
      case Action.BUY_RESERVED -> {
        if (phase == Phase.BASE) {
          throw new IllegalStateException();
        }
        if (player.reservedCards().size() == 0) {
          throw new GameRuleException("You have no cards to reserve");
        }
        var reservedCardIndex = view.promptReservedCardIndex();
        board.buyReserved(player, reservedCardIndex);
      }
    };
  }

  private void promptGiveBackExtraToken(Player player) {
    var extraTokenCount = player.extraTokensCount();
    var tokenList = view.promptGiveBackExtraTokens(player, extraTokenCount);
    board.playerGivesBackToken(player, tokenList);
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
    board.playerClaimNoble(player, chosenNoble);
  }

  private void playerTakeToken(List<Token> tokenList, Player player) {
    switch (tokenList.size()) {
      case 2 -> board.playerTakeTwoTokens(player, tokenList);
      case 3 -> board.playerTakeThreeTokens(player, tokenList);
      default -> throw new NumberFormatException();
    }
  }

  private static Path cardsPathByPhase(Phase phase) {
    var fileName = switch (phase) {
      case Phase.BASE -> "cards_phase_1.csv";
      case Phase.COMPLETE -> "cards_phase_2.csv";
    };
    return Path.of("src", "data", fileName);
  }

  private static void repeatUntilTrue(BooleanSupplier action) {
    while (!action.getAsBoolean()) {
    }
  }

}
