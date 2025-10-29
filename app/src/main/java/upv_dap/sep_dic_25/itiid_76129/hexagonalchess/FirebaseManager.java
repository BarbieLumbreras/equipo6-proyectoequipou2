package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private DatabaseReference gamesRef;
    private DatabaseReference currentGameRef;
    private String currentGameId;
    private String playerId;
    private GameState currentGameState;

    // NUEVO: Guardar el listener para poder removerlo después
    private ValueEventListener gameListener;

    public FirebaseManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gamesRef = database.getReference("games");
        playerId = GameIdGenerator.generateNewId();
    }

    // Crear nueva partida
    public void createGame(OnGameCreatedListener listener) {
        String gameId = GameIdGenerator.generateNewId();
        DatabaseReference gameRef = gamesRef.child(gameId);

        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    createGame(listener);
                } else {
                    initializeGameRoom(gameRef, gameId, listener);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError("Error al verificar ID de partida: " + error.getMessage());
            }
        });
    }

    private void initializeGameRoom(DatabaseReference gameRef, String gameId, OnGameCreatedListener listener) {
        GameState gameState = new GameState(gameId);
        gameState.setWhitePlayerId(playerId);

        HexBoard board = new HexBoard(); // El constructor ya inicializa el tablero

        Map<String, GameState.PieceData> piecesData = new HashMap<>();
        for (Map.Entry<String, HexCell> entry : board.getAllCells().entrySet()) {
            HexCell cell = entry.getValue();
            if (cell.getPiece() != null) {
                piecesData.put(entry.getKey(), new GameState.PieceData(cell.getPiece()));
            }
        }
        gameState.setPieces(piecesData);

        gameRef.setValue(gameState)
                .addOnSuccessListener(aVoid -> {
                    currentGameId = gameId;
                    currentGameRef = gameRef;
                    currentGameState = gameState;
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

                    if (gameState != null && gameState.getBlackPlayerId() == null &&
                            !playerId.equals(gameState.getWhitePlayerId())) {
                        gamesRef.child(gameId).child("blackPlayerId").setValue(playerId);
                        gamesRef.child(gameId).child("status").setValue("playing");

                        currentGameId = gameId;
                        currentGameRef = gamesRef.child(gameId);
                        listener.onGameJoined(gameId);
                    } else if (gameState != null &&
                            (playerId.equals(gameState.getWhitePlayerId()) ||
                                    playerId.equals(gameState.getBlackPlayerId()))) {
                        currentGameId = gameId;
                        currentGameRef = gamesRef.child(gameId);
                        listener.onGameJoined(gameId);
                    } else {
                        listener.onError("Partida llena o no disponible.");
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

    // Realizar movimiento - VERSIÓN MEJORADA
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

                Map<String, GameState.PieceData> pieces = gameState.getPieces() != null ?
                        gameState.getPieces() : new HashMap<>();

                GameState.PieceData piece = pieces.get(fromKey);

                if (piece == null) {
                    listener.onError("No hay pieza en la posición origen");
                    return;
                }

                // Validar que la pieza sea del color correcto
                String expectedColor = "white".equals(gameState.getCurrentTurn()) ? "WHITE" : "BLACK";
                if (!piece.color.equals(expectedColor)) {
                    listener.onError("No puedes mover esa pieza");
                    return;
                }

                // Realizar el movimiento
                pieces.remove(fromKey);
                piece.hasMoved = true; // Marcar que se ha movido (importante para peones)
                pieces.put(toKey, piece);

                String newTurn = "white".equals(gameState.getCurrentTurn()) ? "black" : "white";

                Map<String, Object> updates = new HashMap<>();
                updates.put("pieces", pieces);
                updates.put("currentTurn", newTurn);
                updates.put("lastMoveTimestamp", System.currentTimeMillis());

                currentGameRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            listener.onMoveComplete();
                        })
                        .addOnFailureListener(e -> {
                            listener.onError("Error en Firebase: " + e.getMessage());
                        });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError("Error de conexión: " + error.getMessage());
            }
        });
    }

    // Escuchar cambios en el juego
    public void listenToGame(OnGameUpdateListener listener) {
        if (currentGameRef == null) return;

        // Remover listener anterior si existe
        if (gameListener != null) {
            currentGameRef.removeEventListener(gameListener);
        }

        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                GameState gameState = snapshot.getValue(GameState.class);
                if (gameState != null) {
                    currentGameState = gameState;
                    listener.onGameUpdate(gameState);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };

        currentGameRef.addValueEventListener(gameListener);
    }

    // NUEVO: Método para limpiar recursos
    public void cleanup() {
        if (currentGameRef != null && gameListener != null) {
            currentGameRef.removeEventListener(gameListener);
        }
        gameListener = null;
        currentGameRef = null;
        currentGameId = null;
        currentGameState = null;
    }

    public String getPlayerId() {
        return playerId;
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public String getCurrentGameId() {
        return currentGameId;
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