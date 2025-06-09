package model;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TokenCollection {

  private final EnumMap<Token, Integer> tokens;

  public TokenCollection(Map<Token, Integer> initTokens) {
    Objects.requireNonNull(initTokens);
    tokens = new EnumMap<>(initTokens);
  }

  public static TokenCollection createEmpty(boolean gold) {
    var tokenMap = tokenValues(gold).stream().collect(Collectors.toMap(i -> i, _ -> 0));
    return new TokenCollection(tokenMap);
  }

  public static TokenCollection createFilled(int value, boolean gold) {
    if (value < 0) {
      throw new IllegalArgumentException("value must be positive");
    }
    var tokenMap = tokenValues(gold).stream().collect(Collectors.toMap(i -> i, _ -> value));
    return new TokenCollection(tokenMap);
  }

  /*
   * Token Collection Not Filled !
   */
  public static TokenCollection fromList(List<Token> initTokens) {
    Objects.requireNonNull(initTokens);
    EnumMap<Token, Integer> tokens = initTokens.stream().collect(
        Collectors.toMap(token -> token, _ -> 1, Integer::sum, () -> new EnumMap<>(Token.class)));
    return new TokenCollection(tokens);
  }

  private static List<Token> tokenValues(boolean gold) {
    return Arrays.stream(Token.values()).filter(t -> gold || t != Token.GOLD).toList();
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
      throw new IllegalArgumentException("jokerCount must be positive");
    }

    var sumDeficit = tokensToSub.tokens().entrySet().stream()
        .mapToInt(es -> tokens.get(es.getKey()) - es.getValue()).filter(es -> es < 0)
        .map(diff -> -diff).sum();

    var jokerTokenUsed = 0;
    if (sumDeficit > jokerCount) {
      throw new GameRuleException("You don't have enough tokens including jokers");
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
      throw new GameRuleException("You don't have enough tokens");
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
      throw new GameRuleException("You don't have enough tokens");
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
    return tokens.getOrDefault(t, 0);
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
