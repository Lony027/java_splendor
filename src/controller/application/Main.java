package controller.application;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.Scanner;
import com.github.forax.zen.Application;
import controller.Controller;
import model.utils.Phase;
import view.GUI;
import view.TUI;
import view.View;

public class Main {

  public static void main(String[] args) {
    startGame();
    
// Passer phase en argument !!!!
    // Ouvrir une issue sur zen
    
//    Frame frame = new Frame("AWT Maximized Frame Test");
//
//    Label label = new Label("This frame should start maximized.", Label.CENTER);
//    frame.add(label);
//
//    // Set default size in case maximization fails
////    frame.setSize(10, 100);
//
//    // Add window close handler to exit app
//    frame.addWindowListener(new WindowAdapter() {
//        public void windowClosing(WindowEvent e) {
//            frame.dispose();
//            System.exit(0);  // ensure app exits
//        }
//    });
//
//    // Show frame first (some platforms require this before maximizing)
//    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
//    frame.setVisible(true);
    
//     System.out.println(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
    
  }

  // Should be HERE ????
  public static void startGame() {
    var scanner = new Scanner(System.in);
//    var phase = TUI.promptPhaseSelection(scanner);
    
    // TO REMOVE !!!
    var phase = Phase.THREE;
    
    switch (phase) {
      case Phase.ONE, Phase.TWO -> {
        View tui = new TUI(scanner, phase);
        var controller = Controller.controllerFactory(tui, phase);
        controller.gameLoop();
      }
      case Phase.THREE -> {
        Application.run(Color.WHITE, context -> {
          View gui = new GUI(context);
          var controller = Controller.controllerFactory(gui, phase);
          controller.gameLoop();
//          context.dispose(); //?????
        });
      }
    }

  }
}

