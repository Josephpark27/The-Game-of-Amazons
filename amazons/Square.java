package amazons;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static amazons.Utils.*;

/** Represents a position on an Amazons board.  Positions are numbered
 *  from 0 (lower-left corner) to 99 (upper-right corner).  Squares
 *  are immutable and unique: there is precisely one square created for
 *  each distinct position.  Clients create squares using the factory method
 *  sq, not the constructor.  Because there is a unique Square object for each
 *  position, you can freely use the cheap == operator (rather than the
 *  .equals method) to compare Squares, and the program does not waste time
 *  creating the same square over and over again.
 *  @author Joseph Park
 */
final class Square {

    /** The regular expression for a square designation (e.g.,
     *  a3). For convenience, it is in parentheses to make it a
     *  group.  This subpattern is intended to be incorporated into
     *  other pattern that contain square designations (such as
     *  patterns for moves). */
    static final String SQ = "([a-j](?:[1-9]|10))";

    /** Return my row position, where 0 is the bottom row. */
    int row() {
        return _row;
    }

    /** Return my column position, where 0 is the leftmost column. */
    int col() {
        return _col;
    }

    /** Return my index position (0-99).  0 represents square a1, and 99
     *  is square j10. */
    int index() {
        return _index;
    }

    /** returns piece. */
    Piece getPiece() {
        return piece;
    }

    /** sets piece.
     * @param p piece*/
    void setPiece(Piece p) {
        int pos = _row * 10 + _col;
        SQUARES[pos].piece = p;
        playboard[_row][_col].piece = p;
    }

    /** Return true iff THIS - TO is a valid queen move. */
    boolean isQueenMove(Square to) {
        boolean chk0 = col() != to.col() || row() != to.row();
        boolean chk1 = Math.abs(to.col() - col()) == Math.abs(to.row() - row());
        boolean chk2 = to.col() - col() == 0 || to.row() - row() == 0;
        return chk0 && to.piece == Piece.EMPTY && (chk1 || chk2);
    }

    /** Definitions of direction for queenMove.  DIR[k] = (dcol, drow)
     *  means that to going one step from (col, row) in direction k,
     *  brings us to (col + dcol, row + drow). */
    static final int[][] DIR = {
        { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 },
        { 0, -1 }, { -1, -1 }, {-1, 0 }, {-1, 1 }
    };

    /** Return the Square that is STEPS>0 squares away from me in direction
     *  DIR, or null if there is no such square.
     *  DIR = 0 for north, 1 for northeast, 2 for east, etc., up to 7 for
     *  northwest. If DIR has another value, return null. Thus, unless the
     *  result is null the resulting square is a queen move away from me. */
    Square queenMove(int dir, int steps) {
        if (dir < 0 || dir > 7) {
            return null;
        }
        int dcol = DIR[dir][0];
        int drow = DIR[dir][1];
        int row = _row;
        int col = _col;
        while (steps > 0) {
            row += drow;
            col += dcol;
            steps -= 1;
        }
        Square dest = playboard[row][col];
        return dest;
    }



    /** Return the direction (an int as defined in the documentation
     *  for queenMove) of the queen move THIS-TO. */
    int direction(Square to) {
        if (!isQueenMove(to)) {
            return -1;
        }
        int dirc = 0;
        int dirr = 0;
        if (_row != to._row && _col != to._col) {
            if (_row < to._row) {
                dirr = 1;
            } else {
                dirr = -1;
            }
            if (_col < to._col) {
                dirc = 1;
            } else {
                dirc = -1;
            }
        } else if (_row != to._row) {
            if (_row < to._row) {
                dirr = 1;
            } else {
                dirr = -1;
            }
        } else {
            if (_col < to._col) {
                dirc = 1;
            } else {
                dirc = -1;
            }
        }
        for (int i = 0; i < DIR.length; i += 1) {
            if (DIR[i][0] == dirc && DIR[i][1] == dirr) {
                return i;
            }
        }
        return -1;
    }

    /** direction if to-spear.
     * @param to to square
     * @param asEmpty from
     * @return int direction*/
    int directionAsEmpty(Square to, Square asEmpty) {
        if (!isEmptyMove(to, asEmpty)) {
            return -1;
        }
        int dirc = 0;
        int dirr = 0;
        if (_row != to._row && _col != to._col) {
            if (_row < to._row) {
                dirr = 1;
            } else {
                dirr = -1;
            }
            if (_col < to._col) {
                dirc = 1;
            } else {
                dirc = -1;
            }
        } else if (_row != to._row) {
            if (_row < to._row) {
                dirr = 1;
            } else {
                dirr = -1;
            }
        } else {
            if (_col < to._col) {
                dirc = 1;
            } else {
                dirc = -1;
            }
        }
        for (int i = 0; i < DIR.length; i += 1) {
            if (DIR[i][0] == dirc && DIR[i][1] == dirr) {
                return i;
            }
        }
        return -1;
    }

    /** valid to spear.
     * @param to to square
     * @param asEmpty from
     * @return boolean direction*/
    boolean isEmptyMove(Square to, Square asEmpty) {
        boolean chk0 = col() != to.col() || row() != to.row();
        boolean chk1 = Math.abs(to.col() - col()) == Math.abs(to.row() - row());
        boolean chk2 = to.col() - col() == 0 || to.row() - row() == 0;
        return chk0 && (to.piece == Piece.EMPTY || (to.col() == asEmpty.col()
                && to.row() == asEmpty.row())) && (chk1 || chk2);
    }

    @Override
    public String toString() {
        return _str;
    }

    /** Return true iff COL ROW is a legal square. */
    static boolean exists(int col, int row) {
        return row >= 0 && col >= 0 && row < Board.SIZE && col < Board.SIZE;
    }

    /** Return the (unique) Square denoting COL ROW. */
    static Square sq(int col, int row) {
        if (!exists(row, col)) {
            throw error("row or column out of bounds");
        }
        return SQUARES[row * 10 + col];
    }

    /** Return the (unique) Square denoting the position with index INDEX. */
    static Square sq(int index) {
        return SQUARES[index];
    }

    /** Return the (unique) Square denoting the position COL ROW, where
     *  COL ROW is the standard text format for a square (e.g., a4). */
    static Square sq(String col, String row) {
        int pos = (10 * (Integer.valueOf(row) - 1)) + col.charAt(0) - 'a';
        return SQUARES[pos];
    }

    /** Return the (unique) Square denoting the position in POSN, in the
     *  standard text format for a square (e.g. a4). POSN must be a
     *  valid square designation. */
    static Square sq(String posn) {
        assert posn.matches(SQ);
        String col = posn.substring(0, 1);
        String row = posn.substring(1);
        return sq(col, row);
    }

    /** Return an iterator over all Squares. */
    static Iterator<Square> iterator() {
        return SQUARE_LIST.iterator();
    }

    /** Return the Square with index INDEX.
     * @param index i*/
    Square(int index) {
        _index = index;
        _row = index / 10;
        _col = index % 10;
        piece = Piece.EMPTY;
        String c = Character.toString((char) ((index % 10) + 'a'));
        _str = String.format(c + (_row + 1));
    }

    /** The cache of all created squares, by index. */
    static final Square[] SQUARES = new Square[Board.SIZE * Board.SIZE];

    /** SQUARES viewed as a List. */
    private static final List<Square> SQUARE_LIST = Arrays.asList(SQUARES);

    static {
        for (int i = Board.SIZE * Board.SIZE - 1; i >= 0; i -= 1) {
            SQUARES[i] = new Square(i);
            SQUARES[i].setBoard(new Square[Board.SIZE][Board.SIZE]);
        }
    }

    /** set board.
     * @param b array*/
    public void setBoard(Square[][] b) {
        playboard = b;
    }

    /** My index position. */
    private final int _index;

    /** My row and column (redundant, since these are determined by _index). */
    private final int _row, _col;

    /** My String denotation. */
    private final String _str;

    /** piece. */
    private Piece piece;

    /** board. */
    private Square[][] playboard;
}
