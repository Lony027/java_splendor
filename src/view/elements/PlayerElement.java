package view.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import model.Player;
import view.layout.BatchLayout;
import view.layout.SimpleLayout;

public record PlayerElement(SimpleLayout layout) implements Element {

  public PlayerElement {
    Objects.requireNonNull(layout);
  }

  public static PlayerElement create(Graphics2D g2d, Player player, boolean activePlayer,
      SimpleLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(player);
    Objects.requireNonNull(layout);
    
    Box.create(g2d, layout, false, activePlayer ? RED : Color.BLACK);

    Text.create(g2d, Color.LIGHT_GRAY, player.name() + " :",
        Element.xOffsetPct(layout.x(), layout.width(), 5),
        Element.yOffsetPct(layout.y(), layout.height(), 15), false, 22, false);

    Text.create(g2d, Color.LIGHT_GRAY, "Tokens :",
        Element.xOffsetPct(layout.x(), layout.width(), 5),
        Element.yOffsetPct(layout.y(), layout.height(), 37), false, 22, false);

    var tokenX = Element.xOffsetPct(layout.x(), layout.width(), 27);
    var tokenY = Element.yOffsetPct(layout.y(), layout.height(), 23);
    var tokenH = Element.widthPct(layout.width(), 10);
    var tokenIncr = Element.widthPct(layout.width(), 12);
    var tokensLayout = new BatchLayout(tokenX, tokenY, tokenH, tokenH, tokenIncr, 0);
    TokenElement.createBatch(g2d, player.tokens(), tokensLayout, false, true, false);

    Text.create(g2d, Color.LIGHT_GRAY, "Bonus :", Element.xOffsetPct(layout.x(), layout.width(), 5),
        Element.yOffsetPct(layout.y(), layout.height(), 60), false, 22, false);
    var bonusY = Element.yOffsetPct(layout.y(), layout.height(), 47);
    var bonusLayout = new BatchLayout(tokenX, bonusY, tokenH, tokenH, tokenIncr, 0);
    TokenElement.createBatch(g2d, player.bonus(), bonusLayout, true, true, false);

    Text.create(g2d, Color.LIGHT_GRAY, "Token : " + player.tokens().total() + "/10",
        Element.xOffsetPct(layout.x(), layout.width(), 5),
        Element.yOffsetPct(layout.y(), layout.height(), 90), false, 22, false);

    Text.create(g2d, Color.LIGHT_GRAY, "Prestige : " + player.prestige(),
        Element.xOffsetPct(layout.x(), layout.width(), 55),
        Element.yOffsetPct(layout.y(), layout.height(), 90), false, 22, false);

    return new PlayerElement(layout);
  }

  public static List<PlayerElement> createBatch(Graphics2D g2d, List<Player> players, Player active,
      BatchLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(players);
    Objects.requireNonNull(active);
    Objects.requireNonNull(layout);

    return IntStream.range(0, players.size()).mapToObj(i -> {
      var player = players.get(i);
      var x = layout.x() + i * layout.incrementX();
      var y = layout.y() + i * layout.incrementY();
      return create(g2d, player, player.equals(active),
          new SimpleLayout(x, y, layout.width(), layout.height()));
    }).toList();
  }
}
