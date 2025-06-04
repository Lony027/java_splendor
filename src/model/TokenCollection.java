package model;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import model.utils.Token;

public class TokenCollection {

  private final EnumMap<Token, Integer> tokens;

  public TokenCollection(Map<Token, Integer> initTokens) {
    Objects.requireNonNull(initTokens);
    tokens = new EnumMap<>(initTokens);
    for (var key : Token.values()) {
      tokens.putIfAbsent(key, 0);
    }
  }

  public static TokenCollection createEmpty() {
    var tokenMap = Arrays.stream(Token.values()).collect(Collectors.toMap(i -> i, _ -> 0));
    return new TokenCollection(tokenMap);
  }

  public static TokenCollection createFilled(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Value must be positive");
    }
    var tokenMap = Arrays.stream(Token.values()).collect(Collectors.toMap(i -> i, _ -> value));
    return new TokenCollection(tokenMap);
  }

  public static TokenCollection fromList(List<Token> initTokens) {
    Objects.requireNonNull(initTokens);
    var tokenCollection = createEmpty();
    for (var token : initTokens) {
      tokenCollection.add(token, 1);
    }
    return tokenCollection;
  }

  public void addCollection(TokenCollection tokensToAdd) {
    Objects.requireNonNull(tokensToAdd);
    tokensToAdd.tokens().forEach((key, value) -> tokens.merge(key, value, Integer::sum));
  }

  public void add(Token token, int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Value must be positive");
    }
    tokens.merge(token, value, Integer::sum);
  }

  public int subCollectionJoker(TokenCollection tokensToSub, int jokerCount) {
    Objects.requireNonNull(tokensToSub);
    if (jokerCount < 0) {
      throw new IllegalArgumentException("JokerCount must be positive");
    }

    var sumDeficit = tokensToSub.tokens().entrySet().stream()
        .mapToInt(es -> tokens.get(es.getKey()) - es.getValue()).filter(es -> es < 0)
        .map(diff -> -diff).sum();

    var jokerTokenUsed = 0;
    if (sumDeficit > jokerCount) {
      throw new IllegalArgumentException("Not enough tokens including jokers");
    }
    jokerTokenUsed = sumDeficit;

    tokensToSub.tokens().forEach((key, value) -> {
      tokens.merge(key, -value, Integer::sum);
    });
    tokens.replaceAll((_, value) -> Math.max(value, 0));

    return jokerTokenUsed;
  }

  public boolean canSubCollection(TokenCollection tokensToSub) {
    Objects.requireNonNull(tokensToSub);
    return !tokensToSub.tokens().entrySet().stream()
        .anyMatch(entrySet -> tokens.get(entrySet.getKey()) - entrySet.getValue() < 0);
  }

  public void subCollection(TokenCollection tokensToSub) {
    Objects.requireNonNull(tokensToSub);
    var isSubPossible = canSubCollection(tokensToSub);
    if (!isSubPossible) {
      throw new IllegalArgumentException("Not enough tokens");
    }

    tokensToSub.tokens().forEach((key, value) -> {
      tokens.merge(key, -value, Integer::sum);
    });
  }

  public void sub(Token token, int value) {
    if (value < 0) {
      throw new IllegalArgumentException("Value must be positive");
    }

    var newValue = tokens.get(token) - value;
    if (newValue < 0) {
      throw new IllegalArgumentException("Not enough tokens");
    }
    tokens.put(token, newValue);
  }

  public static TokenCollection discount(TokenCollection originalPrice, TokenCollection toSub) {
    Objects.requireNonNull(originalPrice);
    Objects.requireNonNull(toSub);

    var newMap = new EnumMap<>(originalPrice.tokens());

    toSub.tokens().forEach((key, toSubValue) -> {
      newMap.put(key, Math.max(newMap.get(key) - toSubValue, 0));
    });
    return new TokenCollection(newMap);
  }

  public Map<Token, Integer> tokens() {
    return Map.copyOf(tokens);
  }

  public int tokensByColor(Token t) {
    return tokens.get(t);
  }

  public int total() {
    return tokens.values().stream().mapToInt(Integer::intValue).sum();
  }

  @Override
  public String toString() {
    return tokens.entrySet().stream().map(entry -> entry.getKey().name() + " : " + entry.getValue())
        .collect(Collectors.joining(", "));
  }


}
