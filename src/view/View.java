package view;

import java.util.List;
import controller.Action;
import model.Board;
import model.Noble;
import model.Player;
import model.utils.CardLevel;
import model.utils.Token;

// TODO: Implement DTO
public sealed interface View permits GUI, TUI {

  public Action promptPlayerAction();
  public CardLevel promptCardLevel();
  public int promptCardIndex();
  public List<Token> promptTakeTokens();
  public int promptReservedCardIndex();
  public List<Player> promptPlayerNames();
  
  
  public void printWinner(String playerName);
  public void printException(Exception e);
  public void printWrongTokenFormat();
  public void twoDifferentTokens();
  public void printTurn(Player activePlayer, Board board);
  public List<Token> promptGiveBackExtraTokens(int extra);
  public Noble promptPlayerToChooseNoble(List<Noble> nobles);
}
