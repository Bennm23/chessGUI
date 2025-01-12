package utils;

import javafx.scene.image.Image;

import java.util.Objects;

public class ChessUtils {
    public static Image loadImage(String resource, int width, int height) {
        return new Image(Objects.requireNonNull(ChessUtils.class.getClassLoader().getResourceAsStream(resource)),
                width, height, true, true);
    }
}
