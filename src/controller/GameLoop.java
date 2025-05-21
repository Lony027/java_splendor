package controller;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import model.Board;
import model.Game;
import model.Player;
import view.dto.CardDTO;
import view.dto.PlayerDTO;
import view.dto.TokenCollectionDTO;
import view.tui.TUIGame;
import view.tui.TUITurn;

public class GameLoop {

  public static void gameLoop() {
    var scanner = new Scanner(System.in);
    var playersList = typePlayerList(scanner);
    var game = new Game(List.copyOf(playersList));
    var dataPath = Paths.get("src", "model", "data", "cards.csv");
    var board = new Board(playersList.size(), dataPath);

    Optional<Player> winner;
    do {
      for (var player : playersList) {
        printTurn(playersList, board, player);
        boolean canExit;
        do {
          canExit = PlayerActions.parseAndExecuteAction(scanner, board, player);
        } while (!canExit);
      }
      winner = game.hasWin();
    } while (winner.isEmpty());

    TUIGame.printWinner(winner.get().name());
    scanner.close();
  }

  // Soutenance : is this a good idea ?
  private static void printTurn(List<Player> playersList, Board board, Player activePlayer) {
    Objects.requireNonNull(playersList);
    Objects.requireNonNull(board);
    Objects.requireNonNull(activePlayer);
    
    var playersListDTO = new ArrayList<PlayerDTO>();
    for (var p : playersList) {
      playersListDTO.add(new PlayerDTO(p.name(), p.prestige(), p.playerTokens().toString(), p.bonus().toString()));
    }
    var tokenBankDTO = new TokenCollectionDTO(board.tokenBankSummary());
    var cardsLeft = board.cardsStackLeft();
    var onBoardDTO = new HashMap<Integer, CardDTO>();
    for (var c : board.onBoardCards().entrySet()) {
      onBoardDTO.put(c.getKey(), new CardDTO(c.getValue().toString()));
    }
    var activePlayerDTO = new PlayerDTO(activePlayer.name(), activePlayer.prestige(),
        activePlayer.playerTokens().toString(), activePlayer.bonus().toString());
    TUITurn.printTurn(playersListDTO, tokenBankDTO, cardsLeft, onBoardDTO,
        activePlayerDTO);
  }

  private static List<Player> typePlayerList(Scanner scanner) {
    Objects.requireNonNull(scanner);
    
    var players = new ArrayList<Player>();
    Optional<Integer> playerCount;
    do {
      TUIGame.printAskHowMuchPlayer();
      playerCount = Utils.testIfNumberAndPrintError(scanner.nextLine().trim());
    } while (playerCount.isEmpty() || (playerCount.get() < 2 || playerCount.get() > 4));

    for (int i = 1; i <= playerCount.get(); i++) {
      String name;
      do {
        TUIGame.printAskPlayerName(i);
        name = scanner.nextLine().trim();
      } while (name.isBlank());
      players.add(new Player(name));
    }
    return players;
  }

}


