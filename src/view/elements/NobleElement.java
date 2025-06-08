package view.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import model.Noble;
import view.layout.BatchLayout;
import view.layout.SimpleLayout;

public record NobleElement(Noble noble, SimpleLayout layout) implements Element {

  public NobleElement {
    Objects.requireNonNull(noble);
    Objects.requireNonNull(layout);
  }

  public static NobleElement create(Graphics2D g2d, Noble noble, SimpleLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(noble);
    Objects.requireNonNull(layout);

    var x = layout.x();
    var y = layout.y();
    var width = layout.width();
    var height = layout.height();

    g2d.setColor(DARK);
    g2d.fillRoundRect(x, y, width, height, ROUNDING, ROUNDING);

    var textX = Element.xOffsetPct(x, width, 85);
    var textY = Element.yOffsetPct(y, height, 15);
    Text.create("", g2d, Color.WHITE, Integer.toString(noble.prestige()), textX, textY, true, 24,
        true);

    var baseX = Element.xOffsetPct(x, width, 5);
    var baseY = Element.yOffsetPct(y, height, 70);
    var incrementY = -Element.heightPct(height, 30);
    var tokenDiameter = Element.heightPct(height, 25);

    var bankLayout = new BatchLayout(baseX, baseY, tokenDiameter, tokenDiameter, 0, incrementY);
    TokenElement.createBatch(g2d, noble.bonusPrice(), bankLayout, true, false, false);

    return new NobleElement(noble, layout);
  }

  public static List<NobleElement> createBatch(Graphics2D g2d, List<Noble> nobles,
      BatchLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(nobles);
    Objects.requireNonNull(layout);

    return IntStream.range(0, nobles.size()).mapToObj(i -> {
      var noble = nobles.get(i);
      var x = layout.x() + i * layout.incrementX();
      var y = layout.y() + i * layout.incrementY();
      return create(g2d, noble, new SimpleLayout(x, y, layout.width(), layout.height()));
    }).toList();
  }
}
