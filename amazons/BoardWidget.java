package amazons;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import static amazons.Piece.*;
import static amazons.Square.sq;

/** A widget that displays an Amazons game.
 *  @author Joseph PArk
 */
class BoardWidget extends Pad {

    /* Parameters controlling sizes, speeds, colors, and fonts. */

    /** Colors of empty squares and grid lines. */
    static final Color
        SPEAR_COLOR = new Color(64, 64, 64),
        LIGHT_SQUARE_COLOR = new Color(238, 207, 161),
        DARK_SQUARE_COLOR = new Color(205, 133, 63);

    /** Locations of images of white and black queens. */
    private static final String
        WHITE_QUEEN_IMAGE = "wq4.png",
        BLACK_QUEEN_IMAGE = "bq4.png",
        SPEAR_IMAGE = "spear.png";

    /** Size parameters. */
    private static final int
        SQUARE_SIDE = 30,
        BOARD_SIDE = SQUARE_SIDE * 10;

    /** A graphical representation of an Amazons board that sends commands
     *  derived from mouse clicks to COMMANDS.  */
    BoardWidget(ArrayBlockingQueue<String> commands) {
        _commands = commands;
        setMouseHandler("click", this::mouseClicked);
        setPreferredSize(BOARD_SIDE, BOARD_SIDE);

        try {
            _whiteQueen = ImageIO.read(Utils.getResource(WHITE_QUEEN_IMAGE));
            _blackQueen = ImageIO.read(Utils.getResource(BLACK_QUEEN_IMAGE));
            _spear = ImageIO.read(Utils.getResource(SPEAR_IMAGE));
        } catch (IOException excp) {
            System.err.println("Could not read queen images.");
            System.exit(1);
        }
        _acceptingMoves = false;
    }

    /** Draw the bare board G.  */
    private void drawGrid(Graphics2D g) {
        paintboard = g;
        Color color = LIGHT_SQUARE_COLOR;
        g.setColor(color);
        for (int i = 0; i < BOARD_SIDE; i = i + SQUARE_SIDE) {
            for (int j = 0; j < BOARD_SIDE; j = j + SQUARE_SIDE) {
                g.fillRect(i, j, SQUARE_SIDE, SQUARE_SIDE);
                if (color == LIGHT_SQUARE_COLOR) {
                    color = DARK_SQUARE_COLOR;
                } else {
                    color = LIGHT_SQUARE_COLOR;
                }
                g.setColor(color);
            }
            if (color == LIGHT_SQUARE_COLOR) {
                color = DARK_SQUARE_COLOR;
            } else {
                color = LIGHT_SQUARE_COLOR;
            }
            g.setColor(color);
        }
        int[][] startposw = new int[][] {{3, 0}, {6, 0}, {0, 3}, {9, 3}};
        int[][] startposb = new int[][] {{3, 9}, {6, 9}, {0, 6}, {9, 6}};
        for (int i = 0; i < startposw.length; i += 1) {
            int[] cur = startposw[i];
            Square s = _board.getBoard()[cur[1]][cur[0]];
            drawQueen(g, s, WHITE);
        }
        for (int i = 0; i < startposb.length; i += 1) {
            int[] cur = startposb[i];
            Square s = _board.getBoard()[cur[1]][cur[0]];
            drawQueen(g, s, BLACK);
        }
    }

    /** Draws plain board.
     * @param g graph*/
    void drawPlain(Graphics2D g) {
        paintboard = g;
        Color color = LIGHT_SQUARE_COLOR;
        g.setColor(color);
        for (int i = 0; i < BOARD_SIDE; i = i + SQUARE_SIDE) {
            for (int j = 0; j < BOARD_SIDE; j = j + SQUARE_SIDE) {
                g.fillRect(i, j, SQUARE_SIDE, SQUARE_SIDE);
                if (color == LIGHT_SQUARE_COLOR) {
                    color = DARK_SQUARE_COLOR;
                } else {
                    color = LIGHT_SQUARE_COLOR;
                }
                g.setColor(color);
            }
            if (color == LIGHT_SQUARE_COLOR) {
                color = DARK_SQUARE_COLOR;
            } else {
                color = LIGHT_SQUARE_COLOR;
            }
            g.setColor(color);
        }
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        drawPlain(g);
        if (clicked.size() < 3) {
            for (Square s : clicked) {
                Color select = Color.YELLOW;
                g.setColor(select);
                g.fillRect(s.col() * SQUARE_SIDE, (9 - s.row())
                        * SQUARE_SIDE, SQUARE_SIDE, SQUARE_SIDE);
            }
        }
        for (int i = 0; i < _board.getBoard().length; i += 1) {
            for (int j = 0; j < _board.getBoard().length; j += 1) {
                Square cur = _board.getBoard()[i][j];
                if (cur.getPiece() == WHITE) {
                    drawQueen(g, cur, WHITE);
                }
                if (cur.getPiece() == BLACK) {
                    drawQueen(g, cur, BLACK);
                }
                if (cur.getPiece() == SPEAR) {
                    drawSpear(g, cur);
                }
            }
        }
    }

    /** Draw a queen for side PIECE at square S on G.  */
    private void drawQueen(Graphics2D g, Square s, Piece piece) {
        g.drawImage(piece == WHITE ? _whiteQueen : _blackQueen,
                    cx(s.col()) + 2, cy(s.row()) + 4, null);
    }

    /** draws spear.
     * @param g graphics
     * @param s Sqaure */
    private void drawSpear(Graphics2D g, Square s) {
        g.drawImage(_spear, cx(s.col()) + 2, cy(s.row()) + 4, null);
    }

    /** Handle a click on S. */
    private void click(Square s) {
        clicked.add(s);
        if (clicked.size() == 2) {
            if (clicked.get(0).row() == clicked.get(1).row()
                    && clicked.get(0).col() == clicked.get(1).col()) {
                clicked.clear();
            }
        }
        if (clicked.size() == 3) {
            Square from = clicked.get(0);
            Square to = clicked.get(1);
            Square spear = clicked.get(2);
            boolean chk1 = from.getPiece() == WHITE || from.getPiece() == BLACK;
            boolean chk2 = to.getPiece() == EMPTY;
            boolean chk3 = spear.getPiece() == EMPTY;
            if (from.row() == spear.row() && from.col() == spear.col()) {
                chk3 = true;
            }
            boolean chk4 = _board.isLegal(from, to)
                    && _board.isUnblockedMove(to, spear, from);
            boolean ch5 = from.getPiece() == WHITE;
            if (blackManual) {
                ch5 = (from.getPiece() == WHITE && _board.getTurn() == WHITE)
                        || (from.getPiece() == BLACK
                        && _board.getTurn() == BLACK);
            }
            if (chk1 && chk2 && chk3 && chk4 && ch5) {
                String froms = String.valueOf(from.toString().charAt(0))
                        + (from.row() + 1);
                String tos = String.valueOf(to.toString().charAt(0))
                        + (to.row() + 1);
                String spears = String.valueOf(spear.toString().charAt(0))
                        + (spear.row() + 1);
                String move = froms + "-" + tos + "(" + spears + ")";
                clicked.clear();
                _commands.add(move);
            } else {
                clicked.clear();
            }
        }
        repaint();
    }

    /** Handle mouse click event E. */
    private synchronized void mouseClicked(String unused, MouseEvent e) {
        int xpos = e.getX(), ypos = e.getY();
        int x = xpos / SQUARE_SIDE,
            y = (BOARD_SIDE - ypos) / SQUARE_SIDE;
        if (_acceptingMoves
            && x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
            click(sq(x, y));
        }
    }

    /** Revise the displayed board according to BOARD. */
    synchronized void update(Board board) {
        _board.copy(board);
        repaint();
    }

    /** Turn on move collection iff COLLECTING, and clear any current
     *  partial selection.   When move collection is off, ignore clicks on
     *  the board. */
    void setMoveCollection(boolean collecting) {
        _acceptingMoves = collecting;
        repaint();
    }

    /** Return x-pixel coordinate of the left corners of column X
     *  relative to the upper-left corner of the board. */
    private int cx(int x) {
        return x * SQUARE_SIDE;
    }

    /** Return y-pixel coordinate of the upper corners of row Y
     *  relative to the upper-left corner of the board. */
    private int cy(int y) {
        return (Board.SIZE - y - 1) * SQUARE_SIDE;
    }

    /** Return x-pixel coordinate of the left corner of S
     *  relative to the upper-left corner of the board. */
    private int cx(Square s) {
        return cx(s.col());
    }

    /** Return y-pixel coordinate of the upper corner of S
     *  relative to the upper-left corner of the board. */
    private int cy(Square s) {
        return cy(s.row());
    }

    /** gets board.
     * @return board */
    public Board getBoard() {
        return _board;
    }

    /** if accept moves.
     * @param b boolean */
    void acceptingMoves(boolean b) {
        if (b) {
            _acceptingMoves = true;
        } else {
            _acceptingMoves = false;
        }
    }

    /** if black manual.
     * @param b boolean*/
    void bManual(boolean b) {
        if (b) {
            blackManual = true;
        } else {
            blackManual = false;
        }
    }

    /** return patintboars.
     * @return paintboard */
    Graphics2D getPaintBoard() {
        return paintboard;
    }

    /** Queue on which to post move commands (from mouse clicks). */
    private ArrayBlockingQueue<String> _commands;
    /** Board being displayed. */
    private final Board _board = new Board();

    /** Image of white queen. */
    private BufferedImage _whiteQueen;
    /** Image of black queen. */
    private BufferedImage _blackQueen;
    /** Image of spear. */
    private BufferedImage _spear;

    /** True iff accepting moves from user. */
    private boolean _acceptingMoves;

    /** All clicks.*/
    private ArrayList<Square> clicked = new ArrayList<>();

    /** paintboard. */
    private Graphics2D paintboard;

    /** if black manual. */
    private boolean blackManual = false;
}
