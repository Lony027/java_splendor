package view.tui;

import java.util.Objects;

public class TUIGame {

  public static void printAskPlayerName(int playerNumber) {
    System.out.println("Type player " + playerNumber + " name : ");
  }

  public static void printAskHowMuchPlayer() {
    System.out.println("How much player there is ? (between 2 and 4) ");
  }

  public static void printWinner(String playerName) {
    Objects.requireNonNull(playerName);
    System.out.println(playerName + " has won!");
  }



}
