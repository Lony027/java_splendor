package view.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import model.TokenCollection;
import model.utils.Token;
import view.layout.BatchLayout;
import view.layout.SimpleLayout;

public record TokenElement(Token token, int tokenCount, SimpleLayout layout) implements Element {

  public TokenElement {
    Objects.requireNonNull(layout);
  }

  public static TokenElement create(Graphics2D g2d, Token token, int tokenCount, boolean square,
      SimpleLayout layout) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(layout);

    var diameter = layout.width();

    g2d.setColor(colorOf(token));
    if (square) {
      g2d.fillRoundRect(layout.x(), layout.y(), diameter, diameter, diameter / 5, diameter / 5);
    } else {
      g2d.fillOval(layout.x(), layout.y(), diameter, diameter);
    } ;

    var textX = Element.xOffsetPct(layout.x(), diameter, 50);
    var textY = Element.yOffsetPct(layout.y(), diameter, 50);
    var toCenter = true;
    var textSize = 22;
    var hasStroke = true;
    Text.create("", g2d, Color.WHITE, Integer.toString(tokenCount), textX, textY, toCenter,
        textSize, hasStroke);

    return new TokenElement(token, tokenCount, layout);
  }

  public static List<TokenElement> createBatch(Graphics2D g2d, TokenCollection tokenCollection,
      BatchLayout layout, boolean isSquare, boolean printEmptyToken, boolean goldClickable) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(tokenCollection);
    Objects.requireNonNull(layout);


    var filteredTokens = tokenCollection.tokens().entrySet().stream()
        .filter(es -> printEmptyToken || es.getValue() > 0).toList();


    var orderedTokens = filteredTokens.stream()
        .sorted(Comparator.comparing(e -> e.getKey() == Token.GOLD)).toList();

    var tokenList = IntStream.range(0, orderedTokens.size()).mapToObj(i -> {
      var entry = orderedTokens.get(i);
      int x = layout.x() + i * layout.incrementX();
      int y = layout.y() + i * layout.incrementY();
      return create(g2d, entry.getKey(), entry.getValue(), isSquare,
          new SimpleLayout(x, y, layout.width(), layout.width()));
    }).toList();

    if (!goldClickable) {
      tokenList = tokenList.stream().filter(e -> e.token != Token.GOLD).toList();
    }

    return tokenList;
  }

  public static Color colorOf(Token token) {
    return switch (token) {
      case Token.WHITE -> Color.WHITE;
      case Token.BLUE -> BLUE;
      case Token.GREEN -> GREEN;
      case Token.RED -> RED;
      case Token.BLACK -> Color.BLACK;
      case Token.GOLD -> YELLOW;
    };
  }
}
