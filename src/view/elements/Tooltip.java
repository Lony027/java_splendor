package view.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import view.layout.SimpleLayout;

public record Tooltip(Element element, SimpleLayout layout, List<TextBox> options)
    implements Element {

  public Tooltip {
    Objects.requireNonNull(element);
    Objects.requireNonNull(options);
    Objects.requireNonNull(layout);
  }

  public static Tooltip create(Graphics2D g2d, int width, int height, Element element,
      List<String> options) {
    Objects.requireNonNull(g2d);
    Objects.requireNonNull(element);
    if (options.size() < 2 || options.size() > 3) {
      throw new IllegalArgumentException("Options list must contain at least 2 strings.");
    }

    var x = element.layout().x() + (element.layout().width() / 2);
    var y = element.layout().y() + (element.layout().height() / 2);
    var layout = new SimpleLayout(x, y, width, height);
//    Box.create(g2d, layout, true, Color.BLACK);

    var textBoxList = IntStream.range(0, options.size()).mapToObj(i -> {
      var boxHeight = height / options.size();
      var boxY = y + (boxHeight * i);
      var zone = new SimpleLayout(x, boxY, width, boxHeight);
      return TextBox.create(options.get(i), g2d, Color.BLACK, Color.LIGHT_GRAY, options.get(i),
          true, zone, false, false);
    }).toList();

    return new Tooltip(element, layout, textBoxList);
  }

  // créer à partir
  // liste d'option renvoie l'index de l'option selectionner
  // dis si cliqué outside



  // Les texts sont clickables c'est ce que je renvoie dans create Batch!
  // onclick (pollEvent) // -> si outside renvoyer -1, sinon renvoyer le choix selectionné.
}
