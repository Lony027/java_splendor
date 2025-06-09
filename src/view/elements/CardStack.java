package view.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import model.utils.CardLevel;
import view.layout.BatchLayout;
import view.layout.SimpleLayout;

public record CardStack(CardLevel cardLevel, SimpleLayout layout) implements Element {

  public CardStack {
    Objects.requireNonNull(layout);
  }

  public static CardStack create(Graphics2D g2d, CardLevel cardLevel, int cardLeft,
      SimpleLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(layout);
    if (cardLeft < 0) {
      throw new IllegalArgumentException("cardLeft must be positive");
    }

    g2d.setColor(colorByCardLevel(cardLevel));
    g2d.fillRoundRect(layout.x(), layout.y(), layout.width(), layout.height(), ROUNDING, ROUNDING);

    var prestigeCircleRadius = Element.heightPct(layout.height(), 30);
    var prestigeX = Element.xOffsetPct(layout.x(), layout.width(), 5);
    var prestigeY = Element.yOffsetPct(layout.y(), layout.height(), 5);
    g2d.setColor(Color.WHITE);
    g2d.fillOval(prestigeX, prestigeY, prestigeCircleRadius, prestigeCircleRadius);

    var textX = Element.xOffsetPct(prestigeX, prestigeCircleRadius, 50);
    var textY = Element.yOffsetPct(prestigeY, prestigeCircleRadius, 50);
    Text.create(g2d, Color.WHITE, Integer.toString(cardLeft), textX, textY, true, 22, true);

    return new CardStack(cardLevel, layout);
  }

  public static List<CardStack> createBatch(Graphics2D g2d, Map<CardLevel, Integer> stackMap,
      BatchLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(stackMap);
    Objects.requireNonNull(layout);

    var list = new ArrayList<CardStack>();
    var x = layout.x();
    var y = layout.y();
    for (var entry : stackMap.entrySet()) {
      if (entry.getValue() != 0) {
        CardStack cardStack = create(g2d, entry.getKey(), entry.getValue(),
            new SimpleLayout(x, y, layout.width(), layout.height()));
        list.add(cardStack);
      }
      x += layout.incrementX();
      y += layout.incrementY();
    }
    return list;
  }

  private static Color colorByCardLevel(CardLevel cardLevel) {
    return switch (cardLevel) {
      case CardLevel.ONE -> GREEN;
      case CardLevel.TWO -> YELLOW;
      case CardLevel.THREE -> BLUE;
    };
  }
}
