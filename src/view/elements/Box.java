package view.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;
import view.layout.SimpleLayout;

public record Box(SimpleLayout layout) implements Element {

  public Box {
    Objects.requireNonNull(layout);
  }

  public static Box create(Graphics2D g2d, SimpleLayout layout, boolean opaque, Color color) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(layout);

    if (!opaque) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
    }
    g2d.setColor(color);
    g2d.fillRoundRect(layout.x(), layout.y(), layout.width(), layout.height(), ROUNDING, ROUNDING);
    if (!opaque) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    return new Box(layout);

  }
}
