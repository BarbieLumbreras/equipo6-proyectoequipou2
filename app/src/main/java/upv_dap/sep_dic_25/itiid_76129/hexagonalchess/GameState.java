package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    private String gameId;
    private String whitePlayerId;
    private String blackPlayerId;
    private String currentTurn; // "white" o "black"
    private Map<String, PieceData> pieces; // key: "q,r", value: datos de pieza
    private String status; // "waiting", "playing", "finished"
    private String winner;
    private long lastMoveTimestamp;

    public GameState() {
        // Constructor vacío requerido por Firebase
        pieces = new HashMap<>();
    }

    public GameState(String gameId) {
        this.gameId = gameId;
        this.currentTurn = "white";
        this.status = "waiting";
        this.pieces = new HashMap<>();
        this.lastMoveTimestamp = System.currentTimeMillis();
    }

    // Clase interna para serializar piezas
    public static class PieceData {
        public String type; // "PAWN", "KNIGHT", etc.
        public String color; // "WHITE", "BLACK"
        public boolean hasMoved;

        public PieceData() {}

        public PieceData(ChessPiece piece) {
            this.type = piece.getType().name();
            this.color = piece.getColor().name();
            this.hasMoved = piece.hasMoved();
        }

        public ChessPiece toPiece() {
            return new ChessPiece(
                    ChessPiece.PieceType.valueOf(type),
                    ChessPiece.PieceColor.valueOf(color)
            );
        }
    }

    // Getters y Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getWhitePlayerId() { return whitePlayerId; }
    public void setWhitePlayerId(String id) { this.whitePlayerId = id; }

    public String getBlackPlayerId() { return blackPlayerId; }
    public void setBlackPlayerId(String id) { this.blackPlayerId = id; }

    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String turn) { this.currentTurn = turn; }

    public Map<String, PieceData> getPieces() { return pieces; }
    public void setPieces(Map<String, PieceData> pieces) { this.pieces = pieces; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public long getLastMoveTimestamp() { return lastMoveTimestamp; }
    public void setLastMoveTimestamp(long timestamp) { this.lastMoveTimestamp = timestamp; }
}