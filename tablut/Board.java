package tablut;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Formatter;

import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;



/** The state of a Tablut Game.
 *  @author Young Hoon Kang
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initial positions of empty Squares. */
    static final Square[] INITIAL_EMPTY_SQUARES = {
            sq(0, 0), sq(0, 1), sq(0, 2), sq(0, 6),
            sq(0, 7), sq(0, 8), sq(1, 0), sq(1, 1),
            sq(1, 2), sq(1, 3), sq(1, 5), sq(1, 6),
            sq(1, 7), sq(1, 8), sq(2, 0), sq(2, 1),
            sq(2, 2), sq(2, 3), sq(2, 5), sq(2, 6),
            sq(2, 7), sq(2, 8), sq(3, 1), sq(3, 2),
            sq(3, 3), sq(3, 5), sq(3, 6), sq(3, 7),
            sq(5, 1), sq(5, 2), sq(5, 3), sq(5, 5),
            sq(5, 6), sq(5, 7), sq(6, 0), sq(6, 1),
            sq(6, 2), sq(6, 3), sq(6, 5), sq(6, 6),
            sq(6, 7), sq(6, 8), sq(7, 0), sq(7, 1),
            sq(7, 2), sq(7, 3), sq(7, 5), sq(7, 6),
            sq(7, 7), sq(7, 8), sq(8, 0), sq(8, 1),
            sq(8, 2), sq(8, 6), sq(8, 7), sq(8, 8)
    };

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
        if (model == this) {
            return;
        }
        init();
        _board = new Piece[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                _board[c][r] = model.get(c, r);
            }
        }
        _moveCount = 0;
        _winner = null;
        _turn = model.turn();
        _repeated = false;
        encodedboards = new HashSet<>();
        undos = model.undos;
        sstack = model.sstack;
        pstack = model.pstack;
        capture = false;
        _lim = model._lim;
    }

    /** Clears the board to the initial position. */
    void init() {
        _board = new Piece[SIZE][SIZE];
        _moveCount = 0;
        _winner = null;
        _turn = BLACK;
        sstack = new Stack<Square>();
        pstack = new Stack<Piece>();
        undos = new Stack<Move>();
        encodedboards = new HashSet<String>();
        for (int r = 0;  r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (c == 4 && r == 4) {
                    _board[c][r] = KING;
                }
                for (Square sq: INITIAL_ATTACKERS) {
                    if (sq.col() == c && sq.row() == r) {
                        _board[c][r] = BLACK;
                    }
                }
                for (Square sq2: INITIAL_DEFENDERS) {
                    if (sq2.col() == c && sq2.row() == r) {
                        _board[c][r] = WHITE;
                    }
                }
                for (Square sq3: INITIAL_EMPTY_SQUARES) {
                    if (sq3.col() == c && sq3.row() == r) {
                        _board[c][r] = EMPTY;
                    }
                }
            }
        }
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * @param n = lim */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new IllegalArgumentException();
        }
        _lim = n;
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        String mine = this.encodedBoard();
        if (encodedboards.contains(mine.substring(1))) {
            _repeated = true;
            _winner = turn().opponent();
        } else {
            encodedboards.add(mine.substring(1));
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        Square kp = null;
        for (int r = 0;  r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (_board[c][r].name().equals("KING")) {
                    kp = sq(c, r);
                }
            }
        }
        return kp;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        Piece x = _board[col][row];
        return x;
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(row - '1', col - 'a');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board[s.col()][s.row()] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        pstack.push(get(s));
        sstack.push(s);
        put(p, s);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        boolean isit = true;
        int dir = from.direction(to);
        Square curr = from.rookMove(dir, 1);
        if (from.row() == to.row() && from.col() == to.col()) {
            isit = false;
        }
        if (!from.isRookMove(to)) {
            isit = false;
        }
        if (from.isRookMove(to)) {
            while (curr != to) {
                if (_board[curr.col()][curr.row()] != EMPTY) {
                    isit = false;
                }
                curr = curr.rookMove(dir, 1);
            }
        }
        return isit;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from) == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        Piece there = _board[to.col()][to.row()];
        Piece here = get(from.col(), from.row());
        if (to == THRONE && get(from) != KING) {
            return false;
        }
        if (!isUnblockedMove(from, to)) {
            return false;
        }
        if (there != EMPTY) {
            return false;
        }
        if (here == _turn || (here == KING && _turn.equals(WHITE))) {
            return isUnblockedMove(from, to);
        }
        if (!isLegal(from)) {
            return false;
        }

        return true;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        Piece moving = _board[from.col()][from.row()];
        revPut(moving, to);
        put(EMPTY, from);
        undos.push(mv(from, to));
        capture = false;
        if (get(THRONE) != KING && get(NTHRONE) != KING
                && get(STHRONE) != KING && get(WTHRONE) != KING
                && get(ETHRONE) != KING) {
            for (Square sq: possibleremove(to)) {
                capture(to, sq);
            }
        }
        if (possibleremove(to) != null) {
            for (Square sq: possibleremove(to)) {
                capture(to, sq);
            }
        }
        if (kingPosition() == null) {
            _winner = BLACK;
        }
        if (_winner == null) {
            int kc = kingPosition().col();
            int kr = kingPosition().row();
            if ((kc == 0 && kr == 0) || (kc == 0 && kr == 8)
                    || (kc == 8 && kr == 0) || (kc == 8 && kr == 8)) {
                _winner = WHITE;
            }
        }
        _moveCount += 1;
        checkRepeated();
        _turn = turn().opponent();
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }
    /** Checking for pieces that can be removed when
     * the king is in the throne.
     * @param to = the square that is moved. */
    void throne(Square to) {
        if (get(THRONE) == KING) {
            if (get(NTHRONE) == WHITE && get(STHRONE) == BLACK
                    && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                if (get(NTHRONE.between(to)) == WHITE && get(to) == BLACK) {
                    revPut(EMPTY, NTHRONE.between(to));
                    capture = true;
                }
            }
            if (get(NTHRONE) == BLACK && get(STHRONE) == WHITE
                    && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                if (get(STHRONE.between(to)) == WHITE && get(to) == BLACK) {
                    revPut(EMPTY, STHRONE.between(to));
                    capture = true;
                }
            }
            if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                    && get(ETHRONE) == WHITE && get(WTHRONE) == BLACK) {
                if (get(ETHRONE.between(to)) == WHITE && get(to) == BLACK) {
                    revPut(EMPTY, ETHRONE.between(to));
                    capture = true;
                }
            }
            if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                    && get(ETHRONE) == BLACK && get(WTHRONE) == WHITE) {
                if (get(WTHRONE.between(to)) == WHITE && get(to) == BLACK) {
                    revPut(EMPTY, WTHRONE.between(to));
                    capture = true;
                }
            }
            if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                    && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                revPut(EMPTY, kingPosition());
                capture = true;
                _winner = BLACK;
            }
        }
    }
    /** Checking for pieces that can be removed when the
     * king is not in the throne.
     * @param to = the square that is moved. */
    void throneEmpty(Square to) {
        if (get(THRONE) == EMPTY) {
            if (_turn == BLACK) {
                if (get(NTHRONE) == KING) {
                    if (get(THRONE.rookMove(0, 2)) == BLACK
                            && get(NTHRONE.rookMove(1, 1)) == BLACK
                            && get(NTHRONE.rookMove(3, 1)) == BLACK) {
                        revPut(EMPTY, NTHRONE);
                        capture = true;
                        _winner = turn();
                    }
                }
                if (get(STHRONE) == KING) {
                    if (get(THRONE.rookMove(2, 2)) == BLACK
                            && get(STHRONE.rookMove(3, 1)) == BLACK
                            && get(STHRONE.rookMove(1, 1)) == BLACK) {
                        revPut(EMPTY, STHRONE);
                        capture = true;
                        _winner = turn();
                    }
                }
                if (get(ETHRONE) == KING) {
                    if (get(THRONE.rookMove(1, 2)) == BLACK
                            && get(ETHRONE.rookMove(0, 1)) == BLACK
                            && get(ETHRONE.rookMove(2, 1)) == BLACK) {
                        revPut(EMPTY, ETHRONE);
                        capture = true;
                        _winner = turn();
                    }
                }
                if (get(WTHRONE) == KING) {
                    if (get(THRONE.rookMove(3, 2)) == BLACK
                            && get(WTHRONE.rookMove(0, 1)) == BLACK
                            && get(WTHRONE.rookMove(2, 1)) == BLACK) {
                        revPut(EMPTY, WTHRONE);
                        capture = true;
                        _winner = turn();
                    }
                }
                if (get(THRONE.between(to)) == WHITE) {
                    revPut(EMPTY, THRONE.between(to));
                    capture = true;
                }
            }
            if (_turn == WHITE) {
                if (get(THRONE.between(to)) == BLACK) {
                    revPut(EMPTY, THRONE.between(to));
                    capture = true;
                }
            }
        }
    }
    /** Helper function to check which squares either could be
     * removed or should be removed.
     * @return = a list of moves i can remove
     * @param to = the square that is moved. */
    List<Square> possibleremove(Square to) {
        List<Square> possible = new ArrayList<>();
        List<Square> remove = new ArrayList<>();
        throne(to);
        throneEmpty(to);
        Square first = to.rookMove(0, 2);
        if (first != null) {
            possible.add(first);
        }
        Square second = to.rookMove(1, 2);
        if (second != null) {
            possible.add(second);
        }
        Square third = to.rookMove(2, 2);
        if (third != null) {
            possible.add(third);
        }
        Square fourth = to.rookMove(3, 2);
        if (fourth != null) {
            possible.add(fourth);
        }
        for (Square sq : possible) {
            if (get(THRONE) != KING && get(NTHRONE) != KING
                    && get(STHRONE) != KING && get(WTHRONE) != KING
                    && get(ETHRONE) != KING && get(to.between(sq)) == KING
                    && get(to) == BLACK && get(sq) == BLACK) {
                remove.add(sq);
                revPut(EMPTY, to.between(sq));
                capture = true;
                _winner = BLACK;
            }
            if (get(to.between(sq)) == _turn.opponent() && (get(to) == _turn)
                    && (get(sq) == _turn)) {
                revPut(EMPTY, to.between(sq));
                capture = true;
                remove.add(sq);
            }
            if (get(to.between(sq)) == BLACK && (get(to) == WHITE
                    || get(to) == KING) && (get(sq) == WHITE
                    || get(sq) == KING)) {
                revPut(EMPTY, to.between(sq));
                capture = true;
                remove.add(sq);
            }
        }
        return remove;
    }
    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Piece p0 = get(sq0.col(), sq0.row());
        Piece p2 = get(sq2.col(), sq2.row());
        Square next = sq0.between(sq2);
        Piece between = get(next.col(), next.row());
        Square throneS = THRONE;
        Piece throneP = get(THRONE);
        if (get(THRONE) == KING) {
            if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                    && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                revPut(EMPTY, kingPosition());
                capture = true;
                _winner = BLACK;
            }
        }
        if (p0 == _turn && p2 == _turn && between == _turn.opponent()) {
            revPut(EMPTY, sq0.between(sq2));
            capture = true;
        }
        if (get(THRONE) == EMPTY) {
            throneP = _turn;
            if (get(throneS.between(sq2)) == _turn.opponent()) {
                revPut(EMPTY, throneS.between(sq2));
                capture = true;
            }
            if (get(NTHRONE) != KING && get(STHRONE) != KING
                    && get(ETHRONE) != KING && get(WTHRONE) != KING) {
                if (get(sq0.between(sq2)) == KING && p0 == BLACK
                        && p2 == BLACK) {
                    revPut(EMPTY, kingPosition());
                    capture = true;
                    _winner = BLACK;
                }
            }
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            _moveCount -= 1;
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (!_repeated || _moveCount > 0) {
            if (capture) {
                Piece ppop = pstack.pop();
                Square spop = sstack.pop();
                put(ppop, spop);
                capture = false;
            }
            String undoing = this.encodedBoard();
            encodedboards.remove(undoing);
            Move undid = undos.pop();
            Square from = undid.from();
            Piece to1 = _board[undid.to().col()][undid.to().row()];
            put(to1, from);
            put(EMPTY, undid.to());
        }
        _repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        undos = new Stack<>();
        pstack = new Stack<>();
        sstack = new Stack<>();
        encodedboards = new HashSet<>();
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        List<Move> listOfMoves = new ArrayList<Move>();
        HashSet<Square> locations = pieceLocations(side);
        for (Square s:locations) {
            for (int c = 0; c < SIZE; c++) {
                if (isLegal(s, sq(c, s.row()))) {
                    listOfMoves.add(mv(s, sq(c, s.row())));
                }
            }
            for (int r = 0;  r < SIZE; r++) {
                if (isLegal(s, sq(s.col(), r))) {
                    listOfMoves.add(mv(s, sq(s.col(), r)));
                }
            }
        }
        if (listOfMoves.size() == 0) {
            _winner = _turn.opponent();
        }
        return listOfMoves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        if (legalMoves(side).size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> locations = new HashSet<Square>();
        for (int r = 0;  r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (side.equals(_board[c][r])) {
                    locations.add(sq(c, r));
                }
            }
        }
        return locations;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** A Hashset of all the encodedboard strings of each moves. */
    private HashSet<String> encodedboards;
    /** A Stack of all the moves. */
    private Stack<Move> undos;
    /** A Stack of all the pieces. */
    private Stack<Piece> pstack;
    /** A Stack of all the squares. */
    private Stack<Square> sstack;
    /** a 2-D array of the piece as the board. */
    private Piece[][] _board;
    /** The max number of moves a player can make before ending the game. */
    private int _lim;
    /** To check is a move has been captured or not. */
    private boolean capture;
}
