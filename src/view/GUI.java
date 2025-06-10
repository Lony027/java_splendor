package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import controller.Action;
import model.Board;
import model.CardLevel;
import model.Noble;
import model.Phase;
import model.Player;
import model.Token;
import view.elements.Box;
import view.elements.CardElement;
import view.elements.CardStack;
import view.elements.Element;
import view.elements.NobleElement;
import view.elements.PlayerElement;
import view.elements.Popup;
import view.elements.Text;
import view.elements.TextBox;
import view.elements.TokenElement;
import view.layout.BatchLayout;
import view.layout.ScreenScaling;
import view.layout.SimpleLayout;

public final class GUI implements View {

  private final ApplicationContext context;
  private final List<Element> clickables;
  private final Phase phase;

  private Consumer<Graphics2D> lastTurn;

  private ScreenScaling screenScaling;

  private SimpleLayout infoboxLayout;
  private SimpleLayout sidebarLayout;
  private SimpleLayout reservedLayout;
  private SimpleLayout playerNoblesLayout;
  private SimpleLayout popupLayout;
  private SimpleLayout fullLayout;

  private BatchLayout cardStackLayout;
  private BatchLayout cardLayout;
  private BatchLayout bankLayout;
  private BatchLayout nobleLayout;
  private BatchLayout reservedCardLayout;
  private BatchLayout playerInfoLayout;
  private BatchLayout playerNoblesCardLayout;
  private BatchLayout tokenCollectionBackLayout;
  private BatchLayout chooseNobleLayout;

  public GUI(ApplicationContext context, Phase phase) {
    Objects.requireNonNull(context);
    this.context = context;
    this.phase = phase;
    clickables = new ArrayList<>();
    assignLayout();
  }

  private void render(Consumer<Graphics2D> toRender) {
    context.renderFrame(g2d -> {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      if (screenScaling.scalingChanged()) {
        screenScaling = ScreenScaling.create(context);
        assignLayout();
      }
      toRender.accept(g2d);
    });
  }

  private void assignLayout() {
    screenScaling = ScreenScaling.create(context);
    cardStackLayout = screenScaling.createCardStackLayout();
    cardLayout = screenScaling.createCardLayout();
    bankLayout = screenScaling.createBankLayout();
    infoboxLayout = screenScaling.createInfoBoxLayout();
    sidebarLayout = screenScaling.createSidebarLayout();
    playerInfoLayout = screenScaling.createPlayerInfoLayout();
    popupLayout = screenScaling.createPopupLayout();
    fullLayout = screenScaling.createFullLayout();
    tokenCollectionBackLayout = screenScaling.createTokenCollectionBackLayout();

    if (phase == Phase.COMPLETE) {
      nobleLayout = screenScaling.createNobleLayout();
      reservedLayout = screenScaling.createReservedLayout();
      playerNoblesLayout = screenScaling.createPlayerNoblesLayout();
      reservedCardLayout = screenScaling.createReservedCardsLayout();
      playerNoblesCardLayout = screenScaling.createPlayerNoblesCardLayout();
      chooseNobleLayout = screenScaling.createChooseNobleLayout();
    }
  }

  /*
   * INTERFACE
   */

  @Override
  public Action promptPlayerAction() {
    fillInfoBox("Press space to choose an action");
    repeatUntilTrue(() -> waitForKeyPress(), key -> key == KeyboardEvent.Key.SPACE);
    var messages = new ArrayList<>(List.of("Buy a card", "Take tokens"));
    if (phase == Phase.COMPLETE) {
      messages.add("Reserve a card");
      messages.add("Buy a reserved card");
    }
    Consumer<Graphics2D> frame = (g2d -> {
      var popup = Popup.create(g2d, popupLayout, messages, true);
      clickables.addAll(popup.options());
    });
    render(frame);
    var chosenAction = repeatUntilTrue(() -> {
      var textElement = waitForClickedElement();
      return switch (textElement) {
        case TextBox textEl -> {
          if (messages.contains(textEl.id())) {
            yield textEl.id();
          }
          yield null;
        }
        default -> null;
      };
    }, message -> message != null);
    render(lastTurn);
    return fromPlayerAction(chosenAction);
  }

  @Override
  public Map.Entry<CardLevel, Integer> promptCardLevelIndex() {
    fillInfoBox("BUY A CARD : Choose a card, click anywhere else to cancel");
    var element = waitForClickedElement();
    var cardElement = isCardOrCancel(element);
    if (cardElement.isReserved()) {
      throw new PlayerCancelException();
    }
    return new AbstractMap.SimpleEntry<>(cardElement.card().level(), cardElement.cardIndex());

  }

  @Override
  public Entry<CardLevel, Integer> promptCardLevelIndexReserve() {
    fillInfoBox("RESERVE A CARD : Choose a card, click anywhere else to cancel");

    var element = waitForClickedElement();
    return switch (element) {
      case CardElement card -> {
        if (card.isReserved()) {
          throw new PlayerCancelException();
        }
        yield new AbstractMap.SimpleEntry<>(card.card().level(), card.cardIndex());
      }
      case CardStack cardStack -> new AbstractMap.SimpleEntry<>(cardStack.cardLevel(), -1);
      default -> throw new PlayerCancelException();
    };
  }


  @Override
  public int promptReservedCardIndex() {
    fillInfoBox("BUY A RESERVED CARD : Choose a card, click anywhere else to cancel");
    var element = waitForClickedElement();
    var cardElement = isCardOrCancel(element);
    if (!cardElement.isReserved()) {
      throw new PlayerCancelException();
    }
    return cardElement.cardIndex();
  }

  @Override
  public List<Token> promptTakeTokens() {
    fillInfoBox("TAKE TOKEN : 2 same or 3 different, click anywhere else to cancel");
    return collectToken(
        (tokenList -> !(tokenList.stream().distinct().count() == 1 && tokenList.size() == 2)
            && tokenList.size() < 3));
  }

  @Override
  public List<String> promptPlayerNames() {
    displayPlayerNumber();
    var playerCountString = repeatUntilTrue(() -> {
      var element = waitForClickedElement();
      return switch (element) {
        case TextBox t -> t.id();
        default -> "";
      };
    }, key -> key != null);
    var playerCount = Integer.parseInt(playerCountString);

    clickables.clear();
    displayMaxCharacters();
    var playerList = typePlayerList(playerCount);
    clickables.clear();
    return playerList;
  }

  @Override
  public List<Token> promptGiveBackExtraTokens(Player player, int extra) {
    Objects.requireNonNull(player);
    if (extra < 1) {
      throw new IllegalArgumentException("extra must be more than 0");
    }

    clickables.clear();
    Consumer<Graphics2D> frame = (g2d -> {
      Box.create(g2d, fullLayout, true, Color.DARK_GRAY);
      Text.create(g2d, Color.LIGHT_GRAY, "Give back " + extra + " token(s)",
          screenScaling.offsetX(50), screenScaling.offsetY(25), true, 22, false);
      clickables.addAll(TokenElement.createBatch(g2d, player.tokens(), tokenCollectionBackLayout,
          false, true, true));
    });
    render(frame);
    return collectToken((tokenList -> tokenList.size() < extra));
  }

  @Override
  public Noble promptPlayerToChooseNoble(List<Noble> nobles) {
    Objects.requireNonNull(nobles);

    clickables.clear();
    Consumer<Graphics2D> frame = (g2d -> {
      Box.create(g2d, fullLayout, true, Color.DARK_GRAY);
      Text.create(g2d, Color.LIGHT_GRAY, "Choose a noble", screenScaling.offsetX(50),
          screenScaling.offsetY(25), true, 22, false);
      clickables.addAll(NobleElement.createBatch(g2d, nobles, chooseNobleLayout));
    });
    render(frame);

    return repeatUntilTrue(() -> {
      var element = waitForClickedElement();
      return switch (element) {
        case NobleElement nobleEl -> nobleEl.noble();
        default -> null;
      };
    }, key -> key != null);
  }

  @Override
  public void printWinner(String playerName) {
    Objects.requireNonNull(playerName);

    Consumer<Graphics2D> frame = (g2d -> {
      TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY,
          playerName + " has win ! (Press any key to leave)", false, fullLayout, true, false);
    });
    render(frame);
    waitForKeyPress();
  }

  @Override
  public void printException(Exception e) {
    Objects.requireNonNull(e);

    Consumer<Graphics2D> frame = (g2d -> {
      Popup.create(g2d, popupLayout, List.of(e.getMessage(), "Press space to leave"), true);
    });
    render(frame);
    repeatUntilTrue(() -> waitForKeyPress(), key -> key == KeyboardEvent.Key.SPACE);
    render(lastTurn);
  }

  @Override
  public void printTurn(Player player, Board board) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(board);

    clickables.clear();
    Consumer<Graphics2D> toRender = (g2d -> {
      loadImage("background.png", g2d);

      clickables.addAll(CardStack.createBatch(g2d, board.cardDecksSizes(), cardStackLayout));
      clickables.addAll(CardElement.createLevelBatch(g2d, board.cards(), cardLayout));
      clickables.addAll(
          TokenElement.createBatch(g2d, board.tokenBank(), bankLayout, false, false, false));
      Box.create(g2d, sidebarLayout, false, Element.DARK);
      PlayerElement.createBatch(g2d, board.playerList(), player, playerInfoLayout);

      if (phase == Phase.COMPLETE) {
        clickables.addAll(NobleElement.createBatch(g2d, board.nobles(), nobleLayout));

        Box.create(g2d, reservedLayout, false, Element.DARK);
        clickables.addAll(
            CardElement.createBatchReserved(g2d, player.reservedCards(), reservedCardLayout));

        Box.create(g2d, playerNoblesLayout, false, Element.DARK);
        NobleElement.createBatch(g2d, player.playerNobles(), playerNoblesCardLayout);
      }
    });
    lastTurn = toRender;
    render(toRender);
  }

  /*
   * HELPER
   */

  private Action fromPlayerAction(String playerAction) {
    return switch (playerAction) {
      case "Buy a card" -> Action.BUY;
      case "Take tokens" -> Action.TAKE;
      case "Reserve a card" -> Action.RESERVE;
      case "Buy a reserved card" -> Action.BUY_RESERVED;
      default -> throw new IllegalArgumentException(
          "playerAction : \"" + playerAction + "\" unsupported");
    };
  }

  private void loadImage(String imageName, Graphics2D g2d) {
    try {
      var input = GUI.class.getResourceAsStream("/images/" + imageName);
      var image = ImageIO.read(input);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.drawImage(image, 0, 0, screenScaling.width(), screenScaling.height(), null);
    } catch (IOException e) {
      throw new IllegalArgumentException(imageName + " image not found");
    }
  }

  private CardElement isCardOrCancel(Element element) {
    return switch (element) {
      case CardElement cardElement -> cardElement;
      default -> throw new PlayerCancelException();
    };
  }

  private TokenElement isTokenOrCancel(Element element) {
    return switch (element) {
      case TokenElement tokenElement -> tokenElement;
      default -> throw new PlayerCancelException();
    };
  }

  private List<Token> collectToken(Predicate<List<Token>> pred) {
    var tokenList = new ArrayList<Token>();
    do {
      var element = waitForClickedElement();
      var tokenEl = isTokenOrCancel(element);
      var tokenTook = (int) tokenList.stream().filter(t -> tokenEl.token() == t).count() + 1;
      if (tokenEl.tokenCount() - tokenTook >= 0) {
        Consumer<Graphics2D> frame = (g2d -> {
          TokenElement.create(g2d, tokenEl.token(), tokenEl.tokenCount() - tokenTook, false,
              tokenEl.layout());
        });
        render(frame);
        tokenList.add(tokenEl.token());
      }
    } while (pred.test(tokenList));
    return tokenList;
  }

  private void displayPlayerNumber() {
    Consumer<Graphics2D> frame = (g2d -> {
      loadImage("menu.png", g2d);

      var menuLayout = new SimpleLayout(screenScaling.offsetX(50), screenScaling.offsetY(40),
          screenScaling.scaledWidth(35), screenScaling.scaledHeight(8));
      TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, "Choose how many player will play",
          false, menuLayout, true, true);

      var choosePlayerNumber = new BatchLayout(screenScaling.offsetX(50), screenScaling.offsetY(55),
          screenScaling.scaledWidth(20), screenScaling.scaledHeight(10), 0,
          screenScaling.scaledHeight(12));

      var playerTextBoxes = IntStream.range(2, 5).mapToObj(i -> i + " players").toList();
      var textBoxes = TextBox.createBatch(g2d, playerTextBoxes, choosePlayerNumber,
          (i -> Integer.toString(i + 2)), Color.LIGHT_GRAY, true, true, true);

      clickables.addAll(textBoxes);
    });
    render(frame);
  }

  private void displayMaxCharacters() {
    Consumer<Graphics2D> frame = (g2d -> {
      loadImage("menu.png", g2d);

      Text.create(g2d, Color.WHITE, "Max. 10 characters, press space to confirm",
          screenScaling.offsetX(50), screenScaling.offsetY(60), true, 22, true);
    });
    render(frame);
  }

  private void displayTypePlayerN(int n, SimpleLayout menuLayout) {
    Consumer<Graphics2D> frame = (g2d -> {
      TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, "Type player " + n + " name.", false,
          menuLayout, true, true);
    });
    render(frame);
  }

  private List<String> typePlayerList(int playerCount) {
    Consumer<Graphics2D> frame;
    var playerList = new ArrayList<String>();
    var menuLayout = new SimpleLayout(screenScaling.offsetX(50), screenScaling.offsetY(40),
        screenScaling.scaledWidth(35), screenScaling.scaledHeight(8));
    var typingLayout = new SimpleLayout(screenScaling.offsetX(50), screenScaling.offsetY(54),
        screenScaling.scaledWidth(15), screenScaling.scaledHeight(7));

    for (int i = 0; i < playerCount; i++) {
      displayTypePlayerN(i + 1, menuLayout);
      boolean isTyping = true;
      var typed = new StringBuilder();
      while (isTyping) {
        frame = (g2d -> {
          TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, typed.toString(), true,
              typingLayout, true, true);
        });
        render(frame);
        var keyPressed = waitForKeyPress();
        if (keyPressed.equals(KeyboardEvent.Key.SPACE)) {
          if (!typed.isEmpty()) {
            playerList.add(typed.toString());
            isTyping = false;
          }
        } else if (isAlphabet(keyPressed) && typed.length() < 10) {
          typed.append(keyPressed);
        }
      }
    }
    return playerList;
  }

  private void fillInfoBox(String s) {
    Consumer<Graphics2D> frame = (g2d -> {
      TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, s, false, infoboxLayout, true, false);
    });
    render(frame);
  }

  /*
   * UTILS
   */

  private Element waitForClickedElement() {
    return repeatUntilTrue(() -> {
      var event = context.pollOrWaitEvent(1000);
      if (event == null) {
        return null;
      }
      return switch (event) {
        case PointerEvent pointerEvent -> {
          if (pointerEvent.action() != PointerEvent.Action.POINTER_UP) {
            yield null;
          }
          yield elementClicked(pointerEvent.location().x(), pointerEvent.location().y())
              .orElse(null);
        }
        default -> null;
      };
    }, key -> key != null);
  }

  private KeyboardEvent.Key waitForKeyPress() {
    return repeatUntilTrue(() -> {
      var event = context.pollOrWaitEvent(1000);
      if (event == null) {
        return null;
      }
      return switch (event) {
        case KeyboardEvent keyboardEvent -> {
          if (keyboardEvent.action() != KeyboardEvent.Action.KEY_PRESSED) {
            yield null;
          }
          yield keyboardEvent.key();
        }
        default -> null;
      };
    }, key -> key != null);
  }

  private static boolean isAlphabet(KeyboardEvent.Key key) {
    return switch (key) {
      case KeyboardEvent.Key.A, KeyboardEvent.Key.B, KeyboardEvent.Key.C, KeyboardEvent.Key.D, KeyboardEvent.Key.E, KeyboardEvent.Key.F, KeyboardEvent.Key.G, KeyboardEvent.Key.H, KeyboardEvent.Key.I, KeyboardEvent.Key.J, KeyboardEvent.Key.K, KeyboardEvent.Key.L, KeyboardEvent.Key.M, KeyboardEvent.Key.N, KeyboardEvent.Key.O, KeyboardEvent.Key.P, KeyboardEvent.Key.Q, KeyboardEvent.Key.R, KeyboardEvent.Key.S, KeyboardEvent.Key.T, KeyboardEvent.Key.U, KeyboardEvent.Key.V, KeyboardEvent.Key.W, KeyboardEvent.Key.X, KeyboardEvent.Key.Y, KeyboardEvent.Key.Z, KeyboardEvent.Key.SPACE -> true;
      default -> false;
    };
  }

  private Optional<Element> elementClicked(int clickedX, int clickedY) {
    return clickables.stream().filter(e -> e.layout().isClicked(clickedX, clickedY)).findFirst();
  }

  private static <T> T repeatUntilTrue(Supplier<T> action, Predicate<T> condition) {
    T result;
    do {
      result = action.get();
    } while (!condition.test(result));
    return result;
  }

}
