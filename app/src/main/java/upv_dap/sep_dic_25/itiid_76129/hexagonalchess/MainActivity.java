package upv_dap.sep_dic_25.itiid_76129.hexagonalchess;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private HexagonalBoardView boardView;
    private TextView tvGameId, tvTurn;
    private Button btnCreateGame, btnJoinGame;

    private FirebaseManager firebaseManager;
    private HexCell selectedCell;
    private boolean isMyTurn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        firebaseManager = new FirebaseManager();
        setupListeners();

        // IMPORTANTE: Verificar posiciones iniciales para debugging
        debugBoardPositions();
    }

    private void initializeViews() {
        boardView = findViewById(R.id.hexBoard);
        tvGameId = findViewById(R.id.tvGameId);
        tvTurn = findViewById(R.id.tvTurn);
        btnCreateGame = findViewById(R.id.btnCreateGame);
        btnJoinGame = findViewById(R.id.btnJoinGame);
    }

    private void setupListeners() {
        // Botón crear partida
        btnCreateGame.setOnClickListener(v -> createGame());

        // Botón unirse a partida
        btnJoinGame.setOnClickListener(v -> showJoinGameDialog());

        // IMPORTANTE: Usar el listener correcto para drag & drop
        boardView.setOnMoveAttemptListener((from, to, callback) -> {
            if (!isMyTurn) {
                Toast.makeText(this, "No es tu turno", Toast.LENGTH_SHORT).show();
                callback.onMoveValidated(false);
                return;
            }

            // Verificar que la pieza sea mía
            if (from.getPiece() != null && !isMyPiece(from.getPiece())) {
                Toast.makeText(this, "No puedes mover esa pieza", Toast.LENGTH_SHORT).show();
                callback.onMoveValidated(false);
                return;
            }

            // Realizar el movimiento en Firebase
            String fromKey = from.getQ() + "," + from.getR();
            String toKey = to.getQ() + "," + to.getR();

            makeMove(fromKey, toKey, callback);
        });
    }

    private void debugBoardPositions() {
        // Esperar un poco para que el tablero se renderice
        boardView.postDelayed(() -> {
            HexBoard board = boardView.getBoard();
            Log.d(TAG, "=== DEBUGGING POSICIONES DEL TABLERO ===");
            DebugHelper.printAllPieces(board);
            DebugHelper.verifyKeyPieces(board);
            DebugHelper.printBoardASCII(board);
        }, 500);
    }

    private void createGame() {
        firebaseManager.createGame(new FirebaseManager.OnGameCreatedListener() {
            @Override
            public void onGameCreated(String gameId) {
                tvGameId.setText("ID: " + gameId);
                Toast.makeText(MainActivity.this,
                        "Partida creada. Comparte el ID: " + gameId,
                        Toast.LENGTH_LONG).show();

                listenToGameUpdates();
                enableButtons(false);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showJoinGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unirse a partida");

        final EditText input = new EditText(this);
        input.setHint("Ingresa el ID de la partida");
        builder.setView(input);

        builder.setPositiveButton("Unirse", (dialog, which) -> {
            String gameId = input.getText().toString().trim();
            if (!gameId.isEmpty()) {
                joinGame(gameId);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void joinGame(String gameId) {
        firebaseManager.joinGame(gameId, new FirebaseManager.OnGameJoinedListener() {
            @Override
            public void onGameJoined(String gameId) {
                tvGameId.setText("ID: " + gameId);
                Toast.makeText(MainActivity.this,
                        "Te has unido a la partida", Toast.LENGTH_SHORT).show();

                listenToGameUpdates();
                enableButtons(false);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenToGameUpdates() {
        firebaseManager.listenToGame(new FirebaseManager.OnGameUpdateListener() {
            @Override
            public void onGameUpdate(GameState gameState) {
                updateBoard(gameState);
                updateTurnDisplay(gameState);
                checkMyTurn(gameState);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBoard(GameState gameState) {
        HexBoard board = boardView.getBoard();

        // Limpiar todas las piezas
        for (HexCell cell : board.getAllCells().values()) {
            cell.setPiece(null);
        }

        // Colocar piezas según Firebase
        if (gameState.getPieces() != null) {
            for (String key : gameState.getPieces().keySet()) {
                GameState.PieceData pieceData = gameState.getPieces().get(key);
                String[] coords = key.split(",");
                int q = Integer.parseInt(coords[0]);
                int r = Integer.parseInt(coords[1]);

                HexCell cell = board.getCell(q, r);
                if (cell != null && pieceData != null) {
                    ChessPiece piece = pieceData.toPiece();
                    // IMPORTANTE: Actualizar el estado hasMoved
                    piece.setMoved(pieceData.hasMoved);
                    cell.setPiece(piece);
                }
            }
        }

        boardView.invalidate();
    }

    private void updateTurnDisplay(GameState gameState) {
        String turn = gameState.getCurrentTurn();
        tvTurn.setText("Turno: " + (turn.equals("white") ? "Blancas" : "Negras"));
        tvTurn.setTextColor(turn.equals("white") ?
                getColor(android.R.color.white) :
                getColor(android.R.color.darker_gray));
    }

    private void checkMyTurn(GameState gameState) {
        String myId = firebaseManager.getPlayerId();
        boolean amWhite = myId.equals(gameState.getWhitePlayerId());
        boolean amBlack = myId.equals(gameState.getBlackPlayerId());

        String currentTurn = gameState.getCurrentTurn();
        isMyTurn = (amWhite && "white".equals(currentTurn)) ||
                (amBlack && "black".equals(currentTurn));
    }

    private boolean isMyPiece(ChessPiece piece) {
        if (piece == null) return false;

        String myId = firebaseManager.getPlayerId();
        GameState gameState = firebaseManager.getCurrentGameState();

        if (gameState == null) return false;

        boolean amWhite = myId.equals(gameState.getWhitePlayerId());
        boolean amBlack = myId.equals(gameState.getBlackPlayerId());

        return (amWhite && piece.getColor() == ChessPiece.PieceColor.WHITE) ||
                (amBlack && piece.getColor() == ChessPiece.PieceColor.BLACK);
    }

    private void makeMove(String fromKey, String toKey,
                          HexagonalBoardView.OnMoveValidationCallback callback) {
        firebaseManager.makeMove(fromKey, toKey,
                new FirebaseManager.OnMoveCompleteListener() {
                    @Override
                    public void onMoveComplete() {
                        Toast.makeText(MainActivity.this,
                                "Movimiento realizado", Toast.LENGTH_SHORT).show();
                        callback.onMoveValidated(true);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this,
                                "Error: " + error, Toast.LENGTH_SHORT).show();
                        callback.onMoveValidated(false);
                    }
                });
    }

    private void enableButtons(boolean enable) {
        btnCreateGame.setEnabled(enable);
        btnJoinGame.setEnabled(enable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar el listener de Firebase
        if (firebaseManager != null) {
            firebaseManager.cleanup();
        }
    }
}