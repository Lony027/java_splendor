package view;

import java.util.List;
import java.util.Map;
import controller.Action;
import model.Board;
import model.Noble;
import model.Player;
import model.utils.CardLevel;
import model.utils.Token;

public sealed interface View permits GUI, TUI {

  public Action promptPlayerAction();

  public Map.Entry<CardLevel, Integer> promptCardLevelIndex();

  public Map.Entry<CardLevel, Integer> promptCardLevelIndexReserve();

  public int promptReservedCardIndex();

  public List<Token> promptTakeTokens();

  public List<Player> promptPlayerNames();

  public List<Token> promptGiveBackExtraTokens(Player player, int extra);

  public Noble promptPlayerToChooseNoble(List<Noble> nobles);

  public void printWinner(String playerName);

  public void printException(Exception e);

  public void printTurn(Player activePlayer, Board board);

}
