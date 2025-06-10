package application;

import java.awt.Color;
import com.github.forax.zen.Application;
import controller.Controller;
import model.Phase;
import view.GUI;
import view.TUI;
import view.View;

public class Main {

  public static void main(String[] args) {
    var isTerminal = false;
    var phase = Phase.COMPLETE;

    for (String arg : args) {
      switch (arg.toLowerCase()) {
        case "--terminal" -> isTerminal = true;
        case "--base" -> phase = Phase.BASE;
        default -> {
          System.err.println("Unknown argument: " + arg);
          System.err.println("Usage: java -jar Splendor.jar [--terminal] [--base]");
          return;
        }
      }
    }
    startGame(isTerminal, phase);

  }

  private static void startGame(boolean isTerminal, Phase phase) {
    if (isTerminal) {
      View tui = new TUI(phase);
      var controller = Controller.controllerFactory(tui, phase);
      controller.gameLoop();

    } else {
      Application.run(Color.WHITE, context -> {
        View gui = new GUI(context, phase);
        var controller = Controller.controllerFactory(gui, phase);
        controller.gameLoop();
        context.dispose();
      });
    }
  }

}
