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
    public static final String ACCESSIBLE_CLASS = "accessible_highlight";
    public static final String LAST_MOVE_CLASS = "last_move_highlight";
    public static final int TILE_SIZE = 100;

    boolean highlighted = false;

    Piece piece;
    int row,col;

    MoveSignal moveHandler;
    HoverSignal hoverHandler;
    public ChessTile(Color bgColor, MoveSignal moveHandler, HoverSignal hoverHandler) {
        this.moveHandler = moveHandler;
        this.hoverHandler = hoverHandler;
        super.setMinSize(TILE_SIZE,TILE_SIZE);
        super.setMaxSize(TILE_SIZE,TILE_SIZE);
        super.setBackground(Background.fill(bgColor));
        setOnDragDetected(this::onDragDetected);
        super.setOnDragDropped(this::onDragDropped);
        super.setOnDragOver(this::onDragOver);
        super.setOnMouseEntered(this::onMouseEntered);
        super.setOnMouseExited(this::onMouseExited);

        super.setOnMouseClicked(this::onMouseClicked);
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            setLastMoveHighlight(!highlighted);
        } else if (mouseEvent.getButton() == MouseButton.PRIMARY){
            setLastMoveHighlight(false);
        }
        mouseEvent.consume();
    }

    public ChessTile(Piece p, Color bgColor, MoveSignal moveHandler, HoverSignal hoverHandler) {
        this(bgColor, moveHandler, hoverHandler);
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
    public interface MoveSignal {
        void handleMove(int pieceRow, int pieceColr, ChessTile dest);
    }
    public interface HoverSignal {
        void handleHover(Piece piece, boolean enterSquare);
    }

    void onDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasContent(Piece.CHESS_PIECE)){
            Piece source = deserializePiece(db);
            moveHandler.handleMove(source.getRow(), source.getCol(), this);
        }
        e.consume();
    }

    private void onMouseEntered(MouseEvent mouseEvent) {
        if (piece != null) {
            hoverHandler.handleHover(piece, true);
        }
        mouseEvent.consume();
    }
    private void onMouseExited(MouseEvent mouseEvent) {
        if (piece != null) {
            hoverHandler.handleHover(piece, false);
        }
        mouseEvent.consume();
    }


    public void setAccessibleHighlight(boolean b) {
        if (b) {
            this.getStyleClass().add(ACCESSIBLE_CLASS);
        } else {
            this.getStyleClass().remove(ACCESSIBLE_CLASS);
        }
    }

    public void setLastMoveHighlight(boolean on) {
        if (!highlighted && on) {
            this.getStyleClass().add(LAST_MOVE_CLASS);
            highlighted = true;
        } else if (highlighted && !on) {
            this.getStyleClass().remove(LAST_MOVE_CLASS);
            highlighted = false;
        }
    }

    void onDragOver(DragEvent e) {
        if (e.getDragboard().hasContent(Piece.CHESS_PIECE)){
            e.acceptTransferModes(TransferMode.MOVE);
        }
        e.consume();
    }

    private Piece deserializePiece(Dragboard db) {
        return (Piece) db.getContent(Piece.CHESS_PIECE);
    }

    public void clearPiece() {
        piece = null;
        Platform.runLater(() -> super.setGraphic(new ImageView()));
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        piece.setTile(this);
        Platform.runLater(() -> {
            super.setGraphic(piece.getImageView());
        });
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
