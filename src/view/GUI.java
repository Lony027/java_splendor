package view;

import java.util.List;
import java.util.Map;
import controller.Action;
import model.Card;
import model.Noble;
import model.Player;
import model.TokenCollection;
import model.utils.CardLevel;
import model.utils.Token;

public final class GUI implements View {

  @Override
  public Action promptPlayerAction() {
    // TODO Auto-generated method stub
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
  public void printTurn(Player activePlayer) {
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
