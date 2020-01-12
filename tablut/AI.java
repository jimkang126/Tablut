package tablut;

import java.util.List;

import static java.lang.Math.*;

import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Young Hoon Kang
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        if (_myPiece == WHITE || _myPiece == KING) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else if (_myPiece == BLACK) {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0) {
            return staticScore(board);
        } else {
            if (sense == 1) {
                List<Move> allMoves = board.legalMoves(WHITE);
                int maxEval = -INFTY;
                for (Move move: allMoves) {
                    Board b = new Board(board);
                    b.makeMove(move);
                    int eval = findMove(b, depth - 1, false, -1, alpha, beta);
                    maxEval = max(maxEval, eval);
                    alpha = max(alpha, eval);
                    if (saveMove && (_lastFoundMove == null
                            || eval == maxEval)) {
                        _lastFoundMove = move;
                    }
                    if (board.winner() == WHITE) {
                        return WINNING_VALUE;
                    }
                    if (board.winner() == BLACK) {
                        return -WINNING_VALUE;
                    }
                    if (beta <= alpha) {
                        break;
                    }
                }
                return maxEval;
            } else {
                List<Move> allMoves = board.legalMoves(BLACK);
                int minEval = INFTY;
                for (Move move: allMoves) {
                    Board b = new Board(board);
                    b.makeMove(move);
                    int eval = findMove(b, depth - 1, false, 1, alpha, beta);
                    minEval = min(minEval, eval);
                    beta = min(beta, eval);
                    if (saveMove && (_lastFoundMove == null
                            || eval == minEval)) {
                        _lastFoundMove = move;
                    }
                    if (board.winner() == WHITE) {
                        return WINNING_VALUE;
                    }
                    if (board.winner() == BLACK) {
                        return -WINNING_VALUE;
                    }
                    if (beta <= alpha) {
                        break;
                    }
                }
                return minEval;
            }
        }
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        int x = board.moveCount();
        if (x > 15) {
            return 10;
        }
        if (x > 10) {
            return 7;
        }
        if (x > 5) {
            return 5;
        }
        return 2;
    }
    /** Check distance between king and regular piece.
     * @param kRow = king's row
     * @param kCol = king's col
     * @param row = piece's row
     * @param col = piece's col
     * @return distance between the two pieces */
    private int distance(int kRow, int kCol, int row, int col) {
        return (int) Math.sqrt(Math.pow((kRow - row), 2)
                + Math.pow(kCol - col, 2));
    }
    /** check if a square has a black piece.
     * @param pos = the square's position
     * @param board = the board
     * @return 1 if pos is black and 0 if not */
    private int checkSquareBlack(Square pos, Board board) {
        if (pos != null && board.get(pos) == BLACK) {
            return 1;
        } else {
            return 0;
        }
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Square kingPos = board.kingPosition();
        if (board.winner() == WHITE) {
            return WINNING_VALUE;
        } else if (board.winner() == BLACK) {
            return -WINNING_VALUE;
        } else if (kingPos.rookMove(0, 1).isEdge()
                || kingPos.rookMove(1, 1).isEdge()
                || kingPos.rookMove(2, 1).isEdge()
                || kingPos.rookMove(3, 1).isEdge()) {
            return WILL_WIN_VALUE;
        } else if (board.repeatedPosition()) {
            if (_myPiece == WHITE || _myPiece == KING) {
                return -WINNING_VALUE;
            } else {
                return WINNING_VALUE;
            }
        } else {
            int kCol = kingPos.col();
            int kRow = kingPos.row();
            int[] cornerDists = {
                    distance(kRow, kCol, 0, 0),
                    distance(kRow, kCol, 0, 8),
                    distance(kRow, kCol, 8, 0),
                    distance(kRow, kCol, 8, 8)
            };
            int kingCornerDist = INFTY;
            for (int i = 0; i < cornerDists.length; i++) {
                if (cornerDists[i] < kingCornerDist) {
                    kingCornerDist = cornerDists[i];
                }
            }
            int whiteLegal = board.legalMoves(WHITE).size();
            int blackLegal = board.legalMoves(BLACK).size();

            Square north = kingPos.rookMove(0, 1);
            Square east = kingPos.rookMove(1, 1);
            Square south = kingPos.rookMove(2, 1);
            Square west = kingPos.rookMove(3, 1);
            Square northEast = north.rookMove(1, 1);
            Square northWest = north.rookMove(3, 1);
            Square southEast = south.rookMove(1, 1);
            Square southWest = south.rookMove(3, 1);
            Square[] kingNeighourSquares = {north, east, south, west,
                northWest, northEast, southWest, southEast};
            int kingNeighours = 0;
            for (int i = 0; i < kingNeighourSquares.length; i++) {
                kingNeighours += checkSquareBlack(kingNeighourSquares[i],
                        board);
            }
            int whiteScore = (9 - kingCornerDist) * 3
                    + (whiteLegal - blackLegal);
            int blackScore = (kingNeighours * 3) + (blackLegal - whiteLegal);
            return whiteScore - blackScore;
        }
    }
}
