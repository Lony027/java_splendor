package view.tui;

public class TUIErrors {
  public static void printWrongTokenFormat() {
    System.out.println(
        "Wrong tokens formats, should be either : 'green', 'blue', 'red', 'white' or 'black'");
  }

  public static void twoDifferentTokens() {
    System.out.println("If you pick two tokens, they should be the same color");
  }
 

  public static void printWrongFormat() {
    System.out.println("Wrong format, please try again");
  }
  
  public static void printPositiveNumber() {
    System.out.println("Wrong format, please type a positive number");
  }
}
