package view.elements;

import java.awt.Color;
import view.layout.SimpleLayout;

public sealed interface Element
    permits Text, Box, Tooltip, Popup, TextBox, TokenElement, NobleElement, CardElement, CardStack, PlayerElement {

  final static int ROUNDING = 15;
  final static int BORDER_SIZE = 2;

  final static Color DARK = new Color(103, 103, 103);
  final static Color LIGHT = new Color(192, 75, 35);
  final static Color BLUE = new Color(0, 87, 231);
  final static Color GREEN = new Color(0, 135, 68);
  final static Color RED = new Color(214, 45, 32);
  final static Color YELLOW = new Color(255, 167, 0);

  SimpleLayout layout();

  static int widthPct(int width, int percent) {
    var mult = percent / 100.0;
    return (int) Math.round(width * mult);
  }

  static int heightPct(int heigth, int percent) {
    var mult = percent / 100.0;
    return (int) Math.round(heigth * mult);
  }

  static int xOffsetPct(int x, int width, int percent) {
    var mult = percent / 100.0;
    return (int) Math.round(width * mult) + x;
  }

  static int yOffsetPct(int y, int heigth, int percent) {
    var mult = percent / 100.0;
    return (int) Math.round(heigth * mult) + y;
  }



}
