package chess.chessgui;

import chess.chessgui.players.Player;
import protocols.Chess;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game {

    Player white;
    Player black;
    ChessBoard board;

    int turnCount = 0;
    AtomicBoolean started = new AtomicBoolean(false);

    Thread gameThread;

    public Game(Player white, Player black, ChessBoard board) {
        this.white = white;
        this.black = black;
        this.board = board;
    }

    public void startGame() {
        turnCount = 0;
        started.set(true);
        gameThread = new Thread(() -> {
            while (started.get()) {
                System.out.println("TURN = " + turnCount);
                board.updateProtoBoard(sideToMove(), turnCount);
                Player currPlayer = turnCount % 2 == 0 ? white : black;

                currPlayer.takeTurn(board);

                turnCount++;
            }
        });

        gameThread.start();
    }

    public void stopGame() {
        started.set(false);
        gameThread.interrupt();
    }

    public Chess.PieceColor sideToMove() {
        return turnCount % 2 == 0 ? Chess.PieceColor.WHITE : Chess.PieceColor.BLACK;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public boolean gameStarted() {
        return started.get();
    }

    public void resetGame() {
        stopGame();
        board.reset();
    }
}
