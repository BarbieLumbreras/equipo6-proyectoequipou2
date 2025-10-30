package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import java.util.ArrayList;
import java.util.List;

public class MoveValidator {
    private HexBoard board;
    private HexCell enPassantTarget; // Para captura al paso

    public MoveValidator(HexBoard board) {
        this.board = board;
    }

    /**
     * Verifica si un movimiento es válido
     */
    public boolean isValidMove(HexCell from, HexCell to) {
        if (from == null || to == null) return false;
        if (from.getPiece() == null) return false;
        if (from == to) return false;

        // No se puede mover a una celda ocupada por una pieza del mismo color
        if (to.getPiece() != null &&
                to.getPiece().getColor() == from.getPiece().getColor()) {
            return false;
        }

        ChessPiece piece = from.getPiece();

        switch (piece.getType()) {
            case PAWN:
                return isValidPawnMove(from, to, piece);
            case KNIGHT:
                return isValidKnightMove(from, to);
            case BISHOP:
                return isValidBishopMove(from, to);
            case ROOK:
                return isValidRookMove(from, to);
            case QUEEN:
                return isValidQueenMove(from, to);
            case KING:
                return isValidKingMove(from, to);
            default:
                return false;
        }
    }

    /**
     * Obtiene todos los movimientos válidos para una pieza
     */
    public List<HexCell> getValidMoves(HexCell from) {
        List<HexCell> validMoves = new ArrayList<>();

        if (from == null || from.getPiece() == null) return validMoves;

        for (HexCell cell : board.getAllCells().values()) {
            if (isValidMove(from, cell)) {
                validMoves.add(cell);
            }
        }

        return validMoves;
    }

    // ==================== PEÓN HEXAGONAL GLIŃSKI CORREGIDO ====================
    private boolean isValidPawnMove(HexCell from, HexCell to, ChessPiece pawn) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int distance = from.distanceTo(to);

        boolean isWhite = (pawn.getColor() == ChessPiece.PieceColor.WHITE);
        boolean isCapture = (to.getPiece() != null);
        boolean isEnPassant = isEnPassantCapture(from, to, pawn);

        // Movimiento normal (sin captura)
        if (!isCapture && !isEnPassant) {
            if (isWhite) {
                // PEÓN BLANCO - hacia el NORTE (r negativo)
                if (dr == -1 && dq == 0) return true;
                // Movimiento doble desde posición inicial
                if (!pawn.hasMoved() && dr == -2 && dq == 0) {
                    HexCell intermediate = board.getCell(from.getQ(), from.getR() - 1);
                    return intermediate != null && intermediate.getPiece() == null;
                }
            } else {
                // PEÓN NEGRO - hacia el SUR (r positivo)
                if (dr == 1 && dq == 0) return true;
                // Movimiento doble desde posición inicial
                if (!pawn.hasMoved() && dr == 2 && dq == 0) {
                    HexCell intermediate = board.getCell(from.getQ(), from.getR() + 1);
                    return intermediate != null && intermediate.getPiece() == null;
                }
            }
        }

        // CAPTURAS (Ortogonales según Wikipedia)
        if (isCapture || isEnPassant) {
            if (isWhite) {
                // Capturas ortogonales para peón blanco
                return (distance == 1) &&
                        (dr == -1 || dr == 0 || dr == 1) &&
                        (Math.abs(dq) == 1 || Math.abs(dr) == 1);
            } else {
                // Capturas ortogonales para peón negro
                return (distance == 1) &&
                        (dr == -1 || dr == 0 || dr == 1) &&
                        (Math.abs(dq) == 1 || Math.abs(dr) == 1);
            }
        }

        return false;
    }

    // ==================== CAPTURA AL PASO ====================
    private boolean isEnPassantCapture(HexCell from, HexCell to, ChessPiece pawn) {
        if (enPassantTarget == null) return false;

        boolean isWhite = (pawn.getColor() == ChessPiece.PieceColor.WHITE);
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();

        // Verificar si el movimiento es una captura al paso
        if (isWhite) {
            return (dr == -1 && Math.abs(dq) == 1) && to.equals(enPassantTarget);
        } else {
            return (dr == 1 && Math.abs(dq) == 1) && to.equals(enPassantTarget);
        }
    }

    public void setEnPassantTarget(HexCell target) {
        this.enPassantTarget = target;
    }

    public HexCell getEnPassantTarget() {
        return enPassantTarget;
    }

    // ==================== VERIFICACIÓN DE PROMOCIÓN ====================
    public boolean shouldPromotePawn(HexCell pawnCell) {
        if (pawnCell.getPiece() == null || pawnCell.getPiece().getType() != ChessPiece.PieceType.PAWN) {
            return false;
        }

        boolean isWhite = (pawnCell.getPiece().getColor() == ChessPiece.PieceColor.WHITE);
        int r = pawnCell.getR();

        // Peón blanco llega a la fila r = -5 (fila 11 en notación de Gliński)
        // Peón negro llega a la fila r = 5 (fila 1 en notación de Gliński)
        return (isWhite && r == -5) || (!isWhite && r == 5);
    }

    // ==================== CABALLO HEXAGONAL ====================
    private boolean isValidKnightMove(HexCell from, HexCell to) {
        int dq = Math.abs(to.getQ() - from.getQ());
        int dr = Math.abs(to.getR() - from.getR());
        int ds = Math.abs(to.getS() - from.getS());

        // Movimientos de caballo en hexagonal (12 posibles)
        return (dq == 2 && dr == 1 && ds == 1) ||
                (dq == 1 && dr == 2 && ds == 1) ||
                (dq == 1 && dr == 1 && ds == 2) ||
                (dq == 2 && dr == 2 && ds == 0) ||
                (dq == 2 && dr == 0 && ds == 2) ||
                (dq == 0 && dr == 2 && ds == 2);
    }

    // ==================== ALFIL HEXAGONAL ====================
    private boolean isValidBishopMove(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = to.getS() - from.getS();

        // El alfil se mueve en las 3 direcciones diagonales del hexagonal
        boolean isDiagonal = (dq == 0 && dr != 0 && ds != 0) ||
                (dr == 0 && dq != 0 && ds != 0) ||
                (ds == 0 && dq != 0 && dr != 0);

        if (!isDiagonal) return false;

        return isPathClear(from, to);
    }

    // ==================== TORRE HEXAGONAL ====================
    private boolean isValidRookMove(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = to.getS() - from.getS();

        // La torre se mueve en las 3 direcciones rectas del hexagonal
        boolean isStraight = (dr == 0 && ds == 0 && dq != 0) ||
                (dq == 0 && ds == 0 && dr != 0) ||
                (dq == 0 && dr == 0 && ds != 0);

        if (!isStraight) return false;

        return isPathClear(from, to);
    }

    // ==================== REINA HEXAGONAL ====================
    private boolean isValidQueenMove(HexCell from, HexCell to) {
        return isValidRookMove(from, to) || isValidBishopMove(from, to);
    }

    // ==================== REY HEXAGONAL ====================
    private boolean isValidKingMove(HexCell from, HexCell to) {
        int dq = Math.abs(to.getQ() - from.getQ());
        int dr = Math.abs(to.getR() - from.getR());
        int ds = Math.abs(to.getS() - from.getS());

        // El rey se mueve una casilla en cualquier dirección
        return Math.max(Math.max(dq, dr), ds) == 1;
    }

    // ==================== UTILIDADES ====================

    /**
     * Verifica si el camino entre dos celdas está despejado
     */
    private boolean isPathClear(HexCell from, HexCell to) {
        int distance = hexDistance(from, to);
        if (distance <= 1) return true;

        // Calcular dirección
        int stepQ = Integer.compare(to.getQ() - from.getQ(), 0);
        int stepR = Integer.compare(to.getR() - from.getR(), 0);

        // Verificar cada celda intermedia
        for (int i = 1; i < distance; i++) {
            int q = from.getQ() + stepQ * i;
            int r = from.getR() + stepR * i;

            HexCell cell = board.getCell(q, r);
            if (cell == null || cell.getPiece() != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Obtiene la distancia hexagonal entre dos celdas
     */
    private int hexDistance(HexCell from, HexCell to) {
        return (Math.abs(from.getQ() - to.getQ()) +
                Math.abs(from.getR() - to.getR()) +
                Math.abs(from.getS() - to.getS())) / 2;
    }
}