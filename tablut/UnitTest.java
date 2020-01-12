package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author Young Hoon Kang
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test as a placeholder for real ones. */
    @Test
    public void initTest() {
        Board board = new Board();
        System.out.print(board);
        System.out.println(board.encodedBoard());

    }
    @Test
    public void checkGet() {
        Board board = new Board();
        assertEquals("WHITE", board.get(3, 4).name());
        assertEquals("BLACK", board.get(5, 8).name());
        assertEquals("KING", board.get(4, 4).name());
        assertEquals("EMPTY", board.get(3, 2).name());
    }

    @Test
    public void isUnblockedTest() {
        Board board = new Board();
        Square x = Square.sq(4, 4);
        Square y = Square.sq(4, 8);
        assertFalse(board.isUnblockedMove(x, y));
        Square i = Square.sq(6, 5);
        board.put(Piece.BLACK, i);
        Square a = Square.sq(4, 5);
        Square b = Square.sq(7, 5);
        assertFalse(board.isUnblockedMove(a, b));
        Square j = Square.sq(4, 3);
        Square k = Square.sq(7, 3);
        assertTrue(board.isUnblockedMove(j, k));
        Square h = Square.sq(4, 7);
        Square l = Square.sq(8, 7);
        assertTrue(board.isUnblockedMove(h, l));
    }
    @Test
    public void makeMoveTest() {
        Board board = new Board();
        Square a = Square.sq(4, 1);
        Square b = Square.sq(7, 1);
        board.makeMove(a, b);
        assertEquals("EMPTY", board.get(4, 1).name());
        assertEquals("BLACK", board.get(7, 1).name());
        Square x = Square.sq(5, 4);
        Square y = Square.sq(5, 7);
        board.makeMove(x, y);
        assertEquals("EMPTY", board.get(5, 4).name());
        assertEquals("WHITE", board.get(5, 7).name());
    }
    @Test
    public void undoTest() {
        Board board = new Board();
        System.out.print(board);
        Square a = Square.sq(4, 1);
        Square b = Square.sq(7, 1);
        Square z = Square.sq(5, 1);
        Square x = Square.sq(2, 4);
        Square y = Square.sq(2, 6);
        board.makeMove(a, b);
        System.out.print(board);
        board.undo();
        System.out.print(board);
        board.makeMove(x, y);
        System.out.print(board);
        board.makeMove(a, z);
        System.out.print(board);
        board.undo();
        System.out.print(board);
        board.undo();
        System.out.print(board);
    }
    @Test
    public void kingTest() {
        Board board = new Board();
        Square a = Square.sq(3, 8);
        Square b = Square.sq(1, 8);
        board.makeMove(a, b);
        Square x = Square.sq(5, 4);
        Square y = Square.sq(5, 1);
        board.makeMove(x, y);
        Square c = Square.sq(1, 6);
        board.makeMove(b, c);
        Square k = Square.sq(4, 4);
        board.makeMove(k, x);
        Square l = Square.sq(3, 6);
        board.makeMove(c, l);
        Square last = Square.sq(5, 7);
        board.makeMove(x, last);
        System.out.print(board);
        board.put(Piece.EMPTY, board.kingPosition());
        System.out.print(board);
    }
    @Test
    public void anotherCaptureTest() {
        Board board = new Board();
        Square a = Square.sq(0, 5);
        Square b = Square.sq(0, 7);
        board.makeMove(a, b);
        System.out.println(board);
        Square c = Square.sq(5, 4);
        Square d = Square.sq(5, 6);
        board.makeMove(c, d);
        System.out.println(board);
        Square e = Square.sq(7, 4);
        Square f = Square.sq(7, 8);
        board.makeMove(e, f);
        System.out.println(board);
        Square g = Square.sq(6, 4);
        Square h = Square.sq(6, 8);
        board.makeMove(g, h);
        System.out.println(board);
        Square i = Square.sq(5, 0);
        Square j = Square.sq(8, 0);
        board.makeMove(i, j);
        Square k = Square.sq(4, 4);
        Square l = Square.sq(7, 4);
        board.makeMove(k, l);
        Square m = Square.sq(8, 5);
        Square n = Square.sq(6, 5);
        board.makeMove(m, n);
        Square o = Square.sq(5, 6);
        Square p = Square.sq(5, 4);
        board.makeMove(o, p);
        System.out.println(board);
        Square q = Square.sq(8, 4);
        Square r = Square.sq(8, 6);
        board.makeMove(q, r);
        Square s = Square.sq(4, 2);
        Square t = Square.sq(0, 2);
        board.makeMove(s, t);
        Square u = Square.sq(6, 5);
        Square v = Square.sq(6, 4);
        board.makeMove(u, v);
        System.out.println(board);
        Square w = Square.sq(4, 3);
        Square x = Square.sq(4, 2);
        board.makeMove(w, x);
        System.out.print(board);
        Square y = Square.sq(5, 8);
        Square z = Square.sq(5, 5);
        board.makeMove(y, z);
        Square aa = Square.sq(0, 2);
        Square bb = Square.sq(1, 2);
        board.makeMove(aa, bb);
        System.out.println(board);
        Square cc = Square.sq(8, 3);
        Square dd = Square.sq(5, 3);
        board.makeMove(cc, dd);
        System.out.println(board);
    }
    @Test
    public void lastCaptureTest() {
        Board board = new Board();
        Square a = Square.sq(0, 5);
        Square b = Square.sq(0, 7);
        board.makeMove(a, b);
        Square c = Square.sq(5, 4);
        Square d = Square.sq(5, 6);
        board.makeMove(c, d);
        Square e = Square.sq(7, 4);
        Square f = Square.sq(7, 8);
        board.makeMove(e, f);
        Square g = Square.sq(6, 4);
        Square h = Square.sq(6, 8);
        board.makeMove(g, h);
        Square i = Square.sq(8, 3);
        Square j = Square.sq(8, 0);
        board.makeMove(i, j);
        Square k = Square.sq(4, 4);
        Square l = Square.sq(7, 4);
        board.makeMove(k, l);
        Square m = Square.sq(5, 0);
        Square n = Square.sq(5, 4);
        board.makeMove(m, n);
        System.out.println(board);
        Square o = Square.sq(6, 8);
        Square p = Square.sq(6, 4);
        board.makeMove(o, p);
        System.out.println(board);
    }

    @Test
    public void uniquetestcapture() {
        Board board = new Board();
        Square a = Square.sq(5, 0);
        Square b = Square.sq(5, 3);
        board.makeMove(a, b);
        System.out.println(board);
        Square x = Square.sq(3, 4);
        Square y = Square.sq(3, 5);
        board.makeMove(x, y);
        System.out.println(board);
        Square c = Square.sq(5, 8);
        Square d = Square.sq(5, 5);
        board.makeMove(c, d);
        System.out.println(board);

    }
    @Test
    public void another() {
        Board board = new Board();
        Square a = Square.sq(5, 0);
        Square b = Square.sq(5, 3);
        board.makeMove(a, b);
        System.out.println(board);
        Square x = Square.sq(6, 4);
        Square y = Square.sq(6, 7);
        board.makeMove(x, y);
        System.out.println(board);
        Square c = Square.sq(7, 4);
        Square d = Square.sq(6, 4);
        board.makeMove(c, d);
        System.out.println(board);
        Square e = Square.sq(2, 4);
        Square f = Square.sq(2, 5);
        board.makeMove(e, f);
        System.out.println(board);
        Square g = Square.sq(5, 8);
        Square h = Square.sq(5, 5);
        board.makeMove(g, h);
        System.out.println(board);
    }
    @Test
    public void undocapture() {
        Board board = new Board();
        Square a = Square.sq(4, 7);
        Square b = Square.sq(6, 7);
        board.makeMove(a, b);
        Square c = Square.sq(6, 4);
        Square d = Square.sq(6, 6);
        board.makeMove(c, d);
        Square e = Square.sq(5, 0);
        Square f = Square.sq(6, 0);
        board.makeMove(e, f);
        Square g = Square.sq(2, 4);
        Square h = Square.sq(2, 5);
        board.makeMove(g, h);
        System.out.println(board);
        Square i = Square.sq(6, 0);
        Square j = Square.sq(6, 5);
        board.makeMove(i, j);
        System.out.println(board);
        board.undo();
        System.out.println(board);
    }
    @Test
    public void anothercap() {
        Board board = new Board();
        Square a = Square.sq(3, 0);
        Square b = Square.sq(2, 0);
        board.makeMove(a, b);
        System.out.println(board);
        Square c = Square.sq(4, 5);
        Square d = Square.sq(7, 5);
        board.makeMove(c, d);
        System.out.println(board);
        Square e = Square.sq(4, 7);
        Square f = Square.sq(3, 7);
        board.makeMove(e, f);
        System.out.println(board);
        Square g = Square.sq(4, 4);
        Square h = Square.sq(4, 5);
        board.makeMove(g, h);
        System.out.println(board);
        Square i = Square.sq(3, 7);
        Square j = Square.sq(3, 5);
        board.makeMove(i, j);
        System.out.println(board);
    }
}


