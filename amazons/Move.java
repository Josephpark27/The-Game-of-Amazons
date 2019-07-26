package amazons;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static amazons.Square.sq;
import static amazons.Square.SQ;

/** A move in the game of Amazons. As for Squares, Moves are immutable
 *  and unique: there is only one move object for each possible move
 *  (generated by the factory method  mv, the constructor being private).
 *  As for Squares, you can freely use == to compare Moves.
 *  @author P. N. Hilfinger
 */
final class Move {

    /** The syntax for a move, using either the forms FROM-TO(SPEAR) or
     *  FROM TO SPEAR.  Either groups 1-3 or 4-6 in a match from this pattern
     *  contain FROM, TO, and SPEAR (the other 3 groups are null in each
     *  case. */
    static final Pattern MOVE_PATTERN =
        Pattern.compile(String.format("%s-%s\\(%s\\)|%s\\s+%s\\s+%s",
                                      SQ, SQ, SQ, SQ, SQ, SQ));

    /** Return the square moved from. */
    Square from() {
        return _from;
    }

    /** Return the square moved to. */
    Square to() {
        return _to;
    }

    /** Return the destination of the spear. */
    Square spear() {
        return _spear;
    }

    /** Return the unique Move FROM-TO(SPEAR). */
    static Move mv(Square from, Square to, Square spear) {
        if (MOVES[from.index()][to.index()][spear.index()] == null) {
            MOVES[from.index()][to.index()][spear.index()] =
                new Move(from, to, spear);
        }
        return MOVES[from.index()][to.index()][spear.index()];
    }

    /** Return the Move denoted by STR, if STR denotes a move with
     *  valid syntax in which the piece move and spear throw are proper
     *  queen moves, and null otherwise. */
    static Move mv(String str) {
        _moveMatcher.reset(str);
        if (_moveMatcher.matches()) {
            int k = _moveMatcher.group(1) != null ? 1 : 4;
            Square from = sq(_moveMatcher.group(k)),
                to = sq(_moveMatcher.group(k + 1)),
                spear = sq(_moveMatcher.group(k + 2));
            if (from.isQueenMove(to)) {
                return mv(from, to, spear);
            }
        }
        return null;
    }

    /** Return true iff STR has the right format for a Move, ignoring whether
     *  its piece-move and spear-throw are valid queen moves. */
    static boolean isGrammaticalMove(String str) {
        _moveMatcher.reset(str);
        return _moveMatcher.matches();
    }

    @Override
    public String toString() {
        return _str;
    }

    /** Construct the Move FROM-TO(SPEAR). */
    private Move(Square from, Square to, Square spear) {
        _from = from; _to = to; _spear = spear;
        _str = String.format("%s-%s(%s)", from, to, spear);
    }

    /** The cache of all Moves created. */
    private static final Move[][][] MOVES = new Move[100][100][100];

    /** The components of a Move. */
    private final Square _from, _to, _spear;
    /** The printed form of a Move. */
    private String _str;
    /**  A utility Matcher for moves, using MOVE_PATTERN. */
    private static Matcher _moveMatcher = MOVE_PATTERN.matcher("");
}
