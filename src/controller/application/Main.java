package controller.application;

import controller.Controller;

public class Main {

  public static void main(String[] args) {
    var controller = Controller.controllerFactory();
    controller.gameLoop();
  }

}