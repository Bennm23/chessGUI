package chess.chessgui;

import chess.chessgui.players.Player;
import javafx.concurrent.Task;
import protocols.Chess;

import java.util.concurrent.atomic.AtomicBoolean;

public class Game {

    Player white;
    Player black;
    ChessBoard board;

    int turnCount = 0;
    AtomicBoolean started = new AtomicBoolean(false);

    public Game(Player white, Player black, ChessBoard board) {
        this.white = white;
        this.black = black;
        this.board = board;
    }

    Thread gameThread;
    public void startGame() {
        turnCount = 0;
        started.set(true);
        gameThread = new Thread(() -> {
//            Player currPlayer;
            while (started.get()) {
                System.out.println("TURN = " + turnCount);
                board.updateProtoBoard(sideToMove(), turnCount);
                Player currPlayer = turnCount % 2 == 0 ? white : black;

//                new Thread(
//                        new Task<Void>() {
//                            @Override
//                            protected Void call() throws Exception {
                                currPlayer.takeTurn(board);
//                                return null;
//                            }
//                        how}
//                ).start();
                while (currPlayer.isWaitingForMove()) {
                    try {
                        System.out.println("Waiting for player ready");
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                }
//                currPlayer.takeTurn(board);

//                try {
//                     Thread.sleep(1000);
//                } catch (InterruptedException ignored) {}
//                if (white.myTurn(sideToMove())) {
//                    white.takeTurn(board);
//                } else {
//                    black.takeTurn(board);
//                }
                turnCount++;
            }
        });

        gameThread.start();
//        var gameTask = new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                System.out.println("TURN = " + turnCount);
//                board.updateProtoBoard(sideToMove(), turnCount);
//                if (white.myTurn(sideToMove())) {
//                    white.takeTurn(board);
//                } else {
//                    black.takeTurn(board);
//                }
//                turnCount++;
//                return null;
//            }
//        };
////        gameThread = new Thread(gameTask);
////        gameThread.start();
//        gameThread = new Thread(() -> {
//            Player currPlayer;
//            while (started.get()) {
//                System.out.println("TURN = " + turnCount);
//                board.updateProtoBoard(sideToMove(), turnCount);
//                currPlayer = turnCount % 2 == 0 ? white : black;
//
////                while (currPlayer.isWaitingForMove()) {
////                    try {
////                        Thread.sleep(50);
////                    } catch (InterruptedException ignored) {}
////                }
//                currPlayer.takeTurn(board);
//
////                try {
////                     Thread.sleep(1000);
////                } catch (InterruptedException ignored) {}
////                if (white.myTurn(sideToMove())) {
////                    white.takeTurn(board);
////                } else {
////                    black.takeTurn(board);
////                }
//                turnCount++;
//            }
//        });

//        gameThread.start();
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
