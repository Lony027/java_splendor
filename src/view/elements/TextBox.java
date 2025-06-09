package view.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import view.layout.BatchLayout;
import view.layout.SimpleLayout;

public record TextBox(String id, SimpleLayout layout) implements Element {


  public TextBox {
    Objects.requireNonNull(id);
    Objects.requireNonNull(layout);
  }

  public static TextBox create(String id, Graphics2D g2d, Color color, Color textColor,
      String message, boolean hasBorder, SimpleLayout layout, boolean opaque, boolean toCenter) {

    Objects.requireNonNull(g2d);
    Objects.requireNonNull(textColor);
    Objects.requireNonNull(message);
    Objects.requireNonNull(layout);

    var x = layout.x();
    var y = layout.y();
    var width = layout.width();
    var height = layout.height();

    var boxX = toCenter ? x - width / 2 : x;
    var boxY = toCenter ? y - height / 2 : y;
    var borderColor = hasBorder ? Color.WHITE : color;

    Box.create(g2d, new SimpleLayout(boxX, boxY, width, height), opaque, borderColor);

    if (hasBorder) {
      var innerX = boxX + BORDER_SIZE;
      var innerY = boxY + BORDER_SIZE;
      var innerWidth = width - (BORDER_SIZE * 2);
      var innerHeight = height - (BORDER_SIZE * 2);
      Box.create(g2d, new SimpleLayout(innerX, innerY, innerWidth, innerHeight), opaque, color);
    }
    var textX = toCenter ? x : x + width / 2;
    var textY = toCenter ? y : y + height / 2;
    Text.create(g2d, textColor, message, textX, textY, true, 24, false);

    return new TextBox(id, new SimpleLayout(boxX, boxY, width, height));
  }

  public static List<TextBox> createBatch(Graphics2D g2d, List<String> messages, BatchLayout layout,
      IntFunction<String> idBehaviour, Color textColor, boolean hasBorder, boolean opaque,
      boolean toCenter) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(messages);
    Objects.requireNonNull(layout);
    Objects.requireNonNull(idBehaviour);

    return IntStream.range(0, messages.size()).mapToObj(i -> {
      var message = messages.get(i);
      var x = layout.x() + i * layout.incrementX();
      var y = layout.y() + i * layout.incrementY();
      var id = idBehaviour.apply(i);
      return create(id, g2d, DARK, textColor, message, hasBorder,
          new SimpleLayout(x, y, layout.width(), layout.height()), opaque, toCenter);
    }).toList();
  }
}
