package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import java.util.ArrayList;
import java.util.List;

public class MoveValidator {
    private HexBoard board;

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

    // ==================== PEÓN ====================
    private boolean isValidPawnMove(HexCell from, HexCell to, ChessPiece pawn) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();

        // Blancas avanzan "hacia arriba" en el tablero visual (r disminuye, q aumenta en diagonal)
        // Negras avanzan "hacia abajo" en el tablero visual (r aumenta, q disminuye en diagonal)

        boolean isWhite = (pawn.getColor() == ChessPiece.PieceColor.WHITE);

        // Movimiento hacia adelante (sin captura)
        if (to.getPiece() == null) {
            // Peón blanco: avanza hacia arriba-derecha (dq positivo, dr negativo)
            // Peón negro: avanza hacia abajo-izquierda (dq negativo, dr positivo)

            if (isWhite) {
                // Movimiento recto diagonal
                if (dq == 1 && dr == -1) return true;
                // Dos pasos iniciales
                if (!pawn.hasMoved() && dq == 2 && dr == -2) {
                    HexCell intermediate = board.getCell(from.getQ() + 1, from.getR() - 1);
                    return intermediate != null && intermediate.getPiece() == null;
                }
            } else {
                // Movimiento recto diagonal
                if (dq == -1 && dr == 1) return true;
                // Dos pasos iniciales
                if (!pawn.hasMoved() && dq == -2 && dr == 2) {
                    HexCell intermediate = board.getCell(from.getQ() - 1, from.getR() + 1);
                    return intermediate != null && intermediate.getPiece() == null;
                }
            }
        }

        // Captura (diagonales laterales)
        if (to.getPiece() != null && to.getPiece().getColor() != pawn.getColor()) {
            if (isWhite) {
                // Captura lateral izquierda y derecha
                return (dq == 0 && dr == -1) || (dq == 1 && dr == 0);
            } else {
                // Captura lateral izquierda y derecha
                return (dq == -1 && dr == 0) || (dq == 0 && dr == 1);
            }
        }

        return false;
    }

    // ==================== CABALLO ====================
    private boolean isValidKnightMove(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = (-to.getQ() - to.getR()) - (-from.getQ() - from.getR());

        // En ajedrez hexagonal de Gliński, el caballo tiene 12 movimientos posibles
        // Se mueve 2 celdas en una dirección y 1 en otra perpendicular

        // Verificar todas las combinaciones válidas de salto de caballo
        return (Math.abs(dq) == 2 && Math.abs(dr) == 1 && Math.abs(ds) == 1) ||
                (Math.abs(dq) == 1 && Math.abs(dr) == 2 && Math.abs(ds) == 1) ||
                (Math.abs(dq) == 1 && Math.abs(dr) == 1 && Math.abs(ds) == 2);
    }

    // ==================== ALFIL ====================
    private boolean isValidBishopMove(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = (-to.getQ() - to.getR()) - (-from.getQ() - from.getR());

        // El alfil se mueve en diagonales hexagonales
        // En coordenadas cúbicas: una coordenada constante (dX == 0)
        boolean isDiagonal = (dq == 0 && dr != 0 && ds != 0) ||
                (dr == 0 && dq != 0 && ds != 0) ||
                (ds == 0 && dq != 0 && dr != 0);

        if (!isDiagonal) return false;

        return isPathClear(from, to);
    }

    // ==================== TORRE ====================
    private boolean isValidRookMove(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = (-to.getQ() - to.getR()) - (-from.getQ() - from.getR());

        // La torre se mueve en líneas rectas hexagonales
        // En coordenadas cúbicas: dos coordenadas cambian igual y opuesto (dX = -dY, dZ = 0)
        boolean isStraight = (dq == -dr && ds == 0) ||
                (dq == -ds && dr == 0) ||
                (dr == -ds && dq == 0);

        if (!isStraight) return false;

        return isPathClear(from, to);
    }

    // ==================== REINA ====================
    private boolean isValidQueenMove(HexCell from, HexCell to) {
        // La reina combina movimientos de torre y alfil
        return isValidRookMove(from, to) || isValidBishopMove(from, to);
    }

    // ==================== REY ====================
    private boolean isValidKingMove(HexCell from, HexCell to) {
        // El rey se mueve una celda en cualquier dirección (6 direcciones hexagonales)
        // En coordenadas cúbicas, una celda vecina cumple: |dq| + |dr| + |ds| = 2
        int dq = Math.abs(to.getQ() - from.getQ());
        int dr = Math.abs(to.getR() - from.getR());
        int ds = Math.abs((-to.getQ() - to.getR()) - (-from.getQ() - from.getR()));

        // Un vecino hexagonal directo
        return (dq + dr + ds) == 2;
    }

    // ==================== UTILIDADES ====================

    /**
     * Verifica si el camino entre dos celdas está despejado
     */
    private boolean isPathClear(HexCell from, HexCell to) {
        int q0 = from.getQ();
        int r0 = from.getR();
        int q1 = to.getQ();
        int r1 = to.getR();

        int dq = q1 - q0;
        int dr = r1 - r0;

        // Calcular distancia en el eje que más se mueve
        int distance = Math.max(Math.max(Math.abs(dq), Math.abs(dr)),
                Math.abs(-dq - dr));

        if (distance <= 1) return true; // Adyacente

        // Determinar paso en cada eje
        int stepQ = (dq == 0) ? 0 : dq / Math.abs(dq);
        int stepR = (dr == 0) ? 0 : dr / Math.abs(dr);

        // Verificar cada celda intermedia
        for (int i = 1; i < distance; i++) {
            int q = q0 + (stepQ * i);
            int r = r0 + (stepR * i);

            HexCell cell = board.getCell(q, r);
            if (cell == null || cell.getPiece() != null) {
                return false; // Hay una pieza en el camino
            }
        }

        return true;
    }

    /**
     * Obtiene la distancia hexagonal entre dos celdas
     */
    private int hexDistance(HexCell from, HexCell to) {
        int dq = Math.abs(to.getQ() - from.getQ());
        int dr = Math.abs(to.getR() - from.getR());
        int ds = Math.abs((-to.getQ() - to.getR()) - (-from.getQ() - from.getR()));

        return (dq + dr + ds) / 2;
    }
}