package amazons;

import java.util.Iterator;

import static amazons.Piece.*;

/**
 * A Player that automatically generates moves.
 *
 * @author Joseph Park
 */
class AI extends Player {

    /**
     * A position magnitude indicating a win (for white if positive, black
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        Piece[] save = new Piece[Board.SIZE * Board.SIZE];
        int i = 0;
        for (Square s : Square.SQUARES) {
            save[i] = s.getPiece();
            i += 1;
        }
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        i = 0;
        for (Square s : Square.SQUARES) {
            s.setPiece(save[i]);
            i += 1;
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;
    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0 || board.winner() != EMPTY) {
            return staticScore(board);
        }
        Piece my = WHITE;
        int bestH = alpha;
        if (sense == -1) {
            my = BLACK;
            bestH = beta;
        }
        Iterator<Move> fm = board.legalMoves(my);
        if (!fm.hasNext()) {
            return staticScore(board);
        }
        Square d = board.getBoard()[0][0];
        Move bestM = Move.mv(d, d, d);
        boolean defaultBestM = true;
        while (fm.hasNext()) {
            Move potentialm = fm.next();
            if (potentialm == null && defaultBestM) {
                return findMove(board, depth, saveMove, sense, alpha, beta);
            }
            if (potentialm == null) {
                break;
            }
            board.makeMove(potentialm);
            int checkh = findMove(board, depth - 1,
                    !saveMove, sense * -1, alpha, beta);
            if (sense == 1) {
                if (checkh > alpha) {
                    alpha = checkh;
                    bestH = checkh;
                    bestM = potentialm;
                    defaultBestM = false;
                }
            } else {
                if (checkh < beta) {
                    beta = checkh;
                    bestH = checkh;
                    bestM = potentialm;
                    defaultBestM = false;
                }
            }
            board.undo();
            if (alpha >= beta) {
                break;
            }
        }
        if (saveMove) {
            _lastFoundMove = bestM;
        }
        return bestH;
    }

    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        int heuristic;
        if (N < 2) {
            heuristic = 3;
        } else if (N < 5) {
            heuristic = 6;
        } else if (N < 8) {
            heuristic = 8;
        } else {
            heuristic = 10;
        }
        return heuristic;
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }
        int h = 0;
        if (_myPiece == Piece.WHITE) {
            for (Square q : board.getQueenposw()) {
                Iterator<Square> allmoves = board.reachableFrom(q, null);
                while (allmoves.hasNext()) {
                    allmoves.next();
                    h += 1;
                }
            }
        } else {
            for (Square q : board.getQueenposb()) {
                Iterator<Square> allmoves = board.reachableFrom(q, null);
                while (allmoves.hasNext()) {
                    allmoves.next();
                    h += 1;
                }
            }
        }
        return h;
    }


}
