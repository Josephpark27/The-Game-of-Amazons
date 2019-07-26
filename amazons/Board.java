package amazons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import static amazons.Piece.*;

/** The state of an Amazons Game.
 *  @author Joseph Park
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;
    /** list of white queen. */
    private static final int[][] STARTPOSW =
            new int[][] {{3, 0}, {6, 0}, {0, 3}, {9, 3}};
    /** list of black queen. */
    private static final int[][] STARTPOSB =
            new int[][] {{3, 9}, {6, 9}, {0, 6}, {9, 6}};
    /** all squares. */
    private Square[][] board;
    /** total num moves. */
    private int totalmoves;
    /** current white queen pos. */
    private ArrayList<Square> queenposw = new ArrayList<>();
    /** current black queen pos. */
    private ArrayList<Square> queenposb = new ArrayList<>();
    /** all made moves. */
    private Stack<Move> allMoves = new Stack<>();

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        init();
        this._turn = model._turn;
        this._winner = model._winner;
        for (int i = 0; i < board.length; i += 1) {
            for (int j = 0; j < board[0].length; j += 1) {
                this.board[i][j].setPiece(model.board[i][j].getPiece());
            }
        }
        this.totalmoves = model.totalmoves;
        this.queenposb.clear();
        for (int i = 0; i < model.queenposb.size(); i += 1) {
            this.queenposb.add(board[model.queenposb.get(i).row()]
                    [model.queenposb.get(i).col()]);
        }
        this.queenposw.clear();
        for (int i = 0; i < model.queenposw.size(); i += 1) {
            this.queenposw.add(board[model.queenposw.get(i).row()]
                    [model.queenposw.get(i).col()]);
        }
        this.allMoves.clear();
        for (int i = 0; i < model.allMoves.size(); i += 1) {
            Square f = model.allMoves.get(i).from();
            Square t = model.allMoves.get(i).to();
            Square s = model.allMoves.get(i).spear();
            this.allMoves.add(Move.mv(f, t, s));
        }
    }

    /** Clears the board to the initial position. */
    void init() {
        _turn = WHITE;
        _winner = EMPTY;
        board = new Square[SIZE][SIZE];
        for (int i = 0; i < board.length; i += 1) {
            for (int j = 0; j < board[0].length; j += 1) {
                int index = i * 10 + j;
                Square addsquare = new Square(index);
                addsquare.setBoard(board);
                board[i][j] = addsquare;
                Square.SQUARES[index] = addsquare;
            }
        }
        queenposw = new ArrayList<>();
        for (int i = 0; i < STARTPOSW.length; i += 1) {
            board[STARTPOSW[i][1]][STARTPOSW[i][0]].setPiece(Piece.WHITE);
            queenposw.add(board[STARTPOSW[i][1]][STARTPOSW[i][0]]);
        }
        queenposb = new ArrayList<>();
        for (int j = 0; j < STARTPOSB.length; j += 1) {
            board[STARTPOSB[j][1]][STARTPOSB[j][0]].setPiece(Piece.BLACK);
            queenposb.add(board[STARTPOSB[j][1]][STARTPOSB[j][0]]);
        }
        totalmoves = 0;
    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return totalmoves;
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        Iterator check = legalMoves(_turn);
        if (!check.hasNext()) {
            if (_turn == WHITE) {
                _winner = BLACK;
            } else {
                _winner = WHITE;
            }
        } else {
            _winner = EMPTY;
        }
        return _winner;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return s.getPiece();
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return Square.sq(col, row).getPiece();
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        s.setPiece(p);
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {
        board[row][col].setPiece(p);
        _winner = EMPTY;
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, col - 'a', row - '1');
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        int[] dir = Square.DIR[0];
        try {
            if (asEmpty != null) {
                dir = Square.DIR[from.directionAsEmpty(to, asEmpty)];
            } else {
                dir = Square.DIR[from.direction(to)];
            }
        } catch (ArrayIndexOutOfBoundsException a) {
            return false;
        }
        int checkcol = from.col() + dir[0];
        int checkrow = from.row() + dir[1];
        Piece checkp;
        while (checkcol != to.col() || checkrow != to.row()) {
            checkp = board[checkrow][checkcol].getPiece();
            if (asEmpty != null) {
                if (checkp != Piece.EMPTY) {
                    if (checkcol != asEmpty.col()
                            && checkrow != asEmpty.row()) {
                        return false;
                    }
                }
            } else {
                if (checkp != Piece.EMPTY) {
                    return false;
                }
            }
            checkcol += dir[0];
            checkrow += dir[1];
        }
        checkp = board[checkrow][checkcol].getPiece();
        if (asEmpty != null) {
            if (checkp != Piece.EMPTY) {
                if (checkcol != asEmpty.col() && checkrow != asEmpty.row()) {
                    return false;
                }
            }
        } else {
            if (checkp != Piece.EMPTY) {
                return false;
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return from.getPiece() == Piece.BLACK || from.getPiece() == Piece.WHITE;
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {
        int[] dir = Square.DIR[0];
        try {
            dir = Square.DIR[from.direction(to)];
        } catch (ArrayIndexOutOfBoundsException a) {
            return false;
        }
        int checkcol = from.col() + dir[0];
        int checkrow = from.row() + dir[1];
        Piece checkp;
        while (checkcol != to.col() || checkrow != to.row()) {
            checkp = board[checkrow][checkcol].getPiece();
            if (checkp != Piece.EMPTY) {
                return false;
            }
            checkcol += dir[0];
            checkrow += dir[1];
        }
        checkp = board[checkrow][checkcol].getPiece();
        if (checkp != Piece.EMPTY) {
            return false;
        }
        return true;
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        return isLegal(Move.mv(from, to, spear));

    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        if (!isLegal(move.from(), move.to())) {
            return false;
        }
        if (!isLegal(move.to(), move.spear())) {
            return false;
        }
        return true;
    }

    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {
        makeMove(Move.mv(from, to, spear));
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        Square from = board[move.from().row()][move.from().col()];
        Square to = board[move.to().row()][move.to().col()];
        Square spear = board[move.spear().row()][move.spear().col()];
        Piece p = from.getPiece();
        board[from.row()][from.col()].setPiece(EMPTY);
        board[to.row()][to.col()].setPiece(p);
        board[spear.row()][spear.col()].setPiece(SPEAR);
        Square.SQUARES[from.row() * 10 + from.col()].setPiece(EMPTY);
        Square.SQUARES[to.row() * 10 + to.col()].setPiece(p);
        Square.SQUARES[spear.row() * 10 + spear.col()].setPiece(SPEAR);
        allMoves.push(move);
        if (p == WHITE) {
            for (Square s : queenposw) {
                if (s.row() == from.row() && s.col() == from.col()) {
                    queenposw.remove(s);
                    break;
                }
            }
            queenposw.add(board[to.row()][to.col()]);
        } else {
            for (Square s : queenposb) {
                if (s.row() == from.row() && s.col() == from.col()) {
                    queenposb.remove(s);
                    break;
                }
            }
            queenposb.add(board[to.row()][to.col()]);
        }
        if (_turn == WHITE) {
            _turn = BLACK;
        } else {
            _turn = WHITE;
        }
        totalmoves += 1;
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (allMoves.empty()) {
            return;
        }
        Move prev = allMoves.pop();
        Square from = board[prev.from().row()][prev.from().col()];
        Square to = board[prev.to().row()][prev.to().col()];
        Square spear = board[prev.spear().row()][prev.spear().col()];
        Piece reset = to.getPiece();
        board[from.row()][from.col()].setPiece(reset);
        board[to.row()][to.col()].setPiece(EMPTY);
        Square.SQUARES[from.row() * 10 + from.col()].setPiece(reset);
        Square.SQUARES[to.row() * 10 + to.col()].setPiece(EMPTY);
        if (from.col() != spear.col() && from.row() != spear.col()) {
            board[spear.row()][spear.col()].setPiece(EMPTY);
            Square.SQUARES[spear.row() * 10 + spear.col()].setPiece(EMPTY);
        }
        if (_turn == WHITE) {
            for (Square s : queenposb) {
                if (s.row() == to.row() && s.col() == to.col()) {
                    queenposb.remove(s);
                    break;
                }
            }
            queenposb.add(board[from.row()][from.col()]);
            _turn = BLACK;
        } else {
            for (Square s : queenposw) {
                if (s.row() == to.row() && s.col() == to.col()) {
                    queenposw.remove(s);
                    break;
                }
            }
            queenposw.add(board[from.row()][from.col()]);
            _turn = WHITE;
        }
        totalmoves -= 1;
    }

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {
        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = 0;
            _steps = 1;
            _asEmpty = asEmpty;
        }

        @Override
        public boolean hasNext() {
            return _dir < 8 && checkN();
        }

        /** Advance dir.
         * @return boolean if has dir. */
        private boolean checkN() {
            int[] dir = Square.DIR[_dir];
            int nextc = _from.col() + (_steps * dir[0]);
            int nextr = _from.row() + (_steps * dir[1]);
            boolean chk1 = nextc >= 0 && nextc < SIZE;
            boolean chk2 = nextr >= 0 && nextr < SIZE, chk4 = true;
            if (chk1 && chk2) {
                chk4 = board[nextr][nextc].getPiece() == Piece.EMPTY;
                if (_asEmpty != null) {
                    boolean chk3 =
                            board[nextr][nextc].getPiece() == Piece.EMPTY;
                    chk4 = chk3 || (nextc == _asEmpty.col()
                            && nextr == _asEmpty.row());
                }
            }
            if (!chk1 || !chk2 || !chk4) {
                _dir += 1;
                _steps = 1;
                if (_dir > 7) {
                    return false;
                }
                dir = Square.DIR[_dir];
                nextc = _from.col() + (_steps * dir[0]);
                nextr = _from.row() + (_steps * dir[1]);
                boolean chk = nextc >= 0 && nextc < SIZE
                        && nextr >= 0 && nextr < SIZE;
                boolean chk6 = true;
                if (chk) {
                    chk6 = board[nextr][nextc].getPiece() != Piece.EMPTY;
                    if (_asEmpty != null) {
                        boolean chk5 = nextc != _asEmpty.col()
                                || nextr != _asEmpty.row();
                        chk6 = chk5 && board[nextr][nextc].getPiece()
                                != Piece.EMPTY;
                    }
                }
                while (!chk || chk6) {
                    _dir += 1;
                    if (_dir > 7) {
                        return false;
                    }
                    dir = Square.DIR[_dir];
                    nextc = _from.col() + (_steps * dir[0]);
                    nextr = _from.row() + (_steps * dir[1]);
                    chk = nextc >= 0 && nextc < SIZE
                            && nextr >= 0 && nextr < SIZE;
                    if (chk) {
                        chk6 = board[nextr][nextc].getPiece()
                                != Piece.EMPTY;
                        if (_asEmpty != null) {
                            boolean chk5 = nextc != _asEmpty.col()
                                    || nextr != _asEmpty.row();
                            chk6 = chk5 && board[nextr][nextc].getPiece()
                                    != Piece.EMPTY;
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public Square next() {
            int[] dir = Square.DIR[_dir];
            int nextc = _from.col() + (_steps * dir[0]);
            int nextr = _from.row() + (_steps * dir[1]);
            toNext();
            return board[nextr][nextc];
        }

        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {
            int[] dir = Square.DIR[_dir];
            int nextc = _from.col() + ((_steps + 1) * dir[0]);
            int nextr = _from.row() + ((_steps + 1) * dir[1]);
            boolean chk = nextc >= 0 && nextc < SIZE
                    && nextr >= 0 && nextr < SIZE, chkk = true;
            if (chk) {
                if (_asEmpty == null) {
                    chkk = board[nextr][nextc].getPiece() == Piece.EMPTY;
                } else {
                    chkk = board[nextr][nextc].getPiece() == Piece.EMPTY
                            || (nextc == _asEmpty.col()
                            && nextr == _asEmpty.row());
                }
            }
            if (chk && chkk) {
                _steps += 1;
            } else {
                _dir += 1;
                _steps = 1;
                if (_dir > 7) {
                    return;
                }
                dir = Square.DIR[_dir];
                nextc = _from.col() + (_steps * dir[0]);
                nextr = _from.row() + (_steps * dir[1]);
                chk = nextc >= 0 && nextc < SIZE && nextr >= 0 && nextr < SIZE;
                boolean chkkk = true;
                if (chk) {
                    if (_asEmpty == null) {
                        chkkk = board[nextr][nextc].getPiece() != Piece.EMPTY;
                    } else {
                        chkkk = board[nextr][nextc].getPiece() != Piece.EMPTY
                                && (nextc != _asEmpty.col()
                                && nextr != _asEmpty.row());
                    }
                }
                while (!chk || chkkk) {
                    _dir += 1;
                    if (_dir > 7) {
                        break;
                    }
                    dir = Square.DIR[_dir];
                    nextc = _from.col() + (_steps * dir[0]);
                    nextr = _from.row() + (_steps * dir[1]);
                    chk = nextc >= 0 && nextc < SIZE
                            && nextr >= 0 && nextr < SIZE;
                    if (chk) {
                        if (_asEmpty == null) {
                            chkkk = board[nextr][nextc].getPiece()
                                    != Piece.EMPTY;
                        } else {
                            chkkk = board[nextr][nextc].getPiece()
                                    != Piece.EMPTY && (nextc != _asEmpty.col()
                                    && nextr != _asEmpty.row());
                        }
                    }
                }
            }
        }

        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;
    }

    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<Move> {

        /** All legal moves for SIDE (WHITE or BLACK). */
        LegalMoveIterator(Piece side) {
            if (side == WHITE) {
                ArrayList<Square> cqueenposw = new ArrayList<>();
                for (Square s : queenposw) {
                    cqueenposw.add(s);
                }
                _startingSquares = cqueenposw.iterator();
            } else {
                ArrayList<Square> cqueenposb = new ArrayList<>();
                for (Square s : queenposb) {
                    cqueenposb.add(s);
                }
                _startingSquares = cqueenposb.iterator();
            }
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            nextstart();
            _fromPiece = side;
        }

        @Override
        public boolean hasNext() {
            return _pieceMoves.hasNext() || _startingSquares.hasNext()
                    || _spearThrows.hasNext();
        }

        @Override
        public Move next() {
            if (!_spearThrows.hasNext()) {
                nextstart();
                if (_pieceMoves.hasNext()) {
                    curmove = _pieceMoves.next();
                    _spearThrows = new ReachableFromIterator(curmove, _start);
                }
                while (!_spearThrows.hasNext()) {
                    if (!_pieceMoves.hasNext()) {
                        if (!_startingSquares.hasNext()) {
                            return null;
                        }
                        nextstart();
                    }
                    if (!_startingSquares.hasNext() && !_pieceMoves.hasNext()) {
                        return null;
                    }
                    curmove = _pieceMoves.next();
                    _spearThrows = new ReachableFromIterator(curmove, _start);
                }
            }
            return Move.mv(_start, curmove, _spearThrows.next());
        }

        /** Next start. */
        private void nextstart() {
            while (!_pieceMoves.hasNext()) {
                if (!_startingSquares.hasNext()) {
                    return;
                }
                _start = _startingSquares.next();
                _pieceMoves = new ReachableFromIterator(_start, null);
            }
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
        }

        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Current starting square. */
        private Square _start;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Square _nextSquare;
        /** Remaining moves from _start to consider. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
        /**Current move. */
        private Square curmove;
    }

    @Override
    public String toString() {
        String sboard = "";
        for (int i = SIZE - 1; i >= 0; i -= 1) {
            sboard += "   ";
            for (int j = 0; j < SIZE; j += 1) {
                if (board[i][j].getPiece() == Piece.WHITE) {
                    sboard += "W";
                } else if (board[i][j].getPiece() == Piece.BLACK) {
                    sboard += "B";
                } else if (board[i][j].getPiece() == Piece.SPEAR) {
                    sboard += "S";
                } else {
                    sboard += "-";
                }
                sboard += " ";
            }
            sboard = sboard.substring(0, sboard.lastIndexOf(" "));
            sboard += "\n";
        }
        return sboard;
    }

    /**  q.
     * @return array*/
    ArrayList<Square> getQueenposw() {
        return queenposw;
    }

    /** set.
     * @param a array*/
    void setQueenposw(ArrayList<Square> a) {
        queenposw = a;
    }

    /** q.
     * @return array*/
    ArrayList<Square> getQueenposb() {
        return queenposb;
    }

    /** set.
     * @param a array*/
    void setQueenposb(ArrayList<Square> a) {
        queenposb = a;
    }

    /** Get turn.
     * @return turn */
    Piece getTurn() {
        return _turn;
    }

    /** Get winner.
     * @return winner */
    Piece getWinner() {
        return _winner;
    }

    /** get board.
     * @return board*/
    Square[][] getBoard() {
        return board;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQUARES =
        Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
}
