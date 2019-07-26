package amazons;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static amazons.Piece.EMPTY;
import static amazons.Piece.SPEAR;
import static amazons.Piece.WHITE;
import static org.junit.Assert.assertEquals;

public class BoardTest {

    @Test
    public void testDirection() {
        Board b1 = new Board();
        Square a1 = b1.getBoard()[0][3];
        Square c1 = b1.getBoard()[6][3];
        assertEquals(0, a1.direction(c1));

        Square c2 = b1.getBoard()[3][6];
        assertEquals(1, a1.direction(c2));

        Square c3 = b1.getBoard()[0][5];
        assertEquals(2, a1.direction(c3));

        Square a2 = b1.getBoard()[9][3];
        Square c4 = b1.getBoard()[5][7];
        assertEquals(3, a2.direction(c4));

        Square c5 = b1.getBoard()[4][3];
        assertEquals(4, a2.direction(c5));
    }

    @Test
    public void testQueenmove() {
        Board b1 = new Board();
        Square q1 = b1.getBoard()[6][0];
        Square r1 = q1.queenMove(3, 4);
        assertEquals(4, r1.col());
        assertEquals(2, r1.row());

        Square q2 = b1.getBoard()[9][6];
        Square r2 = q2.queenMove(5, 5);
        assertEquals(1, r2.col());
        assertEquals(4, r2.row());

        Square q3 = b1.getBoard()[6][9];
        Square r3 = q3.queenMove(6, 4);
        assertEquals(5, r3.col());
        assertEquals(6, r3.row());
    }

    @Test
    public void testReachableFromIterator() {
        Board b1 = new Board();
        Square q1 = b1.getBoard()[0][3];
        Iterator<Square> m1 = b1.reachableFrom(q1, null);
        while (m1.hasNext()) {
            Square n = m1.next();
        }
    }

    @Test
    public void testLegalMoveIterator() {
        Board b1 = new Board();
        makeSmile(b1);
        b1.setQueenposb(new ArrayList<>());
        b1.setQueenposw(new ArrayList<>());
        for (int lip = 3; lip < 7; lip += 1) {
            b1.getQueenposw().add(b1.getBoard()[2][lip]);
        }
        b1.getQueenposw().add(b1.getBoard()[3][2]);
        b1.getQueenposw().add(b1.getBoard()[3][7]);
        Iterator<Move> m1 = b1.legalMoves(Piece.WHITE);
    }

    private void makeSmile(Board b) {
        b.put(EMPTY, Square.sq(0, 3));
        b.put(EMPTY, Square.sq(0, 6));
        b.put(EMPTY, Square.sq(9, 3));
        b.put(EMPTY, Square.sq(9, 6));
        b.put(EMPTY, Square.sq(3, 0));
        b.put(EMPTY, Square.sq(3, 9));
        b.put(EMPTY, Square.sq(6, 0));
        b.put(EMPTY, Square.sq(6, 9));
        for (int col = 1; col < 4; col += 1) {
            for (int row = 6; row < 9; row += 1) {
                b.put(SPEAR, Square.sq(col, row));
            }
        }
        b.put(EMPTY, Square.sq(2, 7));
        for (int col = 6; col < 9; col += 1) {
            for (int row = 6; row < 9; row += 1) {
                b.put(SPEAR, Square.sq(col, row));
            }
        }
        b.put(EMPTY, Square.sq(7, 7));
        for (int lip = 3; lip < 7; lip += 1) {
            b.put(WHITE, Square.sq(lip, 2));
        }
        b.put(WHITE, Square.sq(2, 3));
        b.put(WHITE, Square.sq(7, 3));
    }

    @Test
    public void testIsLegal() {
        Board b1 = new Board();
        Square q1 = b1.getBoard()[6][9];
        Square t1 = b1.getBoard()[0][3];
        assertEquals(false, b1.isLegal(q1, t1));

        Square t2 = b1.getBoard()[4][4];
        assertEquals(false, b1.isLegal(q1, t2));

        Square t3 = b1.getBoard()[2][5];
        assertEquals(true, b1.isLegal(q1, t3));
        b1.getBoard()[3][6].setPiece(Piece.SPEAR);
        assertEquals(false, b1.isLegal(q1, t3));

    }
}
