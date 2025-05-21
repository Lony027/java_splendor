package controller;

import java.util.Optional;
import view.tui.TUIErrors;

public class Utils {

  public static Optional<Integer> testIfPositiveNumberAndPrintError(String s) {
    try {
      var result = Integer.parseInt(s);
      if (result < 0) {
        TUIErrors.printPositiveNumber();
        return Optional.empty();
      }
      return Optional.of(result);
    } catch (NumberFormatException e) {
      TUIErrors.printWrongFormat();
      return Optional.empty();
    }
  }
  
  public static Optional<Integer> testIfNumberAndPrintError(String s) {
    try {
      var result = Integer.parseInt(s);
      return Optional.of(result);
    } catch (NumberFormatException e) {
      TUIErrors.printWrongFormat();
      return Optional.empty();
    }
  }
}
