package view.layout;

public record BatchLayout(int x, int y, int width, int height, int incrementX, int incrementY) implements Layout{
  
  public BatchLayout {
    if (x < 0 || y < 0 || width < 0 || height < 0) {
      throw new IllegalArgumentException("x, y, width and height must be positive");
    }
  }
}
