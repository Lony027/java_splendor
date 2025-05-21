package model.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Map;
import model.Card;
import model.TokenCollection;

public class CardReader {

  public static LinkedList<Card> loadCards(Path path) throws IOException {
    var lines = Files.readAllLines(path);

    if (lines.isEmpty()) {
      throw new IllegalArgumentException("cards.csv is empty");
    }

    lines.removeFirst(); 

    var cards = new LinkedList<Card>();
    for (var line : lines) {
      String[] values = line.split(",");

      int prestige = Integer.parseInt(values[0]);
      Token tokenColor = Token.valueOf(values[1].toUpperCase());

      int white = Integer.parseInt(values[2]);
      int blue = Integer.parseInt(values[3]);
      int green = Integer.parseInt(values[4]);
      int red = Integer.parseInt(values[5]);
      int black = Integer.parseInt(values[6]);

      var prices = new TokenCollection(Map.of(Token.GREEN, green, Token.BLUE, blue, Token.RED, red,
          Token.WHITE, white, Token.BLACK, black));

      cards.add(new Card(prestige, prices, tokenColor));
    }

    return cards;
  }
}
