package view.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import view.layout.SimpleLayout;

public record Popup(SimpleLayout layout, List<TextBox> options)
    implements Element {

  public Popup {
    Objects.requireNonNull(options);
    Objects.requireNonNull(layout);
  }

  public static Popup create(Graphics2D g2d, SimpleLayout layout, List<String> options, boolean toCenter) {
    Objects.requireNonNull(g2d);
    if (options.size() < 1 || options.size() > 6) {
      throw new IllegalArgumentException("Options list must contain at least 1 strings and no more than 6.");
    }

    
    var x = toCenter ? layout.x() - layout.width() / 2 : layout.x();
    var y = toCenter ? layout.y() - layout.height() / 2 : layout.y();
    
    var textBoxList = IntStream.range(0, options.size()).mapToObj(i -> {
      var boxHeight = layout.height() / options.size();
      var boxY = y + (boxHeight * i);
      var zone = new SimpleLayout(x, boxY, layout.width(), boxHeight);
      return TextBox.create(options.get(i), g2d, Color.BLACK, Color.LIGHT_GRAY, options.get(i),
          false, zone, true, false);
    }).toList();

    return new Popup(layout, textBoxList);
  }

  // créer à partir
  // liste d'option renvoie l'index de l'option selectionner
  // dis si cliqué outside



  // Les texts sont clickables c'est ce que je renvoie dans create Batch!
  // onclick (pollEvent) // -> si outside renvoyer -1, sinon renvoyer le choix selectionné.
}
