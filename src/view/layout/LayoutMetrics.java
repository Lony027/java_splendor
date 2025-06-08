package view.layout;

import java.util.Objects;
import com.github.forax.zen.ApplicationContext;

public record LayoutMetrics(ApplicationContext context, int height, int width, int marginY,
    int marginX, BatchLayout cardStack, BatchLayout card, BatchLayout bank, BatchLayout noble,
    SimpleLayout infobox, SimpleLayout sidebar, SimpleLayout reserved, SimpleLayout playerNobles,
    BatchLayout playerInfoLayout, BatchLayout reservedCardLayout) {
  public LayoutMetrics {
    Objects.requireNonNull(context);
    // @ todo les requiren on null
  }

  public boolean scalingChanged() {
    return height != context.getScreenInfo().height() || width != context.getScreenInfo().width();
  }

  public static LayoutMetrics create(ApplicationContext context) {
    Objects.requireNonNull(context);
    var height = context.getScreenInfo().height();
    var width = context.getScreenInfo().width();

    var marginY = (int) Math.round(height * 0.05);
    var marginX = (int) Math.round(width * 0.03);

    // find a way to not give width height margin everytime it is a bit ... awkward?
    var cardStackLayout = createCardStackLayout(width, height, marginX, marginY);
    var cardLayout = createCardLayout(width, height, marginX, marginY);
    var bankLayout = createBankLayout(width, height, marginX, marginY);
    var nobleLayout = createNobleLayout(width, height, marginX, marginY);
    var infoBoxLayout = createInfoBoxLayout(width, height, marginX, marginY);
    var sidebarLayout = createSidebarLayout(width, height, marginX, marginY);
    var reservedLayout = createReservedLayout(width, height, marginX, marginY);
    var playerNoblesLayout = createPlayerNoblesLayout(width, height, marginX, marginY);
    var playerInfoLayout = createPlayerInfoLayout(width, height, marginX, marginY);
    var reservedCardLayout = createReservedCardsLayout(width, height, marginX, marginY);

    return new LayoutMetrics(context, height, width, marginY, marginX, cardStackLayout, cardLayout,
        bankLayout, nobleLayout, infoBoxLayout, sidebarLayout, reservedLayout, playerNoblesLayout,
        playerInfoLayout, reservedCardLayout);
  }
  
  private static BatchLayout createReservedCardsLayout(int width, int height, int marginX,
      int marginY) {
    return new BatchLayout(marginX, yPercent(height, marginY, 75), widthPercent(width, marginX, 8), heightPercent(height, marginY, 19),
        widthPercent(width, marginX, 10), 0);
  }

  private static BatchLayout createPlayerInfoLayout(int width, int height, int marginX,
      int marginY) {
    return new BatchLayout(xPercent(width, marginX, 73), yPercent(height, marginY, 3),
        widthPercent(width, marginX, 26), heightPercent(height, marginY, 22), 0,
        heightPercent(height, marginY, 24));
  }


  private static BatchLayout createCardStackLayout(int width, int height, int marginX,
      int marginY) {
    return new BatchLayout(marginX, yPercent(height, marginY, 8), widthPercent(width, marginX, 8),
        heightPercent(height, marginY, 19), 0, heightPercent(height, marginY, 23));
  }

  private static BatchLayout createCardLayout(int width, int height, int marginX, int marginY) {
    return new BatchLayout(marginX + widthPercent(width, marginX, 10), yPercent(height, marginY, 8),
        widthPercent(width, marginX, 8), heightPercent(height, marginY, 19),
        widthPercent(width, marginX, 10), heightPercent(height, marginY, 23));
  }

  private static BatchLayout createBankLayout(int width, int height, int marginX, int marginY) {
    int tokenDiameter = heightPercent(height, marginY, 9);
    return new BatchLayout(xPercent(width, marginX, 51), yPercent(height, marginY, 8),
        tokenDiameter, tokenDiameter, 0, heightPercent(height, marginY, 11));
  }

  private static BatchLayout createNobleLayout(int width, int height, int marginX, int marginY) {
    return new BatchLayout(xPercent(width, marginX, 60), yPercent(height, marginY, 8),
        widthPercent(width, marginX, 8), heightPercent(height, marginY, 10), 0,
        heightPercent(height, marginY, 13));
  }

  private static SimpleLayout createInfoBoxLayout(int width, int height, int marginX, int marginY) {
    return new SimpleLayout(marginX, marginY, widthPercent(width, marginX, 70),
        heightPercent(height, marginY, 5));
  }

  private static SimpleLayout createSidebarLayout(int width, int height, int marginX, int marginY) {
    return new SimpleLayout(xPercent(width, marginX, 72), marginY, widthPercent(width, marginX, 28),
        heightPercent(height, marginY, 100));
  }

  private static SimpleLayout createReservedLayout(int width, int height, int marginX,
      int marginY) {
    return new SimpleLayout(marginX, yPercent(height, marginY, 75),
        widthPercent(width, marginX, 34), heightPercent(height, marginY, 25));
  }

  private static SimpleLayout createPlayerNoblesLayout(int width, int height, int marginX,
      int marginY) {
    return new SimpleLayout(xPercent(width, marginX, 36), yPercent(height, marginY, 75),
        widthPercent(width, marginX, 34), heightPercent(height, marginY, 25));
  }

  private static int widthPercent(int width, int marginX, int percent) {
    var usable = width - (marginX * 2);
    return (int) Math.round(usable * percent / 100.0);
  }

  private static int heightPercent(int height, int marginY, int percent) {
    var usable = height - (marginY * 2);
    return (int) Math.round(usable * percent / 100.0);
  }

  private static int xPercent(int width, int marginX, int percent) {
    var usable = width - (marginX * 2);
    return marginX + (int) Math.round(usable * percent / 100.0);
  }

  private static int yPercent(int height, int marginY, int percent) {
    var usable = height - (marginY * 2);
    return marginY + (int) Math.round(usable * percent / 100.0);
  }



  public int widthPct(int percent) {
    var usable = width - (marginX * 2);
    return (int) Math.round(usable * percent / 100.0);
  }

  public int heightPct(int percent) {
    var usable = height - (marginY * 2);
    return (int) Math.round(usable * percent / 100.0);
  }

  public int xPct(int percent) {
    var usable = width - (marginX * 2);
    return marginX + (int) Math.round(usable * percent / 100.0);
  }

  public int yPct(int percent) {
    var usable = height - (marginY * 2);
    return marginY + (int) Math.round(usable * percent / 100.0);
  }
}
