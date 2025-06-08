package view.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Objects;
import view.layout.SimpleLayout;

// check at the end if id was used
public record Text(String id, SimpleLayout layout) implements Element {

  private final static Font FONT = new Font("Serif", Font.PLAIN, 24);
  private final static HashMap<Integer, Font> FONT_CACHE = new HashMap<>();

  public Text {
    Objects.requireNonNull(id);
    Objects.requireNonNull(layout);
  }

  public static Text create(String id, Graphics2D g2d, Color textColor, String message, int x,
      int y, boolean toCenter, int fontSize, boolean stroke) {
    var font = font(fontSize);
    g2d.setFont(font);

    var bounds = g2d.getFontMetrics().getStringBounds(message, g2d);
    var width = (int) Math.round(bounds.getWidth());
    var height = (int) Math.round(bounds.getHeight());

    if (toCenter) {
      x -= width / 2;
      y -= height / 2;
      y += -bounds.getY();
    }

    if (stroke) {
      var gv = font.createGlyphVector(g2d.getFontRenderContext(), message);
      var textShape = gv.getOutline(x, y);

      g2d.setStroke(new BasicStroke(3f));
      g2d.setColor(Color.BLACK);
      g2d.draw(textShape);

      g2d.setColor(textColor);
      g2d.fill(textShape);
    } else {
      g2d.setColor(textColor);
      g2d.drawString(message, x, y);
    }

    return new Text(id, new SimpleLayout(x, y, width, height));
  }

  private static Font font(int size) {
    return FONT_CACHE.computeIfAbsent(size, s -> FONT.deriveFont((float) s));
  }



}
