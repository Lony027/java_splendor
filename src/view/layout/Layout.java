package view.layout;

public interface Layout {
  
  int x(); 
  int y(); 
  int width(); 
  int height();

  default boolean isClicked(int clickX, int clickY) {
    if (clickX < 0 || clickY < 0) {
      throw new IllegalArgumentException();
    }
    return clickX >= x() && clickX <= x() + width() && clickY >= y() && clickY <= y() + height();
  }
}
