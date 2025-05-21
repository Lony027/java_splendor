package model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Game {

  private final List<Player> playersList;

  public Game(List<Player> playersList) {
    Objects.requireNonNull(playersList);
    if (playersList.size() < 2 || playersList.size() > 4) {
      throw new IllegalArgumentException("Player list must contain from 2 to 4 players");
    }
    this.playersList = playersList;
  }

  public Optional<Player> hasWin() {
    var winners = playersList.stream().filter(p -> p.prestige() >= 15).toList();
    if (winners.isEmpty()) {
      return Optional.empty();
    }
    var maxPrestige = winners.stream().mapToInt(Player::prestige).max();
    winners = playersList.stream().filter(p -> p.prestige() == maxPrestige.getAsInt()).toList();
    if (winners.size() == 1) {
      return Optional.of(winners.get(0));
    } else {
      // What happens when there's a tie ?
      return winners.stream().min(Comparator.comparingInt(Player::playerCardsSize));
    }

    // return playersList.stream().filter(p -> p.prestige() >= 15)
    // .collect(Collectors.groupingBy(Player::prestige, Collectors.toList())).entrySet().stream()
    // .max(Map.Entry.comparingByKey()).map(Map.Entry::getValue)
    // .flatMap(winners -> winners.size() == 1 ? Optional.of(winners.get(0))
    // : winners.stream().min(Comparator.comparingInt(Player::playerCardsSize)));
  }
}
