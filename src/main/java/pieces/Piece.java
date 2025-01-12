package pieces;

import chess.chessgui.ChessTile;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import protocols.Chess.PieceColor;
import protocols.Chess.PieceType;
import protocols.Chess.Position;
import protocols.Chess.ProtoPiece;
import utils.ChessUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Piece implements Serializable {
    public static final DataFormat CHESS_PIECE = new DataFormat("chess.figure");
    public static final Map<String, Image> cachedImages = new HashMap<>();

    transient final PieceType pieceType;
    String name;
    String color;
    String imageString;
    transient ChessTile tile;
    int row, col;
    transient ProtoPiece protoPiece;
    transient List<Position> validMoves = new ArrayList<>();

    public Piece(String pieceName, String pieceColor, PieceType type) {
        pieceType = type;
        color = pieceColor;
        name = pieceName;
        imageString = "images/" + color + "_" + name + ".png";
        updateProtoPiece();

        if (!cachedImages.containsKey(imageString)) {
            cachedImages.put(imageString, ChessUtils.loadImage(imageString, ChessTile.TILE_SIZE, ChessTile.TILE_SIZE));
        }
    }

    public static String colToFile(int col) {
        switch (col) {
            case 0 -> {
                return "a";
            }
            case 1 -> {
                return "b";
            }
            case 2 -> {
                return "c";
            }
            case 3 -> {
                return "d";
            }
            case 4 -> {
                return "e";
            }
            case 5 -> {
                return "f";
            }
            case 6 -> {
                return "g";
            }
            case 7 -> {
                return "h";
            }
            default -> {
                return "";
            }
        }
    }

    private void updateProtoPiece() {
        protoPiece = ProtoPiece.newBuilder()
                .setColor(getColor())
                .setCol(col)
                .setRow(row)
                .setType(pieceType)
                .build();
    }

    public void promoteTo(PieceType type) {

        Piece newPiece;
        switch (type) {
            case PAWN -> newPiece = new Pawn(color);
            case KNIGHT -> newPiece = new Knight(color);
            case BISHOP -> newPiece = new Bishop(color);
            case ROOK -> newPiece = new Rook(color);
            case QUEEN -> newPiece = new Queen(color);
            case KING -> newPiece = new King(color);
            default -> newPiece = null;
        }
        if (newPiece == null) return;

        newPiece.setPosition(row, col);

        tile.clearPiece();
        tile.setPiece(newPiece);
        tile = null;
    }

    public void moveTo(ChessTile destTile) {
        moveTo(destTile, true);
    }

    public void moveTo(ChessTile destTile, boolean primaryMove) {
        tile.clearPiece();
        tile = destTile;
        row = tile.getRow();
        col = tile.getCol();
        updateProtoPiece();
        tile.setPiece(this);
    }

    public void updateValids(List<Position> valids) {
        validMoves.clear();
        validMoves.addAll(valids);
    }

    public List<Position> getValidMoves() {
        return validMoves;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        updateProtoPiece();
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public PieceType getType() {
        return pieceType;
    }

    public PieceColor getColor() {
        return color.equals("black") ? PieceColor.BLACK : PieceColor.WHITE;
    }

    public Image getImage() {
        return cachedImages.get(imageString);
    }

    public ImageView getImageView() {
        return new ImageView(cachedImages.get(imageString));
    }

    public boolean tileIsValid(ChessTile chessTile) {
        for (Position p : validMoves) {
            System.out.println(p);
            if (p.getCol() == chessTile.getCol() && p.getRow() == chessTile.getRow())
                return true;
        }
        return false;
    }

    public ChessTile getTile() {
        return tile;
    }

    public void setTile(ChessTile chessTile) {
        tile = chessTile;
    }

    public boolean isPiece(PieceColor color, PieceType type) {
        return getColor().equals(color) && getType().equals(type);
    }
}
