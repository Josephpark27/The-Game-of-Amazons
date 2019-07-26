package amazons;
import java.util.ArrayList;

/** A Player that takes input as text commands from the standard input.
 *  @author Joseph Park
 */
class TextPlayer extends Player {

    /**
     * A new TextPlayer with no piece or controller (intended to produce
     * a template).
     */
    TextPlayer() {
        this(null, null);
    }

    /**
     * A new TextPlayer playing PIECE under control of CONTROLLER.
     */
    private TextPlayer(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new TextPlayer(piece, controller);
    }

    @Override
    String myMove() {
        while (true) {
            String line = _controller.readLine();
            if (line == null) {
                return "quit";
            } else if (Move.isGrammaticalMove(line)) {
                String c = line.toLowerCase();
                if (c.length() < 8 || c.length() > 12) {
                    reportE();
                    return "";
                }
                boolean punctuation = checkPunctuation(c);
                ArrayList<Character> allcols = parsecol(c, punctuation);
                ArrayList<Integer> allrows = parserow(c, punctuation);
                int fromc = allcols.get(0) - 'a';
                int fromr = allrows.get(0) - 1;
                if (fromc < 0 || fromc > 9 || fromr < 0 || fromr > 9) {
                    reportE();
                    return "";
                }
                Square from = board().getBoard()[fromr][fromc];
                int toc = allcols.get(1) - 'a';
                int tor = allrows.get(1) - 1;
                if (toc < 0 || toc > 9 || tor < 0 || tor > 9) {
                    reportE();
                    return "";
                }
                Square to = board().getBoard()[tor][toc];
                int spearc = allcols.get(2) - 'a';
                int spearr = allrows.get(2) - 1;
                if (spearc < 0 || spearc > 9 || spearr < 0 || spearr > 9) {
                    reportE();
                    return "";
                }
                Square spear = board().getBoard()[spearr][spearc];
                if (checks(c, from, to, spear)) {
                    reportE();
                    return "";
                }
                return line;
            } else {
                return line;
            }
        }
    }

    /** prarse.
     * @param c move
     * @param punctuation format
     * @return arraylist*/
    private ArrayList<Character> parsecol(String c, boolean punctuation) {
        ArrayList<Character> allcols = new ArrayList<>();
        for (int i = 0; i < c.length(); i += 1) {
            Character current = c.charAt(i);
            if (Character.isLetter(current)) {
                allcols.add(current);
            }
        }
        return allcols;
    }

    /** prarse.
     * @param c move
     * @param punctuation format
     * @return arraylist*/
    private ArrayList<Integer> parserow(String c, boolean punctuation) {
        ArrayList<Integer> allrows = new ArrayList<>();
        for (int i = 0; i < c.length(); i += 1) {
            Character current = c.charAt(i);
            if (Character.isDigit(current)) {
                if (punctuation) {
                    if (c.substring(i, i + 2).equals("10")) {
                        allrows.add(10);
                        i += 1;
                    } else {
                        allrows.add(convertC(current));
                    }
                } else {
                    if (i + 1 < c.length()
                            && c.substring(i, i + 2).equals("10")) {
                        allrows.add(10);
                        i += 1;
                    } else {
                        allrows.add(convertC(current));
                    }
                }
            }
        }
        return allrows;
    }
    /** converts.
     * @param current c
     * @return int
     */
    private int convertC(Character current) {
        return Integer.valueOf(String.valueOf(current));
    }

    /** checks.
     * @param c move
     * @param f from
     * @param t to
     * @param s spear
     * @return boolean
     */
    private boolean checks(String c, Square f, Square t, Square s) {
        return Move.mv(c) == null || !board().isLegal(f, t)
                || !board().isUnblockedMove(t, s, f)
                || board().turn() != _myPiece
                || board().winner() != Piece.EMPTY;
    }

    /** punctuation helper.
     * @param c String
     * @return boolean */
    private boolean checkPunctuation(String c) {
        boolean punctuation = true;
        int spaces = 0;
        int parenthesis = 0;
        int dash = 0;
        for (int i = 0; i < c.length(); i += 1) {
            if (Character.isWhitespace(c.charAt(i))) {
                spaces += 1;
            }
            Character cur = c.charAt(i);
            if (cur.equals('-')) {
                dash += 1;
            }
            if (cur.equals('(') || cur.equals(')')) {
                parenthesis += 1;
            }
        }
        boolean chk1 = spaces == 0 && parenthesis == 2 && dash == 1;
        boolean chk2 = spaces == 2 && parenthesis == 0 && dash == 0;
        if (!chk1 && !chk2) {
            reportE();
            return false;
        }
        if (!chk1 && chk2) {
            punctuation = false;
        }
        return punctuation;
    }

    /** reports. */
    private void reportE() {
        _controller.reportError("Invalid move. Please try again.");
    }
}
