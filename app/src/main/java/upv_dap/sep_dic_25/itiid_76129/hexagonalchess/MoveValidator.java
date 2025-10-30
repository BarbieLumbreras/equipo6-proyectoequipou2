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

    // ==================== CABALLO HEXAGONAL GLIŃSKI CORRECTO ====================
    private boolean isValidKnightMove(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();

        // El caballo se mueve: 2 pasos en una dirección + 1 paso en dirección adyacente
        // Las 6 direcciones hexagonales son:
        int[][] directions = {
                {1, 0}, {1, -1}, {0, -1},
                {-1, 0}, {-1, 1}, {0, 1}
        };

        // Para cada dirección principal, el caballo puede moverse a 2 casillas:
        // - 2 pasos en dirección A + 1 paso en dirección A+1 (derecha)
        // - 2 pasos en dirección A + 1 paso en dirección A-1 (izquierda)

        for (int i = 0; i < 6; i++) {
            int[] dir1 = directions[i];
            int[] dir2 = directions[(i + 1) % 6]; // Dirección adyacente derecha
            int[] dir3 = directions[(i + 5) % 6]; // Dirección adyacente izquierda

            // 2 pasos en dirección i + 1 paso en dirección i+1
            int targetQ1 = 2 * dir1[0] + dir2[0];
            int targetR1 = 2 * dir1[1] + dir2[1];

            // 2 pasos en dirección i + 1 paso en dirección i-1
            int targetQ2 = 2 * dir1[0] + dir3[0];
            int targetR2 = 2 * dir1[1] + dir3[1];

            if ((dq == targetQ1 && dr == targetR1) ||
                    (dq == targetQ2 && dr == targetR2)) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidBishopMove(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = -(dq + dr); // importante: derivar s para mantener q+r+s=0

        if (dq == 0 && dr == 0) return false;

        boolean isDiagonal = (dq == dr) || (dr == ds) || (ds == dq);
        if (!isDiagonal) return false;

        // Deja tu chequeo de bloqueo
        return isPathClear(from, to);
    }


    // ==================== TORRE HEXAGONAL ====================
    private boolean isValidRookMove(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = to.getS() - from.getS();

        if (dq == 0 && dr == 0) return false; // No es un movimiento

        // La torre se mueve si un eje permanece constante
        boolean isStraight = (dq == 0) || (dr == 0) || (ds == 0);

        if (!isStraight) return false;

        return isPathClear(from, to);
    }

    // ==================== REINA HEXAGONAL (Gliński - Como la foto) ====================
// ==================== DAMA/REINA HEXAGONAL GLIŃSKI COMPLETA ====================
    private boolean isValidQueenMove(HexCell from, HexCell to) {
        // Movimientos básicos: Torre + Alfil
        if (isValidRookMove(from, to) || isValidBishopMove(from, to)) {
            return true;
        }

        // Movimientos de salto especial de la Dama
        return isValidQueenJump(from, to);
    }

    // ==================== SALTO ESPECIAL DE LA DAMA ====================
    private boolean isValidQueenJump(HexCell from, HexCell to) {
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = to.getS() - from.getS();

        // Los saltos especiales de la Dama siguen patrones específicos
        // Desde (0,0) salta a: (-2,-2), (2,-4), (-4,2), (-2,4), (2,2), (4,-2)
        // Estos patrones se pueden generalizar para cualquier posición

        // Los saltos son movimientos donde las 3 coordenadas cambian
        // y siguen la relación: |dq| = 2, |dr| = 2, |ds| = 4 en varias permutaciones

        int absDq = Math.abs(dq);
        int absDr = Math.abs(dr);
        int absDs = Math.abs(ds);

        // Patrones de salto de la Dama:
        // 1. (2, 2, -4) y permutaciones
        // 2. (2, -2, 0) y permutaciones (pero esto sería movimiento de alfil normal)
        // 3. (4, -2, -2) y permutaciones

        // Basado en los ejemplos que diste:
        // (-2,-2,4), (2,-4,2), (-4,2,2), (-2,4,-2), (2,2,-4), (4,-2,-2)

        return (absDq == 2 && absDr == 2 && absDs == 4) ||
                (absDq == 2 && absDr == 4 && absDs == 2) ||
                (absDq == 4 && absDr == 2 && absDs == 2) ||
                // También incluyendo los patrones con signos opuestos
                (absDq == 2 && absDr == 2 && absDs == 0) || // Esto sería alfil normal
                (absDq == 4 && absDr == 2 && absDs == 2) ||
                (absDq == 2 && absDr == 4 && absDs == 2);
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
        int dq = to.getQ() - from.getQ();
        int dr = to.getR() - from.getR();
        int ds = -(dq + dr); // mantener q+r+s=0

        // Número de pasos "rectos" (arista o vértice) hasta el destino
        int g = gcd3(Math.abs(dq), Math.abs(dr), Math.abs(ds));
        if (g <= 1) return true; // adyacente o misma casilla (ya filtrado fuera)

        // Paso correcto a lo largo de la línea (axial o diagonal por vértice)
        int stepQ = dq / g;
        int stepR = dr / g;

        // Verificar celdas intermedias (excluye origen y destino)
        int q = from.getQ();
        int r = from.getR();
        for (int i = 1; i < g; i++) {
            q += stepQ;
            r += stepR;

            HexCell cell = board.getCell(q, r);
            if (cell == null || cell.getPiece() != null) {
                return false;
            }
        }
        return true;
    }

    private int gcd(int a, int b) {
        a = Math.abs(a); b = Math.abs(b);
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    private int gcd3(int a, int b, int c) {
        return gcd(gcd(a, b), c);
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