package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CardReader {

  public static HashMap<CardLevel, LinkedList<Card>> loadCards(Path path) throws IOException {
    var lines = Files.readAllLines(path);

    if (lines.isEmpty()) {
      throw new IllegalArgumentException(".csv is empty");
    }

    lines.removeFirst();

    var cards = new HashMap<CardLevel, LinkedList<Card>>();

    for (var line : lines) {
      String[] values = line.split(",");

      var cardLevel = CardLevel.valueOf(values[0]);
      var prestige = Integer.parseInt(values[1]);
      var tokenColor = Token.valueOf(values[2].toUpperCase());

      var white = Integer.parseInt(values[3]);
      var blue = Integer.parseInt(values[4]);
      var green = Integer.parseInt(values[5]);
      var red = Integer.parseInt(values[6]);
      var black = Integer.parseInt(values[7]);

      var prices = new TokenCollection(Map.of(Token.GREEN, green, Token.BLUE, blue, Token.RED, red,
          Token.WHITE, white, Token.BLACK, black));

      var levelStack = cards.getOrDefault(cardLevel, new LinkedList<Card>());
      var card = new Card(prestige, prices, tokenColor, cardLevel);

      levelStack.add(card);
      cards.put(cardLevel, levelStack);
    }

    return cards;
  }

  public static List<Noble> loadNobles(Path path) throws IOException {
    var lines = Files.readAllLines(path);

    if (lines.isEmpty()) {
      throw new IllegalArgumentException(".csv is empty");
    }

    lines.removeFirst();

    var nobles = new ArrayList<Noble>();

    for (var line : lines) {
      String[] values = line.split(",");

      var prestige = Integer.parseInt(values[0]);

      var white = Integer.parseInt(values[1]);
      var blue = Integer.parseInt(values[2]);
      var green = Integer.parseInt(values[3]);
      var red = Integer.parseInt(values[4]);
      var black = Integer.parseInt(values[5]);
      
      var name = values[6];

      var prices = new TokenCollection(Map.of(Token.GREEN, green, Token.BLUE, blue, Token.RED, red,
          Token.WHITE, white, Token.BLACK, black));

      nobles.add(new Noble(prestige, prices, name));
    }

    return nobles;
  }
}
