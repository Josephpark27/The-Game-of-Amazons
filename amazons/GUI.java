package amazons;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.concurrent.ArrayBlockingQueue;

/** The GUI controller for an Amazons board and buttons.
 *  @author Joseph Park
 */
class GUI extends TopLevel implements View, Reporter {

    /** Minimum size of board in pixels. */
    private static final int MIN_SIZE = 500;

    /** A new window with given TITLE providing a view of an Amazons board. */
    GUI(String title) {
        super(title, true);

        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Settings->Seed", this::newSeed);
        addMenuButton("Game->New", this::newGame);
        addMenuButton("Settings->Set White as AI", this::newWhiteAI);
        addMenuButton("Settings->Set White as Manual", this::newWhiteManual);
        addMenuButton("Settings->Set Black as AI", this::newBlackeAI);
        addMenuButton("Settings->Set Black as Manual", this::newBlackManual);
        addMenuButton("View->White", this::newViewWhite);
        addMenuButton("View->Black", this::newViewBlack);
        addMenuButton("Game->Undo", this::newUndo);

        _widget = new BoardWidget(_pendingCommands);
        add(_widget,
            new LayoutSpec("y", 1,
                           "height", 1,
                           "width", 3));
    }

    /** Response to "Quit" button click. */
    private void quit(String dummy) {
        _pendingCommands.offer("quit");
    }

    /** Pattern describing the 'seed' command's arguments. */
    private static final Pattern SEED_PATN =
        Pattern.compile("\\s*(-?\\d{1,18})\\s*$");

    /** Response to "Seed" button click. */
    private void newSeed(String dummy) {
        String response =
            getTextInput("Enter new random seed.",
                         "New seed",  "plain", "");
        if (response != null) {
            Matcher mat = SEED_PATN.matcher(response);
            if (mat.matches()) {
                _pendingCommands.offer(String.format("seed %s", mat.group(1)));
            } else {
                showMessage("Enter an integral seed value.", "Error", "error");
            }
        }
    }

    /** new Game. */
    private void newGame(String dummy) {
        if (_widget.getBoard() != null) {
            _pendingCommands.offer("manual white");
            _pendingCommands.offer("new");
            _widget.drawPlain(_widget.getPaintBoard());
        }
    }

    /** new whiteAI. */
    private void newWhiteAI(String dummy) {
        whiteAI = true;
        _widget.acceptingMoves(false);
        _pendingCommands.offer("auto white");
    }

    /** new WhiteManual. */
    private void newWhiteManual(String dummy) {
        whiteAI = false;
        _widget.acceptingMoves(true);
        _pendingCommands.offer("manual white");
    }

    /** new BlackAI. */
    private void newBlackeAI(String dummy) {
        blackAI = true;
        _widget.bManual(false);
        _widget.acceptingMoves(false);
        _pendingCommands.offer("auto black");
    }

    /** new BlackManual. */
    private void newBlackManual(String dummy) {
        blackAI = false;
        _widget.bManual(true);
        _widget.acceptingMoves(true);
        _pendingCommands.offer("manual black");
    }

    /** new ViewWhite. */
    private void newViewWhite(String dummy) {
        if (whiteAI) {
            showMessage("White is an AI", "White", "error");
        } else {
            showMessage("White is Manual", "White", "error");
        }
    }

    /** new ViewBlack. */
    private void newViewBlack(String dummy) {
        if (blackAI) {
            showMessage("Black is an AI", "White", "error");
        } else {
            showMessage("Black is Manual", "White", "error");
        }
    }

    /** undo.
     *
     * @param dummy dum
     */
    private void newUndo(String dummy) {
        _pendingCommands.offer("undo");
    }

    /** Return the next command from our widget, waiting for it as necessary.
     *  The BoardWidget uses _pendingCommands to queue up moves that it
     *  receives.  Thie class uses _pendingCommands to queue up commands that
     *  are generated by clicking on menu items. */
    String readCommand() {
        try {
            _widget.setMoveCollection(true);
            String cmnd = _pendingCommands.take();
            _widget.setMoveCollection(false);
            return cmnd;
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    @Override
    public void update(Board board) {
        _widget.update(board);
    }

    @Override
    public void reportError(String fmt, Object... args) {
        showMessage(String.format(fmt, args), "Amazons Error", "error");
    }

    @Override
    public void reportNote(String fmt, Object... args) {
        showMessage(String.format(fmt, args), "Amazons Message", "information");
    }

    @Override
    public void reportMove(Move unused) {
    }

    /** The board widget. */
    private BoardWidget _widget;

    /** check if white is AI. */
    private boolean whiteAI = false;

    /** check if black is AI. */
    private boolean blackAI = true;

    /** Queue of pending commands resulting from menu clicks and moves on the
     *  board.  We use a blocking queue because the responses to clicks
     *  on the board and on menus happen in parallel to the methods that
     *  call readCommand, which therefore needs to wait for clicks to happen. */
    private ArrayBlockingQueue<String> _pendingCommands =
        new ArrayBlockingQueue<>(5);

}