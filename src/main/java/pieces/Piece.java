package pieces;

import chess.chessgui.ChessBoard;
import chess.chessgui.ChessTile;
import chess.chessgui.Comms;
import com.google.protobuf.InvalidProtocolBufferException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import protocols.Chess.*;
import protocols.Common;
import utils.ChessUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Piece implements Serializable {
    public static final DataFormat CHESS_PIECE = new DataFormat("chess.figure");
    public static final Map<String, Image> cachedImages = new HashMap<>();

    String name;
    String color;
    String imageString;

    transient ChessTile tile;
    int row,col;
//    transient Position.Builder position = Position.newBuilder();
    transient final PieceType pieceType;
    transient ProtoPiece protoPiece;
    transient List<Position> validMoves = new ArrayList<>();

    public Piece(String pieceName, String pieceColor, PieceType type) {
        pieceType = type;
        color = pieceColor;
        name = pieceName;
        imageString = "images/" + color + "_" + name + ".png";
        updateProtoPiece();

        if (!cachedImages.containsKey(imageString)){
            cachedImages.put(imageString, ChessUtils.loadImage(imageString, 50,50));
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

    public void moveTo(ChessTile chessTile) {
        System.out.println("MOVE TO CALLED");
        tile.clearPiece();
        tile = chessTile;
//        position.setCol(chessTile.getPosition().getCol())
//                .setRow(chessTile.getPosition().getRow());
        row = tile.getRow();
        col = tile.getCol();
        updateProtoPiece();
        tile.setPiece(this);

        ChessBoard.nextTurn();
    }

    public void requestValidMoves() {
//        System.out.println("REQUEST VALID MOVES CALLED");
        GetValidMoves validMovesMsg = GetValidMoves.newBuilder()
                .setBoard(ChessBoard.generateBoard())
                .setPieceToMove(protoPiece)
                .build();
//        System.out.println("CALLING COMMS.send");
        validMoves.clear();
        Comms.send(Common.MessageID.GET_VALID_MOVES, validMovesMsg, this::handleValidMoveResponse);

    }
    void handleValidMoveResponse(byte[] bytes) {
        try {
//            System.out.println("-----------------------");
//            System.out.println("PIECE PROCESSING Valid Move Response");
            ValidMovesResponse response = ValidMovesResponse.parseFrom(bytes);
            validMoves.addAll(response.getMovesList());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }








    public List<Position> getValidMoves() {
        return validMoves;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        updateProtoPiece();
    }
    public void setTile(ChessTile chessTile) {
        tile = chessTile;
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
    public Image getImage(){
        return cachedImages.get(imageString);
    }
    public ImageView getImageView() {
        return new ImageView(cachedImages.get(imageString));
    }

    public boolean tileIsValid(ChessTile chessTile) {
        System.out.println("EVALUATING TILE AT ROW = " + chessTile.getRow() +
                " COL = " + chessTile.getCol());
        System.out.println("FOR PIECE AT ROW = " + row + " COL = " + col);
        for (Position p : validMoves) {
            System.out.println(p);
            if (p.getCol() == chessTile.getCol() && p.getRow() == chessTile.getRow())
                return true;
        }
        return false;
    }
}
