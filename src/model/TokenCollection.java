package model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import model.utils.Token;

public class TokenCollection {

  private final EnumMap<Token, Integer> tokens;

  public TokenCollection() {
    tokens = new EnumMap<>(
        Map.of(Token.GREEN, 0, Token.BLUE, 0, Token.RED, 0, Token.WHITE, 0, Token.BLACK, 0));
  }

  public TokenCollection(Map<Token, Integer> initTokens) {
    Objects.requireNonNull(initTokens);
    tokens = new EnumMap<>(initTokens);
    for (var key : Token.values()) {
      tokens.putIfAbsent(key, 0);
    }
  }
  
  public TokenCollection(List<Token> initTokens) {
    Objects.requireNonNull(initTokens);
    // @to-do: Stream version or at least find a more elegant way
    var tokensToCreate = new EnumMap<>(
        Map.of(Token.GREEN, 0, Token.BLUE, 0, Token.RED, 0, Token.WHITE, 0, Token.BLACK, 0));
    for (var token : initTokens) {
      var val = tokensToCreate.getOrDefault(token, 0);
      tokensToCreate.put(token, val+1);
    }
    tokens = tokensToCreate;
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
  
  public static TokenCollection discount(TokenCollection originalPrice, TokenCollection toSub) {
      Objects.requireNonNull(originalPrice);
      Objects.requireNonNull(toSub);

      var newMap = new EnumMap<>(originalPrice.tokens());
      
      toSub.tokens().forEach((key, toSubValue) -> {
        newMap.put(key, Math.max(newMap.get(key) - toSubValue, 0));
      });
      return new TokenCollection(newMap);
  }

  public void subCollection(TokenCollection tokensToSub) {
    Objects.requireNonNull(tokensToSub);
    
    // Soutenance : Is this a good solution ? Immutability ?
    var isNotPossible = tokensToSub.tokens().entrySet().stream()
        .anyMatch(entrySet -> tokens.get(entrySet.getKey()) - entrySet.getValue() < 0);
    if (isNotPossible) {
      throw new IllegalArgumentException("Not enough tokens");
    }
    
    // Soutenance : Choose this over for (:) ?
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

  public static TokenCollection createFilledTokenCollection(int value) {
    return new TokenCollection(Map.of(Token.GREEN, value, Token.BLUE, value, Token.RED, value,
        Token.WHITE, value, Token.BLACK, value));
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
