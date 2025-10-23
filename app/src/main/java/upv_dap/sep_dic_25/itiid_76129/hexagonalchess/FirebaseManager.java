package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.GameState;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.HexBoard;
import upv_dap.sep_dic_25.itiid_76129.hexagonalchess.HexCell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FirebaseManager {
    private DatabaseReference gamesRef;
    private DatabaseReference currentGameRef;
    private String currentGameId;
    private String playerId;

    public FirebaseManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gamesRef = database.getReference("games");
        playerId = UUID.randomUUID().toString();
    }

    // Crear nueva partida
    public void createGame(OnGameCreatedListener listener) {
        String gameId = gamesRef.push().getKey();
        GameState gameState = new GameState(gameId);
        gameState.setWhitePlayerId(playerId);

        // Inicializar tablero y convertir a Firebase
        HexBoard board = new HexBoard();
        Map<String, GameState.PieceData> piecesData = new HashMap<>();

        for (Map.Entry<String, HexCell> entry : board.getAllCells().entrySet()) {
            HexCell cell = entry.getValue();
            if (cell.getPiece() != null) {
                piecesData.put(entry.getKey(),
                        new GameState.PieceData(cell.getPiece()));
            }
        }

        gameState.setPieces(piecesData);

        gamesRef.child(gameId).setValue(gameState)
                .addOnSuccessListener(aVoid -> {
                    currentGameId = gameId;
                    currentGameRef = gamesRef.child(gameId);
                    listener.onGameCreated(gameId);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Unirse a partida existente
    public void joinGame(String gameId, OnGameJoinedListener listener) {
        gamesRef.child(gameId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    GameState gameState = snapshot.getValue(GameState.class);

                    if (gameState != null && gameState.getBlackPlayerId() == null) {
                        gamesRef.child(gameId).child("blackPlayerId").setValue(playerId);
                        gamesRef.child(gameId).child("status").setValue("playing");

                        currentGameId = gameId;
                        currentGameRef = gamesRef.child(gameId);
                        listener.onGameJoined(gameId);
                    } else {
                        listener.onError("Partida llena o no disponible");
                    }
                } else {
                    listener.onError("Partida no encontrada");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // Realizar movimiento
    public void makeMove(String fromKey, String toKey, OnMoveCompleteListener listener) {
        if (currentGameRef == null) {
            listener.onError("No hay partida activa");
            return;
        }

        currentGameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                GameState gameState = snapshot.getValue(GameState.class);

                if (gameState == null) {
                    listener.onError("Estado de juego inválido");
                    return;
                }

                // Validar turno
                boolean isWhite = playerId.equals(gameState.getWhitePlayerId());
                boolean isBlack = playerId.equals(gameState.getBlackPlayerId());
                boolean correctTurn = (isWhite && "white".equals(gameState.getCurrentTurn())) ||
                        (isBlack && "black".equals(gameState.getCurrentTurn()));

                if (!correctTurn) {
                    listener.onError("No es tu turno");
                    return;
                }

                // Realizar movimiento
                Map<String, GameState.PieceData> pieces = gameState.getPieces();
                GameState.PieceData piece = pieces.get(fromKey);

                if (piece == null) {
                    listener.onError("No hay pieza en la posición origen");
                    return;
                }

                // Actualizar posición
                pieces.remove(fromKey);
                piece.hasMoved = true;
                pieces.put(toKey, piece);

                // Cambiar turno
                String newTurn = "white".equals(gameState.getCurrentTurn()) ? "black" : "white";

                // Actualizar en Firebase
                Map<String, Object> updates = new HashMap<>();
                updates.put("pieces", pieces);
                updates.put("currentTurn", newTurn);
                updates.put("lastMoveTimestamp", System.currentTimeMillis());

                currentGameRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> listener.onMoveComplete())
                        .addOnFailureListener(e -> listener.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // Escuchar cambios en el juego
    public void listenToGame(OnGameUpdateListener listener) {
        if (currentGameRef == null) return;

        currentGameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                GameState gameState = snapshot.getValue(GameState.class);
                if (gameState != null) {
                    listener.onGameUpdate(gameState);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public String getPlayerId() {
        return playerId;
    }

    // Interfaces de callbacks
    public interface OnGameCreatedListener {
        void onGameCreated(String gameId);
        void onError(String error);
    }

    public interface OnGameJoinedListener {
        void onGameJoined(String gameId);
        void onError(String error);
    }

    public interface OnMoveCompleteListener {
        void onMoveComplete();
        void onError(String error);
    }

    public interface OnGameUpdateListener {
        void onGameUpdate(GameState gameState);
        void onError(String error);
    }
}