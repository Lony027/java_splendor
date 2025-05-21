package view.tui;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import view.dto.CardDTO;
import view.dto.PlayerDTO;
import view.dto.TokenCollectionDTO;

public class TUITurn {

  public static void printTurn(List<PlayerDTO> playersList, TokenCollectionDTO tokenBank,
      int cardsLeft, Map<Integer, CardDTO> onBoard, PlayerDTO activePlayer) {
    Objects.requireNonNull(playersList);
    Objects.requireNonNull(tokenBank);
    Objects.requireNonNull(onBoard);
    Objects.requireNonNull(activePlayer);

    System.out.println(cardsLeft(cardsLeft));
    System.out.println(onBoardCards(onBoard));
    System.out.println(tokenBank(tokenBank));
    System.out.println(separator());
    System.out.println(playerInfos(playersList));
    System.out.println(separator());
    System.out.println(playerAction(activePlayer));
  }
  
  public static void printAskPlayerToRemoveTokens(int extra) {
    System.out.println("You have " + extra
        + " extra tokens, type the one you want to remove.\n Example: (green blue blue)");
  }

  private static String cardsLeft(int cardsLeft) {
    return "Cards left in deck : " + cardsLeft;
  }


  private static String onBoardCards(Map<Integer, CardDTO> onBoard) {
    Objects.requireNonNull(onBoard);

    var sb = new StringBuilder();
    // .boxed to convert to Stream<Integer> and be able to map it to String
    var cardsString = IntStream.range(0, 4).boxed()
        .map(i -> "Position " + i + ": " + onBoard.get(i).cardSummary())
        .collect(Collectors.joining("\n"));
    sb.append(cardsString);
    return sb.toString();
  }

  private static String tokenBank(TokenCollectionDTO tokenBank) {
    Objects.requireNonNull(tokenBank);
    return "Gem Bank : " + tokenBank.tokenSummary();
  }

  private static String separator() {
    return "\n+===========================+\n\n";
  }

  private static String playerInfos(List<PlayerDTO> playersList) {
    Objects.requireNonNull(playersList);
    var sb = new StringBuilder();

    for (var p : playersList) {
      sb.append(p.name()).append(" : \n");
      sb.append("\t - Prestige : ").append(p.prestige()).append("\n");
      sb.append("\t - Gems : ").append(p.tokenSummary()).append("\n");
    }
    return sb.toString();
  }

  private static String playerAction(PlayerDTO player) {
    Objects.requireNonNull(player);
    // Soutenance : May be confusing
    return "! " + player.name() + " Turn" + """

        ? What will you do :
        \t- Buy a card\t: b (position)
        \t- Take token\t: t (color1 color2 color3) or (t color color)
        """;
  }
}
