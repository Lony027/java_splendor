package view;

import java.awt.Color;
import java.util.List;
import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import controller.Action;
import model.Board;
import model.Noble;
import model.Player;
import model.utils.CardLevel;
import model.utils.Token;

public final class GUI implements View {

  private final ApplicationContext context;

  public GUI(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public List<Player> promptPlayerNames() {
    context.renderFrame(g2d -> {
      g2d.setColor(Color.BLACK);

      var screenInfo = context.getScreenInfo();



      // g2d.fillRect(0, 0, screenInfo.width(), screenInfo.height());
      g2d.fillRect(0, 0, 5, 5);
    });
    return List.of(new Player("hugo"));
  }



  @Override
  public Action promptPlayerAction() {
    return null;
  }

  @Override
  public CardLevel promptCardLevel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int promptCardIndex() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<Token> promptTakeTokens() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int promptReservedCardIndex() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void printWinner(String playerName) {
    // TODO Auto-generated method stub

  }

  @Override
  public void printException(Exception e) {
    // TODO Auto-generated method stub

  }

  @Override
  public void printWrongTokenFormat() {
    // TODO Auto-generated method stub

  }

  @Override
  public void twoDifferentTokens() {
    // TODO Auto-generated method stub

  }

  @Override
  public void printTurn(Player activePlayer, Board board) {
    // TODO Auto-generated method stub

  }

  @Override
  public List<Token> promptGiveBackExtraTokens(int extra) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Noble promptPlayerToChooseNoble(List<Noble> nobles) {
    // TODO Auto-generated method stub
    return null;
  }



}
