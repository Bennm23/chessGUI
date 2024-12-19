package chess.chessgui;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

import pieces.Piece;
import protocols.Chess.*;

public class ChessTile extends Label {
    public static final String ACCESSIBLE_STYLE = "-fx-border-color : red;";

    Piece piece;
    int row,col;
    public ChessTile(Color bgColor) {
        super.setMinSize(50,50);
        super.setMaxSize(50,50);
        super.setBackground(Background.fill(bgColor));
        setOnDragDetected(this::onDragDetected);
        super.setOnDragDropped(this::onDragDropped);
        super.setOnDragOver(this::onDragOver);
        super.setOnMouseEntered(this::onMouseEntered);
        super.setOnMouseExited(this::onMouseExited);

    }

    public ChessTile(Piece p, Color bgColor) {
        this(bgColor);
        setPiece(p);
    }

    void onDragDetected(MouseEvent e) {
        if (piece != null) {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            db.setDragView(piece.getImage());
            db.setDragViewOffsetX(25);
            db.setDragViewOffsetY(25);
            ClipboardContent content = new ClipboardContent();
            content.put(Piece.CHESS_PIECE,piece);
            db.setContent(content);
        }
        e.consume();
    }

    void onDragDone(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasContent(Piece.CHESS_PIECE)){
//            Piece source = deserializePiece(db);
        }
        e.consume();
    }
    void onDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasContent(Piece.CHESS_PIECE)){
            Piece source = deserializePiece(db);
            source = ChessBoard.getPiece(source.getRow() * 8 + source.getCol());
            if (source.getColor().equals(ChessBoard.playerTurn) && source.getValidMoves().size() != 0 && source.tileIsValid(this)) {
                source.moveTo(this);
            }
        }
        e.consume();
    }

    private void onMouseEntered(MouseEvent mouseEvent) {
        if (piece != null) {
            piece.requestValidMoves();
            for (Position p : piece.getValidMoves()) {
                ChessTile t = ChessBoard.getTile(p);
                t.setAccessibleHighlight(true);
            }
        }

        mouseEvent.consume();
    }
    private void onMouseExited(MouseEvent mouseEvent) {
        if (piece != null) {
            piece.requestValidMoves();
            for (Position p : piece.getValidMoves()) {
                ChessTile t = ChessBoard.getTile(p);
                t.setAccessibleHighlight(false);
            }
        }

        mouseEvent.consume();

    }

    private void setAccessibleHighlight(boolean b) {
        if (b) {
            this.setStyle(ACCESSIBLE_STYLE);
        } else {
            this.setStyle(this.getStyle().replace(ACCESSIBLE_STYLE, ""));
        }
    }


    void onDragOver(DragEvent e) {
        if (e.getDragboard().hasContent(Piece.CHESS_PIECE)){
            e.acceptTransferModes(TransferMode.MOVE);
        }
        e.consume();
    }

    private Piece deserializePiece(Dragboard db) {
        Piece source = (Piece) db.getContent(Piece.CHESS_PIECE);
        return source;
    }

    public void clearPiece() {
        piece = null;
        Platform.runLater(() -> super.setGraphic(new ImageView()));
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        super.setGraphic(piece.getImageView());
        piece.setTile(this);
    }
    public ProtoPiece getProtoPiece() {
        ProtoPiece.Builder protoPiece = ProtoPiece.newBuilder()
                .setCol(col)
                .setRow(row);
        if (piece == null) {
            return protoPiece
                    .setType(PieceType.NONE)
                    .build();
        }
        return protoPiece
                .setType(piece.getType())
                .setColor(piece.getColor())
                .build();
    }


    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public Piece getPiece() {
        return piece;
    }

}
