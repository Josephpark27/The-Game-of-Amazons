package amazons;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Consumer;

import static amazons.Utils.*;
import static amazons.Piece.*;

/** The input/output and GUI controller for play of Amazons.
 *  @author Joseph Park */
final class Controller {

    /** Controller for one or more games of Amazons, using
     *  MANUALPLAYERTEMPLATE as an exemplar for manual players
     *  (see the Player.create method) and AUTOPLAYERTEMPLATE
     *  as an exemplar for automated players.  Reports
     *  board changes to VIEW at appropriate points.  Uses REPORTER
     *  to report moves, wins, and errors to user. If LOGFILE is
     *  non-null, copies all commands to it. If STRICT, exits the
     *  program with non-zero code on receiving an erroneous move from a
     *  player. */
    Controller(View view, PrintStream logFile, Reporter reporter,
               Player manualPlayerTemplate, Player autoPlayerTemplate) {
        _view = view;
        _playing = false;
        _logFile = logFile;
        _input = new Scanner(System.in);
        _autoPlayerTemplate = autoPlayerTemplate;
        _manualPlayerTemplate = manualPlayerTemplate;
        _nonPlayer = manualPlayerTemplate.create(EMPTY, this);
        _reporter = reporter;
    }

    /** Play Amazons. */
    void play() {
        _playing = true;
        _winner = null;
        _board.init();
        _white = _manualPlayerTemplate.create(WHITE, this);
        _black = _autoPlayerTemplate.create(BLACK, this);
        while (_playing) {
            _view.update(_board);
            String command;
            if (_winner == null) {
                if (_board.turn() == WHITE) {
                    command = _white.myMove();
                } else {
                    command = _black.myMove();
                }
            } else {
                command = _nonPlayer.myMove();
                if (command == null) {
                    command = "quit";
                }
            }
            try {
                executeCommand(command);
            } catch (IllegalArgumentException excp) {
                reportError("Error: %s%n", excp.getMessage());
            }
        }
        if (_logFile != null) {
            _logFile.close();
        }
    }

    /** Return the current board.  The value returned should not be
     *  modified by the caller. */
    Board board() {
        return _board;
    }

    /** Return a random integer in the range 0 inclusive to U, exclusive.
     *  Available for use by AIs that use random selections in some cases.
     *  Once setRandomSeed is called with a particular value, this method
     *  will always return the same sequence of values. */
    int randInt(int U) {
        return _randGen.nextInt(U);
    }

    /** Re-seed the pseudo-random number generator (PRNG) that supplies randInt
     *  with the value SEED. Identical seeds produce identical sequences.
     *  Initially, the PRNG is randomly seeded. */
    void setSeed(long seed) {
        _randGen.setSeed(seed);
    }

    /** Return the next line of input, or null if there is no more. First
     *  prompts for the line.  Trims the returned line (if any) of all
     *  leading and trailing whitespace. */
    String readLine() {
        System.out.print("> ");
        System.out.flush();
        if (_input.hasNextLine()) {
            return _input.nextLine().trim();
        } else {
            return null;
        }
    }

    /** Report error by calling reportError(FORMAT, ARGS) on my reporter. */
    void reportError(String format, Object... args) {
        _reporter.reportError(format, args);
    }

    /** Report note by calling reportNote(FORMAT, ARGS) on my reporter. */
    void reportNote(String format, Object... args) {
        _reporter.reportNote(format, args);
    }

    /** Report move by calling reportMove(MOVE) on my reporter. */
    void reportMove(Move move) {
        _reporter.reportMove(move);
    }

    /** A Command is pair (<pattern>, <processor>), where <pattern> is a
     *  Matcher that matches instances of a particular command, and
     *  <processor> is a functional object whose .accept method takes a
     *  successfully matched Matcher and performs some operation. */
    private static class Command {
        /** A new Command that matches PATN (a regular expression) and uses
         *  PROCESSOR to process commands that match the pattern. */
        Command(String patn, Consumer<Matcher> processor) {
            _matcher = Pattern.compile(patn).matcher("");
            _processor = processor;
        }

        /** A Matcher matching my pattern. */
        protected final Matcher _matcher;
        /** The function object that implements my command. */
        protected final Consumer<Matcher> _processor;
    }

    /** A list of Commands describing the valid textual commands to the
     *  Amazons program and the methods to process them. */
    private Command[] _commands = {
        new Command("quit$", this::doQuit),
        new Command("seed\\s+(\\d+)$", this::doSeed),
        new Command("dump$", this::doDump),
        new Command("new$", this::doNew),
        new Command("auto\\s+(white|black|White|Black)$", this::doAuto),
        new Command("manual\\s+(white|black|White|Black)$", this::doManual),
        new Command("[a-z](?:[1-9]|0[1-9]|10)\\s[a-z](?:[1-9]|0[1-9]|10)"
                + "\\s[a-z](?:[1-9]|0[1-9]|10)$", this::doMove),
        new Command("[a-z](?:[1-9]|0[1-9]|10)-[a-z](?:[1-9]|0[1-9]|10)"
                + "\\([a-z](?:[1-9]|0[1-9]|10)\\)$", this::doMove),
        new Command("undo$", this::doUndo)
    };

    /** A Matcher whose Pattern matches comments. */
    private final Matcher _comment = Pattern.compile("#.*").matcher("");

    /** Check that CMND is one of the valid Amazons commands and execute it, if
     *  so, raising an IllegalArgumentException otherwise. */
    private void executeCommand(String cmnd) {
        if (_logFile != null) {
            _logFile.println(cmnd);
            _logFile.flush();
        }

        _comment.reset(cmnd);
        cmnd = _comment.replaceFirst("").trim().toLowerCase();

        if (cmnd.isEmpty()) {
            return;
        }
        for (Command parser : _commands) {
            parser._matcher.reset(cmnd);
            if (parser._matcher.matches()) {
                parser._processor.accept(parser._matcher);
                return;
            }
        }
        throw error("Bad command: %s", cmnd);
    }

    /** Command "new". */
    private void doNew(Matcher unused) {
        _board.init();
        _winner = null;
    }

    /** Command "quit". */
    private void doQuit(Matcher unused) {
        _playing = false;
    }

    /** Command "seed N" where N is the first group of MAT. */
    private void doSeed(Matcher mat) {
        try {
            setSeed(Long.parseLong(mat.group(1)));
        } catch (NumberFormatException excp) {
            throw error("number too large");
        }
    }

    /** auto.
     * @param mat mat
     */
    private void doAuto(Matcher mat) {
        try {
            String c = mat.group(1).toLowerCase();
            if (c.equals("black")) {
                _black = _autoPlayerTemplate.create(BLACK, this);
                return;
            }
            if (c.equals("white")) {
                _white = _autoPlayerTemplate.create(WHITE, this);
                return;
            }
            throw new IOException("Incorrect input");
        } catch (IOException i) {
            System.out.println("Incorrect input");
            return;
        }
    }

    /** manual.
     * @param mat mat */
    private void doManual(Matcher mat) {
        try {
            String c = mat.group(1).toLowerCase();
            if (c.equals("black")) {
                _black = _manualPlayerTemplate.create(BLACK, this);
                return;
            }
            if (c.equals("white")) {
                _white = _manualPlayerTemplate.create(WHITE, this);
                return;
            }
            throw new IOException("Incorrect input");
        } catch (IOException i) {
            System.out.println("Incorrect input");
            return;
        }
    }

    /** undo.
     * @param move move */
    private void doUndo(Matcher move) {
        _board.undo();
    }

    /** move.
     * @param  mat mat*/
    private void doMove(Matcher mat) {
        try {
            String c = mat.group(0).toLowerCase();
            Boolean punctuation = doMoveH(c);
            ArrayList<Character> allcols = new ArrayList<>();
            ArrayList<Integer> allrows = new ArrayList<>();
            for (int i = 0; i < c.length(); i += 1) {
                Character current = c.charAt(i);
                if (Character.isLetter(current)) {
                    allcols.add(current);
                }
                if (Character.isDigit(current)) {
                    if (punctuation) {
                        if (c.substring(i, i + 2).equals("10")) {
                            allrows.add(10);
                            i += 1;
                        } else {
                            allrows.add(Integer.valueOf
                                    (String.valueOf(current)));
                        }
                    } else {
                        if (i + 1 < c.length()
                                && c.substring(i, i + 2).equals("10")) {
                            allrows.add(10);
                            i += 1;
                        } else {
                            allrows.add(Integer.valueOf
                                    (String.valueOf(current)));
                        }
                    }
                }
            }
            int fromc = allcols.get(0) - 'a';
            int fromr = allrows.get(0) - 1;
            if (fromc < 0 || fromc > 9 || fromr < 0 || fromr > 9) {
                throw new IOException("Incorrect input-1");
            }
            Square from = _board.getBoard()[fromr][fromc];
            int toc = allcols.get(1) - 'a';
            int tor = allrows.get(1) - 1;
            if (toc < 0 || toc > 9 || tor < 0 || tor > 9) {
                throw new IOException("Incorrect input-2");
            }
            Square to = _board.getBoard()[tor][toc];
            int spearc = allcols.get(2) - 'a';
            int spearr = allrows.get(2) - 1;
            if (spearc < 0 || spearc > 9 || spearr < 0 || spearr > 9) {
                throw new IOException("Incorrect input-3");
            }
            Square spear = _board.getBoard()[spearr][spearc];
            if (from.getPiece() != _board.turn()) {
                throw new IOException("Not your turn");
            }
            _board.makeMove(from, to, spear);
            setWinner();
        } catch (IOException i) {
            reportError("Invalid move. Please try again.");
            return;
        }
    }

    /** punctuation.
     * @param c String
     * @return boolean*/
    private boolean doMoveH(String c) {
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
        if (!chk1 && chk2) {
            punctuation = false;
        }
        return punctuation;
    }

    /** set winner. */
    private void setWinner() {
        Piece checkw = _board.winner();
        if (checkw != EMPTY) {
            _winner = _board.getWinner();
            if (_winner == WHITE) {
                reportNote("White wins.");
            } else {
                reportNote("Black wins.");
            }
        } else {
            _winner = null;
        }
    }

    /** Dump the contents of the board on standard output. */
    private void doDump(Matcher unused) {
        System.out.printf("===%n%s===%n", _board);
    }

    /** The board. */
    private Board _board = new Board();

    /** The winning side of the current game. */
    private Piece _winner;

    /** True while game is still active. */
    private boolean _playing;

    /** The object that is displaying the current game. */
    private View _view;

    /** My pseudo-random number generator. */
    private Random _randGen = new Random();

    /** Log file, or null if absent. */
    private PrintStream _logFile;

    /** Input source. */
    private Scanner _input;

    /** The current White and Black players, each created from
     *  _autoPlayerTemplate or _manualPlayerTemplate. */
    private Player _white, _black;

    /** A dummy Player used to return commands but not moves when no
     *  game is in progress. */
    private Player _nonPlayer;

    /** The current templates for manual and automated players. */
    private Player _autoPlayerTemplate, _manualPlayerTemplate;

    /** Reporter for messages and errors. */
    private Reporter _reporter;

}
