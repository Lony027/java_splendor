package view.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import model.Card;
import model.utils.CardLevel;
import view.layout.BatchLayout;
import view.layout.SimpleLayout;

public record CardElement(Card card, int cardIndex, SimpleLayout layout, boolean isReserved)
    implements Element {

  public CardElement {
    Objects.requireNonNull(card);
    Objects.requireNonNull(layout);
    if (cardIndex < 0) {
      throw new IllegalArgumentException("cardIndex must be positive");
    }
  }

  public static CardElement create(Graphics2D g2d, Card card, int cardIndex, SimpleLayout layout,
      boolean isReserved) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(card);
    Objects.requireNonNull(layout);

    g2d.setColor(DARK);
    g2d.fillRoundRect(layout.x(), layout.y(), layout.width(), layout.height(), ROUNDING, ROUNDING);

    var textX = Element.xOffsetPct(layout.x(), layout.width(), 15);
    var textY = Element.yOffsetPct(layout.y(), layout.height(), 12);
    Text.create(g2d, Color.WHITE, Integer.toString(card.prestige()), textX, textY, true, 24, true);

    var bonusCircleDiameter = Element.heightPct(layout.height(), 15);
    var bonusX = Element.xOffsetPct(layout.x(), layout.width(), 70);
    var bonusY = Element.yOffsetPct(layout.y(), layout.height(), 5);
    g2d.setColor(TokenElement.colorOf(card.bonus()));
    g2d.fillOval(bonusX, bonusY, bonusCircleDiameter, bonusCircleDiameter);

    var baseX = Element.xOffsetPct(layout.x(), layout.width(), 5);
    var baseY = Element.yOffsetPct(layout.y(), layout.height(), 80);
    var incrementY = -Element.heightPct(layout.height(), 15);
    var tokenDiameter = bonusCircleDiameter;
    var tokenLayout = new BatchLayout(baseX, baseY, tokenDiameter, tokenDiameter, 0, incrementY);
    TokenElement.createBatch(g2d, card.price(), tokenLayout, false, false, false);

    return new CardElement(card, cardIndex, layout, isReserved);
  }

  public static List<CardElement> createBatchReserved(Graphics2D g2d, List<Card> cards,
      BatchLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(cards);
    Objects.requireNonNull(layout);

    return IntStream.range(0, cards.size()).mapToObj(i -> {
      var card = cards.get(i);
      var x = layout.x() + i * layout.incrementX();
      var y = layout.y() + i * layout.incrementY();
      return create(g2d, card, i, new SimpleLayout(x, y, layout.width(), layout.height()), true);
    }).toList();
  }

  public static List<CardElement> createLevelBatch(Graphics2D g2d,
      Map<CardLevel, Map<Integer, Card>> cards, BatchLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(cards);
    Objects.requireNonNull(layout);

    var elements = new ArrayList<CardElement>();
    var levels = CardLevel.values();

    for (int row = 0; row < levels.length; row++) {
      // Ensure cardStack are printed is in order (kind of ugly ahah)
      var level = levels[levels.length - 1 - row];
      var cardRow = cards.get(level);
      if (cardRow == null)
        continue;

      var col = 0;
      for (var entry : cardRow.entrySet()) {
        int x = layout.x() + col * layout.incrementX();
        int y = layout.y() + row * layout.incrementY();
        if (entry.getValue() != null) {
          elements.add(create(g2d, entry.getValue(), entry.getKey(),
              new SimpleLayout(x, y, layout.width(), layout.height()), false));
        }
        col++;
      }
    }

    return elements;
  }
}
