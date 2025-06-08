package controller;

import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BooleanSupplier;
import com.github.forax.zen.Application;
import model.Board;
import model.GameRuleException;
import model.Player;
import model.TokenCollection;
import model.utils.Phase;
import model.utils.Token;
import view.GUI;
import view.PlayerCancelException;
import view.TUI;
import view.View;

public record Controller(Phase phase, Board board, View view) {

	// Pattern matching quand y'aura GUI/TUI

	public static Controller controllerFactory(View view, Phase phase) {
		var playersList = view.promptPlayerNames();
		var cardPath = cardsPathByPhase(phase);
		var noblePath = Path.of("src", "data", "nobles_phase_2.csv");
		var board = Board.factory(playersList.size(), cardPath, noblePath, phase, List.copyOf(playersList));
		return new Controller(phase, board, view);
	}

	public void gameLoop() {
		var playerList = board.playerList();

		Optional<Player> winner;
		do {
			for (var player : playerList) {
				view.printTurn(player, board);
				repeatUntilTrue(() -> playTurn(player));
				repeatUntilTrue(() -> checkAfterTurn(player));
			}
			winner = board.winner();
		} while (winner.isEmpty());

		view.printWinner(winner.get().name());
	}

	// should be in a kind of util
	private void repeatUntilTrue(BooleanSupplier action) {
		while (!action.getAsBoolean()) {
		}
	}

	// TODO: Custom exception and catch only the custom one
	// Because we catch exception that should'nt be
	private boolean playTurn(Player player) {
		try {
			parseAndExecuteAction(player);
			return true;
		} catch (NumberFormatException e) {
			TUI.printWrongFormat();
			return false;
		} catch (PlayerCancelException p) {
			view.printTurn(player, board);
			return false;
		} catch (GameRuleException e) {
			view.printException(e);
			return false;
		}
	}

	private boolean checkAfterTurn(Player player) {
		try {
			if (player.hasExtraTokens()) {
				promptGiveBackExtraToken(player);
			}
			if (phase != Phase.ONE) {
				promptClaimNoble(player);
			}
			return true;
		} catch (GameRuleException e) {
			view.printException(e);
			return false;
		}
	}

	// TODO: Create custom exceptions (here and in the model) that will be readable
	// by the view
	// (either GUI or TUI)

	// TODO: handle phases in a better way
	private void parseAndExecuteAction(Player player) {
		// var parts = Arrays.asList(scanner.nextLine().split("\\s+"));
		// var actionChoosed = parts.get(0);

		var actionChoosed = view.promptPlayerAction();
		switch (actionChoosed) {
		case Action.BUY -> {
			var cardLevelIndex = view.promptCardLevelIndex();
			board.buyCard(player, cardLevelIndex);
		}
		// TAKE TOKEN
		case Action.TAKE -> {
			var tokens = view.promptTakeTokens();
			playerTakeToken(tokens, player);
		}
		// RESERVE CARD
		case Action.RESERVE -> {
			if (phase == Phase.ONE) {
				throw new NumberFormatException();
			}
			var cardLevelIndex = view.promptCardLevelIndexReserve();
			board.playerReserveCard(player, cardLevelIndex);

		}
		// BUY RESERVED CARD
		case Action.BUY_RESERVED -> {
			if (phase == Phase.ONE) {
				throw new NumberFormatException();
			}
			if (player.reservedCards().size() == 0) {
				throw new GameRuleException("You have no cards to reserve");
			}
			var reservedCardIndex = view.promptReservedCardIndex();
			board.buyReserved(player, reservedCardIndex);
		}
		}
		;
	}

	private void promptGiveBackExtraToken(Player player) {
		var extraTokenCount = player.extraTokensCount();
		var tokenList = view.promptGiveBackExtraTokens(player, extraTokenCount);
		board.playerGivesBackToken(player, TokenCollection.fromList(tokenList));
	}

	private void promptClaimNoble(Player player) {
		var nobles = board.noblesPlayerCanClaim(player);
		if (nobles.isEmpty()) {
			return;
		}

		var chosenNoble = nobles.get(0);
		if (nobles.size() > 1) {
			chosenNoble = view.promptPlayerToChooseNoble(nobles);

		}
		board.playerClaimNoble(player, chosenNoble);
	}

	// Should be separated and string part in tui, here should be token colleciton
	private void playerTakeToken(List<Token> tokenList, Player player) {
		switch (tokenList.size()) {
		case 2 -> board.playerTakeTwoTokens(player, tokenList);
		case 3 -> board.playerTakeThreeTokens(player, tokenList);
		default -> throw new NumberFormatException();
		}
	}

	private static Path cardsPathByPhase(Phase phase) {
		var fileName = switch (phase) {
		case Phase.ONE -> "cards_phase_1.csv";
		case Phase.TWO -> "cards_phase_2.csv";
		case Phase.THREE -> "cards_phase_2.csv";
		};
		return Path.of("src", "data", fileName);
	}

}
