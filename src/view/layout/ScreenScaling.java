package view.layout;

import java.util.Objects;
import com.github.forax.zen.ApplicationContext;

public record ScreenScaling(ApplicationContext context, int height, int width, int marginX,
    int marginY) {

  public ScreenScaling {
    Objects.requireNonNull(context);
    if (height < 0 || width < 0 || marginY < 0 || marginX < 0) {
      throw new IllegalArgumentException("height, width, marginX and marginY must be positive");
    }
  }

  public boolean scalingChanged() {
    return height != context.getScreenInfo().height() || width != context.getScreenInfo().width();
  }

  public static ScreenScaling create(ApplicationContext context) {
    Objects.requireNonNull(context);
    var height = context.getScreenInfo().height();
    var width = context.getScreenInfo().width();
    var marginY = (int) Math.round(height * 0.05);
    var marginX = (int) Math.round(width * 0.03);

    return new ScreenScaling(context, height, width, marginX, marginY);
  }

  public int scaledWidth(int percent) {
    var usable = width - (marginX * 2);
    return (int) Math.round(usable * percent / 100.0);
  }

  public int scaledHeight(int percent) {
    var usable = height - (marginY * 2);
    return (int) Math.round(usable * percent / 100.0);
  }

  public int offsetX(int percent) {
    var usable = width - (marginX * 2);
    return marginX + (int) Math.round(usable * percent / 100.0);
  }

  public int offsetY(int percent) {
    var usable = height - (marginY * 2);
    return marginY + (int) Math.round(usable * percent / 100.0);
  }

  public BatchLayout createReservedCardsLayout() {
    return new BatchLayout(marginX, offsetY(75), scaledWidth(8), scaledHeight(19), scaledWidth(10),
        0);
  }

  public BatchLayout createPlayerInfoLayout() {
    return new BatchLayout(offsetX(73), offsetY(3), scaledWidth(26), scaledHeight(22), 0,
        scaledHeight(24));
  }


  public BatchLayout createCardStackLayout() {
    return new BatchLayout(marginX, offsetY(8), scaledWidth(8), scaledHeight(19), 0,
        scaledHeight(23));
  }

  public BatchLayout createCardLayout() {
    return new BatchLayout(marginX + scaledWidth(10), offsetY(8), scaledWidth(8), scaledHeight(19),
        scaledWidth(10), scaledHeight(23));
  }

  public BatchLayout createBankLayout() {
    var tokenDiameter = scaledHeight(9);
    return new BatchLayout(offsetX(51), offsetY(8), tokenDiameter, tokenDiameter, 0,
        scaledHeight(11));
  }

  public BatchLayout createNobleLayout() {
    return new BatchLayout(offsetX(60), offsetY(8), scaledWidth(8), scaledHeight(10), 0,
        scaledHeight(13));
  }

  public SimpleLayout createInfoBoxLayout() {
    return new SimpleLayout(marginX, marginY, scaledWidth(70), scaledHeight(5));
  }

  public SimpleLayout createSidebarLayout() {
    return new SimpleLayout(offsetX(72), marginY, scaledWidth(28), scaledHeight(100));
  }

  public SimpleLayout createReservedLayout() {
    return new SimpleLayout(marginX, offsetY(75), scaledWidth(34), scaledHeight(25));
  }

  public SimpleLayout createPlayerNoblesLayout() {
    return new SimpleLayout(offsetX(36), offsetY(75), scaledWidth(34), scaledHeight(25));
  }

  public BatchLayout createPlayerNoblesCardLayout() {
    return new BatchLayout(offsetX(36), offsetY(75), scaledWidth(8), scaledHeight(10),
        scaledWidth(10), 0);
  }

  public SimpleLayout createPopupLayout() {
    return new SimpleLayout(offsetX(50), offsetY(50), scaledWidth(50), scaledHeight(50));
  }

  public SimpleLayout createFullLayout() {
    return new SimpleLayout(offsetX(15), offsetY(15), scaledWidth(75), scaledHeight(75));
  }

  public BatchLayout createTokenCollectionBackLayout() {
    return new BatchLayout(offsetX(25), offsetY(47), scaledWidth(8), scaledHeight(20),
        scaledWidth(9), 0);
  }

  public BatchLayout createChooseNobleLayout() {
    return new BatchLayout(offsetX(25), offsetY(47), scaledWidth(8), scaledHeight(20),
        scaledWidth(9), 0);
  }



}
