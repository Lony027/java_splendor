package view.layout;

public record SimpleLayout(int x, int y, int width, int height) implements Layout {

  public SimpleLayout {
    if (x < 0 || y < 0 || width < 0 || height < 0) {
      throw new IllegalArgumentException("x, y, width and height must be positive");
    }
  }
}
