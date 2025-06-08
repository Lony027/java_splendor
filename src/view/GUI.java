package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import controller.Action;
import model.Board;
import model.Card;
import model.Noble;
import model.Player;
import model.TokenCollection;
import model.utils.CardLevel;
import model.utils.Token;
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
import view.elements.Tooltip;
import view.layout.BatchLayout;
import view.layout.LayoutMetrics;
import view.layout.SimpleLayout;

public final class GUI implements View {

  private final ApplicationContext context;
  private final List<Element> clickables;

  // TODO REMOVE TOOL TIP
  // TODO REPLACE render(lastTurn) PAR JUSTE UN APPEL DEPUIS LE CONTROLLEUR ET
  // PEUX ETRE CANCEL
  // EXCEPTION

  // Not final as windows is resizeable!
  private LayoutMetrics layout;

  // vraiment bizzare
  private Consumer<Graphics2D> lastTurn;

  public GUI(ApplicationContext context) {
    this.context = context;
    clickables = new ArrayList<>();
    layout = LayoutMetrics.create(context);
  }

  // List<Consumer<Graphics2D>> toRender
  private void render(Consumer<Graphics2D> toRender) {
    context.renderFrame(g2d -> {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      if (layout.scalingChanged()) {
        layout = LayoutMetrics.create(context);
      }
      // toRender.forEach(consumer -> consumer.accept(g2d));
      toRender.accept(g2d);
    });
  }

  // break into functions
  @Override
  public void printTurn(Player player, Board board) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(board);

    clickables.clear();
    Consumer<Graphics2D> toRender = (g2d -> {
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.drawImage(loadImage("background.png"), 0, 0, layout.width(), layout.height(), null);

      // var message = player.name() + " turn!";
      // TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, message, false,
      // layout.infobox(),
      // true, false);

      clickables.clear();
      var cardStackList = CardStack.createBatch(g2d, board.cardDecksSizes(), layout.cardStack());
      var cardsElementList = CardElement.createLevelBatch(g2d, board.cards(), layout.card());
      var bankList =
          TokenElement.createBatch(g2d, board.tokenBank(), layout.bank(), false, false, false);
      var nobleList = NobleElement.createBatch(g2d, board.nobles(), layout.noble());

      clickables.addAll(bankList);
      clickables.addAll(cardsElementList);
      clickables.addAll(cardStackList);
      clickables.addAll(nobleList);

      Box.create(g2d, layout.reserved(), false, Element.DARK);
      var reservedCards =
          CardElement.createBatchReserved(g2d, player.reservedCards(), layout.reservedCardLayout());
      clickables.addAll(reservedCards);

      Box.create(g2d, layout.playerNobles(), false, Element.DARK);
      // to center, bellek créer un record!
      var tmpLayout = new BatchLayout(layout.playerNobles().x(), layout.playerNobles().y(), layout.noble().width(), layout.noble().y(), layout.noble().width()+50, 0);
      NobleElement.createBatch(g2d, player.playerNobles(),tmpLayout);
      // TODO: Noble and reserved cards

      Box.create(g2d, layout.sidebar(), false, Element.DARK);
      PlayerElement.createBatch(g2d, board.playerList(), player, layout.playerInfoLayout());

    });
    lastTurn = toRender;
    render(toRender);
  }

  private BufferedImage loadImage(String imageName) {
    try (var input = GUI.class.getResourceAsStream("/images/" + imageName)) {
      return ImageIO.read(input);
    } catch (IOException e) {
      throw new IllegalArgumentException(imageName + " image not found");
    }
  }

  // Should be in a kind of util
  private static <T> T repeatUntilTrue(Supplier<T> action, Predicate<T> condition) {
    T result;
    do {
      result = action.get();
    } while (!condition.test(result));
    return result;
  }

  public String popup(List<String> messages) {

    var textElement = waitForClickedElement();
    return switch (textElement) {
      case TextBox textEl -> {
        if (messages.contains(textEl.id())) {
          yield textEl.id();
        }
        yield "";
      }
      default -> "";
    };
  }

  @Override
  public Action promptPlayerAction() {
    fillInfoBox("Press space to choose an action");
    repeatUntilTrue(() -> waitForKeyPress(), key -> "SPACE".equals(key));
    var messages = List.of("BUY", "TAKE", "RESERVE", "BUY_RESERVED");
    Consumer<Graphics2D> frame = (g2d -> {
      // TODO put layout in layout metrics !!!
      var tmp = new SimpleLayout(layout.xPct(50), layout.yPct(50), layout.widthPct(50),
          layout.heightPct(50));
      var popup = Popup.create(g2d, tmp, messages, true);
      clickables.addAll(popup.options());
    });
    render(frame);
    var chosenAction = repeatUntilTrue(() -> popup(messages), message -> !message.isEmpty());
    // clickables.clear();
    render(lastTurn);
    return Action.valueOf(chosenAction);
  }

  @Override
  public List<Token> promptTakeTokens() {
    fillInfoBox("Pick tokens, click anywhere else to cancel");
    var tokenList = new ArrayList<Token>();

    do {
      var element = waitForClickedElement();
      switch (element) {
        case TokenElement tokenEl -> {
          var tokenTook = (int) tokenList.stream().filter(t -> tokenEl.token() == t).count() + 1;
          if (tokenEl.tokenCount() - tokenTook < 0) {
            fillInfoBox("You cannot pick empty token");
          } else {
            Consumer<Graphics2D> frame = (g2d -> {
              TokenElement.create(g2d, tokenEl.token(), tokenEl.tokenCount() - tokenTook, false,
                  tokenEl.layout());
            });
            render(frame);
            tokenList.add(tokenEl.token());
          }
        }
        default -> {
          throw new PlayerCancelException();
        }
      }
    } while (!(tokenList.stream().distinct().count() == 1 && tokenList.size() == 2)
        && tokenList.size() < 3);
    return tokenList;
  }

  @Override
  public List<Player> promptPlayerNames() {

    var menuLayout = new SimpleLayout(layout.xPct(50), layout.yPct(40), layout.widthPct(35),
        layout.heightPct(8));
    Consumer<Graphics2D> frame = (g2d -> {
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.drawImage(loadImage("menu.png"), 0, 0, layout.width(), layout.height(), null);

      TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, "Choose how many player will play",
          false, menuLayout, true, true);

      var choosePlayerNumber = new BatchLayout(layout.xPct(50), layout.yPct(55),
          layout.widthPct(20), layout.heightPct(10), 0, layout.heightPct(12));
      var playerTextBoxes = IntStream.range(2, 5).mapToObj(i -> i + " players").toList();
      var textBoxes = TextBox.createBatch(g2d, playerTextBoxes, choosePlayerNumber,
          (i -> Integer.toString(i + 2)), Color.LIGHT_GRAY, true, true, true);

      clickables.addAll(textBoxes);
    });
    render(frame);

    String id = "";
    while (id == "") {
      var element = waitForClickedElement();
      id = switch (element) {
        case TextBox t -> t.id();
        default -> "";
      };
    }
    var playerCount = Integer.parseInt(id);
    clickables.clear();

    frame = (g2d -> {
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.drawImage(loadImage("menu.png"), 0, 0, layout.width(), layout.height(), null);

      Text.create("", g2d, Color.WHITE, "Max. 10 characters, press space to confirm",
          layout.xPct(50), layout.yPct(60), true, 22, true);
    });
    render(frame);

    var playerList = new ArrayList<Player>();
    var typingLayout = new SimpleLayout(layout.xPct(50), layout.yPct(54), layout.widthPct(15),
        layout.heightPct(7));
    for (int i = 0; i < playerCount; i++) {
      var index = i + 1;
      frame = (g2d -> {
        TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, "Type player " + index + " name.",
            false, menuLayout, true, true);
      });
      render(frame);

      boolean isTyping = true;
      var typed = new StringBuilder();
      while (isTyping) {
        frame = (g2d -> {
          TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, typed.toString(), true,
              typingLayout, true, true);
        });
        render(frame);
        var keyPressed = waitForKeyPress();
        // Limit string size!
        // verif string vide
        // c un enum normalement les string c un peu dégueu
        if (keyPressed.equals("SPACE")) {
          if (!typed.isEmpty()) {
            playerList.add(new Player(typed.toString()));
            isTyping = false;
          }
        } else if (typed.length() < 10) {
          typed.append(keyPressed);
        }
      }

    }
    clickables.clear();
    return playerList;
  }

  private Optional<Element> elementClicked(int clickedX, int clickedY) {
    return clickables.stream().filter(e -> e.layout().isClicked(clickedX, clickedY)).findFirst();
  }

  private static boolean isAlphabet(KeyboardEvent.Key key) {
    return switch (key) {
      case KeyboardEvent.Key.A, KeyboardEvent.Key.B, KeyboardEvent.Key.C, KeyboardEvent.Key.D, KeyboardEvent.Key.E, KeyboardEvent.Key.F, KeyboardEvent.Key.G, KeyboardEvent.Key.H, KeyboardEvent.Key.I, KeyboardEvent.Key.J, KeyboardEvent.Key.K, KeyboardEvent.Key.L, KeyboardEvent.Key.M, KeyboardEvent.Key.N, KeyboardEvent.Key.O, KeyboardEvent.Key.P, KeyboardEvent.Key.Q, KeyboardEvent.Key.R, KeyboardEvent.Key.S, KeyboardEvent.Key.T, KeyboardEvent.Key.U, KeyboardEvent.Key.V, KeyboardEvent.Key.W, KeyboardEvent.Key.X, KeyboardEvent.Key.Y, KeyboardEvent.Key.Z, KeyboardEvent.Key.SPACE -> true;
      default -> false;
    };
  }

  private String waitForKeyPressedHelper() {
    var event = context.pollOrWaitEvent(1000);
    if (event == null) {
      return "";
    }

    return switch (event) {
      case PointerEvent _ -> {
        yield "";
      }
      case KeyboardEvent keyboardEvent -> {
        if (keyboardEvent.action() != KeyboardEvent.Action.KEY_PRESSED) {
          yield "";
        }
        if (!isAlphabet(keyboardEvent.key())) {
          yield "";
        }
        yield keyboardEvent.key().toString();
      }
    };
  }

  private String waitForKeyPress() {
    var key = "";
    while (key.isEmpty()) {
      key = waitForKeyPressedHelper();
    }
    return key;
  }

  private Element waitForClickedElementHelper() {
    var event = context.pollOrWaitEvent(1000);
    if (event == null) {
      return null;
    }
    return switch (event) {
      case PointerEvent pointerEvent -> {
        if (pointerEvent.action() != PointerEvent.Action.POINTER_UP) {
          yield null;
        }
        yield elementClicked(pointerEvent.location().x(), pointerEvent.location().y()).orElse(null);
      }
      default -> null;
    };
  }

  private Element waitForClickedElement() {
    return repeatUntilTrue(() -> waitForClickedElementHelper(), key -> key != null);
  }

  @Override
  public Map.Entry<CardLevel, Integer> promptCardLevelIndex() {
    fillInfoBox("BUY A CARD : Choose a card, click anywhere else to cancel");

    var element = waitForClickedElement();
    return switch (element) {
      case CardElement card -> {
        if (card.isReserved()) {
          throw new PlayerCancelException();
        }
        yield new AbstractMap.SimpleEntry<>(card.card().level(), card.cardIndex());
      }
      default -> throw new PlayerCancelException();
    };
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
    return switch (element) {
      case CardElement card -> {
        if (!card.isReserved()) {
          throw new PlayerCancelException();
        }
        yield card.cardIndex();
      }
      default -> throw new PlayerCancelException();
    };
  }

  @Override
  public void printWinner(String playerName) {
    Consumer<Graphics2D> frame = (g2d -> {
      var fullLayout = new SimpleLayout(layout.xPct(0), layout.yPct(0), layout.widthPct(100),
          layout.heightPct(100));
      TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, playerName + " has win !", false,
          fullLayout, true, false);
    });
    render(frame);
  }

  @Override
  public void printException(Exception e) {
    Consumer<Graphics2D> frame = (g2d -> {
      // TODO put layout in layout metrics !!!
      var tmp = new SimpleLayout(layout.xPct(50), layout.yPct(50), layout.widthPct(50),
          layout.heightPct(50));
      Popup.create(g2d, tmp, List.of(e.getMessage(), "Press space to leave"), true);
    });
    render(frame);
    repeatUntilTrue(() -> waitForKeyPress(), key -> "SPACE".equals(key));
    if (lastTurn != null) {
      render(lastTurn);
    }
  }

  private void fillInfoBox(String s) {
    Consumer<Graphics2D> frame = (g2d -> {
      TextBox.create("", g2d, Element.DARK, Color.LIGHT_GRAY, s, false, layout.infobox(), true,
          false);
    });
    render(frame);
  }

  @Override
  public List<Token> promptGiveBackExtraTokens(Player player, int extra) {
    Consumer<Graphics2D> frame = (g2d -> {
      clickables.clear();

      var fullLayout = new SimpleLayout(layout.xPct(15), layout.yPct(15), layout.widthPct(75),
          layout.heightPct(75));
      Box.create(g2d, fullLayout, true, Color.DARK_GRAY);


      Text.create("", g2d, Color.LIGHT_GRAY, "Give back " + extra + " token(s)", layout.xPct(50),
          layout.yPct(25), true, 22, false);

      var tokenCollectionLayout = new BatchLayout(layout.xPct(25), layout.yPct(47),
          layout.widthPct(8), layout.heightPct(20), layout.widthPct(9), 0);
      clickables.addAll(TokenElement.createBatch(g2d, TokenCollection.createFilled(3),
          tokenCollectionLayout, false, true, true)); // player.tokens()
    });
    render(frame);

    var tokenList = new ArrayList<Token>();
    do {
      var element = waitForClickedElement();
      switch (element) {
        case TokenElement tokenEl -> {
          var tokenTook = (int) tokenList.stream().filter(t -> tokenEl.token() == t).count() + 1;
          if (tokenEl.tokenCount() - tokenTook >= 0) {
            frame = (g2d -> {
              TokenElement.create(g2d, tokenEl.token(), tokenEl.tokenCount() - tokenTook, false,
                  tokenEl.layout());
            });
            render(frame);
            tokenList.add(tokenEl.token());
          }
        }
        default -> {}
      }
    } while (tokenList.size() < extra);
    return tokenList;
  }

  @Override
  public Noble promptPlayerToChooseNoble(List<Noble> nobles) {
    Consumer<Graphics2D> frame = (g2d -> {
      clickables.clear();

      var fullLayout = new SimpleLayout(layout.xPct(15), layout.yPct(15), layout.widthPct(75),
          layout.heightPct(75));
      Box.create(g2d, fullLayout, true, Color.DARK_GRAY);


      Text.create("", g2d, Color.LIGHT_GRAY, "Choose a noble", layout.xPct(50),
          layout.yPct(25), true, 22, false);

      var nobleCollectionLayout = new BatchLayout(layout.xPct(25), layout.yPct(47),
          layout.widthPct(8), layout.heightPct(20), layout.widthPct(9), 0);
      clickables.addAll(NobleElement.createBatch(g2d, nobles,
          nobleCollectionLayout)); // player.tokens()
    });
    render(frame);
    
    Noble noble = null;
    do {
      var element = waitForClickedElement();
      switch (element) {
        case NobleElement nobleEl -> noble = nobleEl.noble();
        default -> {}
      }
    } while (noble == null);
    return noble;
  }

  //
  // switch

  // Si jamais après il clique ailleurs on lui throw un erreur qui reset le tour
  // public String waitForToolTip(Element element, List<String> messages) {
  // Consumer<Graphics2D> frame = (g2d -> {
  // // TODO CHANGE SIZE !!!
  // var tooltip = Tooltip.create(g2d, 200, 150, element, messages);
  // clickables.addAll(tooltip.options());
  // });
  // render(frame);
  //
  // // bellek faire un truc pour juste si tu clique ailleurs is j'ai le temps
  // var textElement = waitForClickedElement();
  // return switch (textElement) {
  // case TextBox textEl -> {
  // if (messages.contains(textEl.id())) {
  // yield textEl.id();
  // }
  // yield "";
  // }
  // default -> "";
  // };
  // }
  //
  // public Action promptPlayerActionHelper() {
  // var element = waitForClickedElement();
  // return switch (element) {
  // case TokenElement tokenEl -> {
  // // Forced to belong to the bank has there's no other clickable tokens;
  // if (tokenEl.tokenCount() <= 0) {
  // yield null;
  // }
  // fillInfoBox("Do you want to take this gem ?");
  // var response = waitForToolTip(tokenEl, List.of("Confirm", "Discard"));
  // if (response.equals("Confirm")) {
  // yield Action.TAKE;
  // }
  // yield null;
  // }
  // case CardElement card -> {
  // // Buy reserved possible !
  // var response = waitForToolTip(card, List.of("Buy", "Rerserve", "Discard"));
  // // afficher tooltip (doit contenir l'Element qui l'appelé!)
  // // yield Action.RESERVE;
  // yield Action.BUY;
  // }
  // case CardStack stack -> {
  // fillInfoBox("Do you want to reserve this card ?");
  // var response = waitForToolTip(stack, List.of("Confirm", "Discard"));
  // if (response.equals("Confirm")) {
  // yield Action.RESERVE;
  // }
  // yield null;
  // }
  // default -> null;
  // };
  // }

  // TODO: Change the way we do this
  // Objects.requireNonNull(lastClickedElement);
  //
  // var tokenList = new ArrayList<Token>();
  // switch (lastClickedElement) {
  // case TokenElement t -> {
  // Consumer<Graphics2D> frame = (g2d -> {
  // TokenElement.create(g2d, t.token(), t.tokenCount() - 1, false, t.layout());
  // });
  // render(frame);
  // tokenList.add(t.token());
  // }
  // default -> {
  // return tokenList;
  // }
  // }
  // lastClickedElement = null;
  //
  // fillInfoBox("Select one or two more token");
  // var element = waitForClickedElement();
  // Token token;
  // do {
  // token = waitForTokenElement(element);
  // System.out.println(token);
  // } while (token == null);
  // tokenList.add(token);
  //
  // private Token waitForTokenElement(Element element) {
  //
  // return switch (element) {
  // case TokenElement t -> {
  // var response = waitForToolTip(t, List.of("Confirm", "Discard"));
  // if (!response.equals("Confirm")) {
  // yield null;
  // }
  // clickables.clear();
  // render(lastTurn);
  //

  // }
  // default -> null;
  // };
  // }

  // reprint la gem moins un
  // deux fois même gems

  // ou 3 fois differentes

  // renvoyer la liste de token

}
