import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Board {
    int dim = 8;
    char[][] board = new char[dim][dim]; // board dim: 8x8
    boolean whiteToMove = true;
    boolean whiteCanCastleK = true; //castle king side
    boolean whiteCanCastleQ = true; //castle queen side
    boolean blackCanCastleK = true; //castle king side
    boolean blackCanCastleQ = true; //castle queen side
    int nonPawnMovesCount = 0;
    int currentMoveCount = 0;
    int colorTheme; // board&piece themes?

    public Board() {
        board[7] = new char[]{'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'};
        board[6] = new char[]{'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'};
        for (int i = 2; i < 6; i++) {
            board[i] = new char[]{'-', '-', '-', '-', '-', '-', '-', '-'};
        }
        board[1] = new char[]{'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'};
        board[0] = new char[]{'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'};
    }

    public Board(String fen) {
        String[] fenSplit = fen.split(" ");
        whiteToMove = fenSplit[1].equals("w");
        whiteCanCastleK = fenSplit[2].contains("K");
        whiteCanCastleQ = fenSplit[2].contains("Q");
        blackCanCastleK = fenSplit[2].contains("k");
        blackCanCastleQ = fenSplit[2].contains("q");
        nonPawnMovesCount = Integer.valueOf(fenSplit[4]);
        currentMoveCount = Integer.valueOf(fenSplit[5]);
        fen = fenSplit[0];
        int rank = 7;
        int file = 0;
        for (char c : fen.toCharArray()) {
            if (c == '/') {
                rank--;
                file = 0;
            } else if (c > 47 && c < 58) {// is number
                for (int i = 0; i < c - 48; i++) {
                    board[rank][file] = '-';
                    file++;
                }
            } else {
                board[rank][file] = c;
                file++;
            }
        }
    }

    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }//https://stackoverflow.com/questions/8992100/test-if-a-string-contains-any-of-the-strings-from-an-array

    public String boardToFen() {
        String ans = "";
        // board fen
        for (int rank = 7; rank >= 0; rank--) {
            int blankCounter = 0;
            for (int file = 0; file < dim; file++) {
                if (board[rank][file] != '-') {
                    if (blankCounter != 0) {
                        ans += String.valueOf(blankCounter);
                        blankCounter = 0;
                    }
                    ans += board[rank][file];
                } else {
                    blankCounter++;
                }
            }
            if (blankCounter != 0) {
                ans += String.valueOf(blankCounter);
            }
            if (rank > 0) {
                ans += "/";
            }
        }
        // whose move
        if (whiteToMove) {
            ans += " w ";
        } else {
            ans += " b ";
        }
        // castle-ing rights
        boolean neither = true;
        if (whiteCanCastleK) {
            ans += "K";
            neither = false;
        }
        if (whiteCanCastleQ) {
            ans += "Q";
            neither = false;
        }
        if (blackCanCastleK) {
            ans += "k";
            neither = false;
        }
        if (blackCanCastleQ) {
            ans += "q";
            neither = false;
        }
        if (neither) {
            ans += "-";
        }
        // weird fen formatting
        ans += " -";
        // weird non-pawn move counter
        ans += " " + nonPawnMovesCount;
        // current move count
        ans += " " + currentMoveCount;
        return ans;
    }

    public boolean whitePieceOnSquare(int rank, int file) {
        return board[rank][file] == 'P' || board[rank][file] == 'N' || board[rank][file] == 'B' || board[rank][file] == 'R' || board[rank][file] == 'K' || board[rank][file] == 'Q';
    }

    public boolean blackPieceOnSquare(int rank, int file) {
        return board[rank][file] == 'p' || board[rank][file] == 'n' || board[rank][file] == 'b' || board[rank][file] == 'r' || board[rank][file] == 'k' || board[rank][file] == 'q';
    }

    public void move(String input) throws Exception {
        String[] pieceNames = new String[]{"R", "N", "B", "Q", "K", "O"}; //determine if pawn move
        if (!stringContainsItemFromList(input, pieceNames) || input.contains("=")) {//pawn
            pawnMove(input, whiteToMove);
        } else if (input.contains("B")) {//bishop
            bishopMove(input, whiteToMove);
        } else if (input.contains("R")) {//rook
            rookMove(input, whiteToMove);
        }else if(input.contains("Q")){//queen
            queenMove(input, whiteToMove);
        }else if(input.contains("N")) {
            knightMove(input, whiteToMove); //in development
        }else{//necessarily is the King or special king move such as castling
            kingMove(input, whiteToMove); //implement check blocking castle
        }
        whiteToMove = !whiteToMove;
        currentMoveCount++;

    }

    public void queenMove(String input, boolean whiteToMove) throws Exception{
        // check if move is legal threw existing methods
        try{
            rookMoveQ(input, whiteToMove); //pass off the move as a rook move
        }catch (Exception rook){
            try{
                bishopMoveQ(input, whiteToMove);  //pass off the move as a rook move
            }catch (Exception bishop){
                throw new Exception("Invalid queen move");
            }
        }
    }

    public void rookMoveQ(String input, boolean whiteToMove) throws Exception {
        if (whiteToMove) {
            boolean capture = input.contains("x"); // capture or no
            //letter coordinate to int, 97-104 is a-h
            input = input.replace("x", "");
            int file = input.charAt(input.length() - 2) - 97;// last two are always target coordinates
            int rank = input.charAt(input.length() - 1) - 49;
            //check that no piece is there just in case
            if (capture) {
                if (whitePieceOnSquare(rank, file)) {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid rook move: you cannot capture your own pieces!");
                }
            } else {
                if (board[rank][file] != '-') {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid rook move: square already occupied!");
                }
            }
            int rFile = file;
            int rRank = rank;
            //time to go find which rook the user is moving
            boolean matched = false; //for debugging purposes
            if (input.length() == 3) {// only one valid rook
                for (rFile = file + 1; rFile < dim; rFile++) {//check right
                    if (board[rRank][rFile] == 'Q') {
                        matched = true;
                        break;
                    } else if (board[rRank][rFile] != '-') {
                        break;
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rFile = file - 1; rFile >= 0; rFile--) {//check left
                        if (board[rRank][rFile] == 'Q') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rRank = rank + 1; rRank < dim; rRank++) {//check above
                        if (board[rRank][rFile] == 'Q') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rRank = rank - 1; rRank >= 0; rRank--) {//check below
                        if (board[rRank][rFile] == 'Q') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
            } else {
                if (input.charAt(1) > 47 && input.charAt(1) < 58) {// rank given
                    int givenRank = input.charAt(1) - 49;//check along given rank
                    for (rFile = file + 1; rFile < dim; rFile++) {//check right
                        if (board[givenRank][rFile] == 'Q') {
                            matched = true;
                            break;
                        } else if (board[givenRank][rFile] != '-') {
                            break;
                        }
                    }
                    if (!matched) {
                        for (rFile = file - 1; rFile >= 0; rFile--) {//check left
                            if (board[givenRank][rFile] == 'Q') {
                                matched = true;
                                break;
                            } else if (board[givenRank][rFile] != '-') {
                                break;
                            }
                        }
                    }
                } else {// file given
                    for (rRank = rank + 1; rRank < dim; rRank++) {//check above
                        int givenFile = input.charAt(1) - 97;//check along given file
                        if (board[rRank][givenFile] == 'Q') {
                            matched = true;
                            break;
                        } else if (board[rRank][givenFile] != '-') {
                            break;
                        }
                    }
                    if (!matched) {
                        rFile = file;
                        rRank = rank;
                        for (rRank = rank - 1; rRank >= 0; rRank--) {//check below
                            if (board[rRank][rFile] == 'Q') {
                                matched = true;
                                break;
                            } else if (board[rRank][rFile] != '-') {
                                break;
                            }
                        }
                    }
                }
            }
            if (!matched) {
                throw new Exception("invalid rook move: no rook maneuverable to that square!");
            }
            // now update board
            board[rRank][rFile] = '-';
            board[rank][file] = 'Q';
        } else {// black to move
            boolean capture = input.contains("x"); // capture or no
            //letter coordinate to int, 97-104 is a-h
            input = input.replace("x", "");
            int file = input.charAt(input.length() - 2) - 97;// last two are always target coordinates
            int rank = input.charAt(input.length() - 1) - 49;
            //check that no piece is there just in case
            if (capture) {
                if (blackPieceOnSquare(rank, file)) {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid rook move: you cannot capture your own pieces!");
                }
            } else {
                if (board[rank][file] != '-') {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid rook move: square already occupied!");
                }
            }
            int rFile = file;
            int rRank = rank;
            //time to go find which rook the user is moving
            boolean matched = false; //for debugging purposes
            if (input.length() == 3) {// only one valid rook
                for (rFile = file + 1; rFile < dim; rFile++) {//check right
                    if (board[rRank][rFile] == 'q') {
                        matched = true;
                        break;
                    } else if (board[rRank][rFile] != '-') {
                        break;
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rFile = file - 1; rFile >= 0; rFile--) {//check left
                        if (board[rRank][rFile] == 'q') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rRank = rank + 1; rRank < dim; rRank++) {//check above
                        if (board[rRank][rFile] == 'q') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rRank = rank - 1; rRank >= 0; rRank--) {//check below
                        if (board[rRank][rFile] == 'q') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
            } else {
                if (input.charAt(1) > 47 && input.charAt(1) < 58) {// rank given
                    int givenRank = input.charAt(1) - 49;//check along given rank
                    for (rFile = file + 1; rFile < dim; rFile++) {//check right
                        if (board[rRank][rFile] == 'q') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                    if (!matched) {
                        for (rFile = file - 1; rFile >= 0; rFile--) {//check left
                            if (board[rRank][rFile] == 'q') {
                                matched = true;
                                break;
                            } else if (board[rRank][rFile] != '-') {
                                break;
                            }
                        }
                    }
                } else {// file given
                    for (rRank = rank + 1; rRank < dim; rRank++) {//check above
                        int givenFile = input.charAt(1) - 97;//check along given file
                        if (board[rRank][rFile] == 'q') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                    if (!matched) {
                        rFile = file;
                        rRank = rank;
                        for (rRank = rank - 1; rRank >= 0; rRank--) {//check below
                            if (board[rRank][rFile] == 'q') {
                                matched = true;
                                break;
                            } else if (board[rRank][rFile] != '-') {
                                break;
                            }
                        }
                    }
                }
            }
            if (!matched) {
                throw new Exception("invalid rook move: no rook maneuverable to that square!");
            }
            // now update board
            board[rRank][rFile] = '-';
            board[rank][file] = 'q';
        }
    } //queen move helper method

    public void bishopMoveQ(@NotNull String input, boolean whiteToMove) throws Exception {
        String loc = input.substring(input.length() - 2); //chess coord for move
        int file = loc.charAt(0) - 97;
        int rank = loc.charAt(1) - 49;
        boolean matches = false;
        if (whiteToMove) {
            if (whitePieceOnSquare(rank, file)) {
                throw new Exception("invalid bishop move: you cannot capture your own pieces!");
            }
            int bFile = 0; //correct bishop file if exists
            int bRank = 0; //correct bishop file if exists
            if ((rank + file) % 2 == 1) {//white squares
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == 'Q' && (row + col) % 2 == 1) {
                            bFile = col;
                            bRank = row;
                            int r = row;
                            int c = col;
                            //check same diagonal first
                            matches=Math.abs(bRank-rank)==Math.abs(bFile-file);
                            //check diagonals clear
                            if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file > col) {
                                while (r < rank - 1 && c < file - 1) {
                                    r++;
                                    c++;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank < row && file < col) {
                                while (r > rank + 1 && c > file + 1) {
                                    r--;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {//black squares
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == 'Q' && (row + col) % 2 == 0) {
                            bFile = col; //where bishop was found, recall: rank and file is target location
                            bRank = row;
                            int r = row; //temp variables traversing the diagonals
                            int c = col;
                            //check same diagonal first
                            matches=Math.abs(bRank-rank)==Math.abs(bFile-file);
                            //check diagonals clear
                            if (rank > row && file < col) { //top left
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file > col) { // top right
                                while (r < rank - 1 && c < file - 1) {
                                    r++;
                                    c++;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank < row && file < col) {
                                while (r > rank + 1 && c > file + 1) {
                                    r--;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!matches) {
                throw new Exception("invalid bishop move: move path obstructed");
            }
            board[bRank][bFile] = '-';
            board[rank][file] = 'Q';
        } else {
            if (blackPieceOnSquare(rank, file)) {
                throw new Exception("invalid bishop move: you cannot capture your own pieces!");
            }
            int bFile = 0; //correct bishop file if exists
            int bRank = 0; //correct bishop file if exists
            if ((rank + file) % 2 == 1) {//white squares
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == 'q' && (row + col) % 2 == 1) {
                            bFile = col;
                            bRank = row;
                            int r = row;
                            int c = col;
                            //check same diagonal first
                            matches=Math.abs(bRank-rank)==Math.abs(bFile-file);
                            //check diagonals clear
                            if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file > col) {
                                while (r < rank - 1 && c < file - 1) {
                                    r++;
                                    c++;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank < row && file < col) {
                                while (r > rank + 1 && c > file + 1) {
                                    r--;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {//black squares
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == 'q' && (row + col) % 2 == 0) {
                            bFile = col;
                            bRank = row;
                            int r = row;
                            int c = col;
                            //check same diagonal first
                            matches=Math.abs(bRank-rank)==Math.abs(bFile-file);
                            //check diagonals clear
                            if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file > col) {
                                while (r < rank - 1 && c < file - 1) {
                                    r++;
                                    c++;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank < row && file < col) {
                                while (r > rank + 1 && c > file + 1) {
                                    r--;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!matches) {
                throw new Exception("invalid bishop move: move path obstructed");
            }
            board[bRank][bFile] = '-';
            board[rank][file] = 'q';
        }
    } //queen move helper method

    public void rookMove(String input, boolean whiteToMove) throws Exception {
        if (whiteToMove) {
            boolean capture = input.contains("x"); // capture or no
            //letter coordinate to int, 97-104 is a-h
            input = input.replace("x", "");
            int file = input.charAt(input.length() - 2) - 97;// last two are always target coordinates
            int rank = input.charAt(input.length() - 1) - 49;
            //check that no piece is there just in case
            if (capture) {
                if (whitePieceOnSquare(rank, file)) {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid rook move: you cannot capture your own pieces!");
                }
            } else {
                if (board[rank][file] != '-') {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid rook move: square already occupied!");
                }
            }
            int rFile = file;
            int rRank = rank;
            //time to go find which rook the user is moving
            boolean matched = false; //for debugging purposes
            if (input.length() == 3) {// only one valid rook
                for (rFile = file + 1; rFile < dim; rFile++) {//check right
                    if (board[rRank][rFile] == 'R') {
                        matched = true;
                        break;
                    } else if (board[rRank][rFile] != '-') {
                        break;
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rFile = file - 1; rFile >= 0; rFile--) {//check left
                        if (board[rRank][rFile] == 'R') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rRank = rank + 1; rRank < dim; rRank++) {//check above
                        if (board[rRank][rFile] == 'R') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rRank = rank - 1; rRank >= 0; rRank--) {//check below
                        if (board[rRank][rFile] == 'R') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
            } else {
                if (input.charAt(1) > 47 && input.charAt(1) < 58) {// rank given
                    int givenRank = input.charAt(1) - 49;//check along given rank
                    for (rFile = file + 1; rFile < dim; rFile++) {//check right
                        if (board[givenRank][rFile] == 'R') {
                            matched = true;
                            break;
                        } else if (board[givenRank][rFile] != '-') {
                            break;
                        }
                    }
                    if (!matched) {
                        for (rFile = file - 1; rFile >= 0; rFile--) {//check left
                            if (board[givenRank][rFile] == 'R') {
                                matched = true;
                                break;
                            } else if (board[givenRank][rFile] != '-') {
                                break;
                            }
                        }
                    }
                } else {// file given
                    for (rRank = rank + 1; rRank < dim; rRank++) {//check above
                        int givenFile = input.charAt(1) - 97;//check along given file
                        if (board[rRank][givenFile] == 'R') {
                            matched = true;
                            break;
                        } else if (board[rRank][givenFile] != '-') {
                            break;
                        }
                    }
                    if (!matched) {
                        rFile = file;
                        rRank = rank;
                        for (rRank = rank - 1; rRank >= 0; rRank--) {//check below
                            if (board[rRank][rFile] == 'R') {
                                matched = true;
                                break;
                            } else if (board[rRank][rFile] != '-') {
                                break;
                            }
                        }
                    }
                }
            }
            if (!matched) {
                throw new Exception("invalid rook move: no rook maneuverable to that square!");
            }
            //update castle privileges
            if(rRank==0&&rFile==0){//bottom left rook
                whiteCanCastleQ=false;
            }else if(rRank==0&&rFile==7){//bottom right rook
                whiteCanCastleK=false;
            }
            // now update board
            board[rRank][rFile] = '-';
            board[rank][file] = 'R';
        } else {// black to move
            boolean capture = input.contains("x"); // capture or no
            //letter coordinate to int, 97-104 is a-h
            input = input.replace("x", "");
            int file = input.charAt(input.length() - 2) - 97;// last two are always target coordinates
            int rank = input.charAt(input.length() - 1) - 49;
            //check that no piece is there just in case
            if (capture) {
                if (blackPieceOnSquare(rank, file)) {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid rook move: you cannot capture your own pieces!");
                }
            } else {
                if (board[rank][file] != '-') {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid rook move: square already occupied!");
                }
            }
            int rFile = file;
            int rRank = rank;
            //time to go find which rook the user is moving
            boolean matched = false; //for debugging purposes
            if (input.length() == 3) {// only one valid rook
                for (rFile = file + 1; rFile < dim; rFile++) {//check right
                    if (board[rRank][rFile] == 'r') {
                        matched = true;
                        break;
                    } else if (board[rRank][rFile] != '-') {
                        break;
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rFile = file - 1; rFile >= 0; rFile--) {//check left
                        if (board[rRank][rFile] == 'r') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rRank = rank + 1; rRank < dim; rRank++) {//check above
                        if (board[rRank][rFile] == 'r') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
                if (!matched) {
                    rFile = file;
                    rRank = rank;
                    for (rRank = rank - 1; rRank >= 0; rRank--) {//check below
                        if (board[rRank][rFile] == 'r') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                }
            } else {
                if (input.charAt(1) > 47 && input.charAt(1) < 58) {// rank given
                    int givenRank = input.charAt(1) - 49;//check along given rank
                    for (rFile = file + 1; rFile < dim; rFile++) {//check right
                        if (board[rRank][rFile] == 'r') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                    if (!matched) {
                        for (rFile = file - 1; rFile >= 0; rFile--) {//check left
                            if (board[rRank][rFile] == 'r') {
                                matched = true;
                                break;
                            } else if (board[rRank][rFile] != '-') {
                                break;
                            }
                        }
                    }
                } else {// file given
                    for (rRank = rank + 1; rRank < dim; rRank++) {//check above
                        int givenFile = input.charAt(1) - 97;//check along given file
                        if (board[rRank][rFile] == 'r') {
                            matched = true;
                            break;
                        } else if (board[rRank][rFile] != '-') {
                            break;
                        }
                    }
                    if (!matched) {
                        rFile = file;
                        rRank = rank;
                        for (rRank = rank - 1; rRank >= 0; rRank--) {//check below
                            if (board[rRank][rFile] == 'r') {
                                matched = true;
                                break;
                            } else if (board[rRank][rFile] != '-') {
                                break;
                            }
                        }
                    }
                }
            }
            if (!matched) {
                throw new Exception("invalid rook move: no rook maneuverable to that square!");
            }
            //update castle privileges
            if(rRank==7&&rFile==0){//top left rook
                blackCanCastleQ=false;
            }else if(rRank==7&&rFile==7){//top right rook
                blackCanCastleK=false;
            }
            // now update board
            board[rRank][rFile] = '-';
            board[rank][file] = 'r';
        }
    }

    public void bishopMove(@NotNull String input, boolean whiteToMove) throws Exception {
        String loc = input.substring(input.length() - 2); //chess coord for move
        int file = loc.charAt(0) - 97;
        int rank = loc.charAt(1) - 49;
        boolean matches = false;
        if (whiteToMove) {
            if (whitePieceOnSquare(rank, file)) {
                throw new Exception("invalid bishop move: you cannot capture your own pieces!");
            }
            int bFile = 0; //correct bishop file if exists
            int bRank = 0; //correct bishop file if exists
            if ((rank + file) % 2 == 1) {//white squares
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == 'B' && (row + col) % 2 == 1) {
                            bFile = col;
                            bRank = row;
                            int r = row;
                            int c = col;
                            //check same diagonal first
                            matches=Math.abs(bRank-rank)==Math.abs(bFile-file);
                            //check diagonals clear
                            if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file > col) {
                                while (r < rank - 1 && c < file - 1) {
                                    r++;
                                    c++;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank < row && file < col) {
                                while (r > rank + 1 && c > file + 1) {
                                    r--;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {//black squares
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == 'B' && (row + col) % 2 == 0) {
                            bFile = col; //where bishop was found, recall: rank and file is target location
                            bRank = row;
                            int r = row; //temp variables traversing the diagonals
                            int c = col;
                            //check same diagonal first
                            matches=Math.abs(bRank-rank)==Math.abs(bFile-file);
                            //check diagonals clear
                            if (rank > row && file < col) { //top left
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file > col) { // top right
                                while (r < rank - 1 && c < file - 1) {
                                    r++;
                                    c++;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank < row && file < col) {
                                while (r > rank + 1 && c > file + 1) {
                                    r--;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!matches) {
                throw new Exception("invalid bishop move: move path obstructed");
            }
            board[bRank][bFile] = '-';
            board[rank][file] = 'B';
        } else {
            if (blackPieceOnSquare(rank, file)) {
                throw new Exception("invalid bishop move: you cannot capture your own pieces!");
            }
            int bFile = 0; //correct bishop file if exists
            int bRank = 0; //correct bishop file if exists
            if ((rank + file) % 2 == 1) {//white squares
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == 'b' && (row + col) % 2 == 1) {
                            bFile = col;
                            bRank = row;
                            int r = row;
                            int c = col;
                            //check same diagonal first
                            matches=Math.abs(bRank-rank)==Math.abs(bFile-file);
                            //check diagonals clear
                            if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file > col) {
                                while (r < rank - 1 && c < file - 1) {
                                    r++;
                                    c++;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank < row && file < col) {
                                while (r > rank + 1 && c > file + 1) {
                                    r--;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {//black squares
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == 'b' && (row + col) % 2 == 0) {
                            bFile = col;
                            bRank = row;
                            int r = row;
                            int c = col;
                            //check same diagonal first
                            matches=Math.abs(bRank-rank)==Math.abs(bFile-file);
                            //check diagonals clear
                            if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file > col) {
                                while (r < rank - 1 && c < file - 1) {
                                    r++;
                                    c++;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank < row && file < col) {
                                while (r > rank + 1 && c > file + 1) {
                                    r--;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            } else if (rank > row && file < col) {
                                while (r < rank - 1 && c > file + 1) {
                                    r++;
                                    c--;
                                    if (board[r][c] != '-') {
                                        matches = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!matches) {
                throw new Exception("invalid bishop move: move path obstructed");
            }
            board[bRank][bFile] = '-';
            board[rank][file] = 'b';
        }
    }

    public void knightMove(String input, boolean whiteToMove) throws Exception {
        boolean capture = input.contains("x"); // capture or no
        //letter coordinate to int, 97-104 is a-h
        input = input.replace("x", "");
        int file = input.charAt(input.length() - 2) - 97;// last two are always target coordinates
        int rank = input.charAt(input.length() - 1) - 49;

        if (whiteToMove) {
            //check that no piece is there just in case
            if (capture) {
                if (whitePieceOnSquare(rank, file)) {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid king move: you cannot capture your own pieces!");
                }
            } else {
                if (board[rank][file] != '-') {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid king move: square already occupied!");
                }
            }
            int kFile = 0;
            int kRank = 0;
            for (int r = 0; r < dim; r++) {
                for (int c = 0; c < dim; c++) {
                    if (board[r][c] == 'K') {
                        kFile = c;
                        kRank = r;
                    }
                }
            }
            if (Math.abs(kRank - rank) <= 1 && Math.abs(kFile - file) <= 1) {
                board[rank][file] = 'K';
                board[kRank][kFile] = '-';
            } else {
                throw new Exception("invalid king move: king not maneuverable to that square");
            }
        } else {
            //check that no piece is there just in case
            if (capture) {
                if (whitePieceOnSquare(rank, file)) {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid king move: you cannot capture your own pieces!");
                }
            } else {
                if (board[rank][file] != '-') {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid king move: square already occupied!");
                }
            }
            int kFile = 0;
            int kRank = 0;
            for (int r = 0; r < dim; r++) {
                for (int c = 0; c < dim; c++) {
                    if (board[r][c] == 'k') {
                        kFile = c;
                        kRank = r;
                    }
                }
            }
            if (Math.abs(kRank - rank) <= 1 && Math.abs(kFile - file) <= 1) {
                board[rank][file] = 'k';
                board[kRank][kFile] = '-';
            } else {
                throw new Exception("invalid king move: king not maneuverable to that square");
            }
        }
    }

    public void pawnMove(String input, boolean whiteToMove) throws Exception {
        if (whiteToMove) {
            // capture or no
            if (input.split("x").length == 1) {//no capture
                //letter coordinate to int, 97-104 is a-h
                int file = input.charAt(0) - 97;
                int rank = input.charAt(1) - 49;
                //check that no piece is there just in case
                if (board[rank][file] != '-') {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid pawn move: square already occupied!");
                }
                //two move or not
                if (rank == 3) {//yes
                    if (board[rank - 1][file] == 'P') {
                        board[rank - 1][file] = '-';
                        board[rank][file] = 'P';
                    } else if (board[rank - 2][file] == 'P' && board[rank - 1][file] == '-') {
                        board[rank - 2][file] = '-';
                        board[rank][file] = 'P';
                    } else {
                        System.out.println(rank + ", " + file);
                        throw new Exception("invalid pawn move: no pawn maneuverable to that square!");
                    }
                } else {//no
                    //check if promotion
                    if (rank == 7) {
                        board[rank][file] = input.charAt(input.length() - 1);
                        board[rank - 1][file] = '-';
                    } else {
                        if (board[rank - 1][file] == 'P') {
                            board[rank - 1][file] = '-';
                            board[rank][file] = 'P';
                        } else {
                            throw new Exception("invalid pawn move: no pawn maneuverable to that square!");
                        }
                    }
                }
            } else {
                String splitSecond = input.split("x")[1];
                int file = splitSecond.charAt(0) - 97;
                int rank = splitSecond.charAt(1) - 49;
                int originFile = input.split("x")[0].charAt(0) - 97;
                //check no white pieces
                if (whitePieceOnSquare(rank, file)) {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid pawn move: you cannot capture your own pieces!");
                }
                //check if promotion
                if (rank == 7) {
                    board[rank][file] = splitSecond.charAt(splitSecond.length() - 1); //what the pawn turns into
                    board[rank - 1][originFile] = '-';
                } else {
                    board[rank - 1][originFile] = '-';
                    board[rank][file] = 'P';
                }
                // check en peasant
                if (rank == 5 && board[rank][file] == '-' && board[rank - 1][file] == 'p') {//yes
                    board[rank][file] = 'P';
                    board[rank - 1][originFile] = '-';
                    board[rank - 1][file] = '-';
                } else {
                    if (board[rank - 1][originFile] == 'P') {//check the pawn capturing exists
                        board[rank][file] = 'P';
                        board[rank - 1][originFile] = '-';
                    }
                }
            }
        } else {//black to move
            // capture or no
            if (input.split("x").length == 1) {//no capture
                //letter coordinate to int, 97-104 is a-h
                int file = input.charAt(0) - 97;
                int rank = input.charAt(1) - 49;
                //check that no piece is there just in case
                if (board[rank][file] != '-') {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid pawn move: square already occupied!");
                }
                //two move or not
                if (rank == 4) {//yes
                    if (board[rank + 1][file] == 'p') {
                        board[rank + 1][file] = '-';
                        board[rank][file] = 'p';
                    } else if (board[rank + 2][file] == 'p' && board[rank + 1][file] == '-') {
                        board[rank + 2][file] = '-';
                        board[rank][file] = 'p';
                    } else {
                        System.out.println(rank + ", " + file);
                        throw new Exception("invalid pawn move: no pawn maneuverable to that square!");
                    }
                } else {//no
                    //check if promotion
                    if (rank == 0) {
                        board[rank][file] = input.toLowerCase().charAt(input.length() - 1); //toLowerCase because black pieces
                        board[rank + 1][file] = '-';
                    } else {
                        if (board[rank - 1][file] == 'P') {
                            board[rank - 1][file] = '-';
                            board[rank][file] = 'P';
                        } else {
                            throw new Exception("invalid pawn move: no pawn maneuverable to that square!");
                        }
                    }
                }
            } else {
                String splitSecond = input.split("x")[1];
                int file = splitSecond.charAt(0) - 97;
                int rank = splitSecond.charAt(1) - 49;
                int originFile = input.split("x")[0].charAt(0) - 97;
                //check no black pieces
                if (blackPieceOnSquare(rank, file)) {
                    System.out.println(rank + ", " + file);
                    throw new Exception("invalid pawn move: you cannot capture your own pieces!");
                }
                //check if promotion
                if (rank == 0) {
                    board[rank][file] = splitSecond.toLowerCase().charAt(splitSecond.length() - 1); //toLowerCase because black pieces
                    board[rank + 1][originFile] = '-';
                } else {
                    board[rank + 1][originFile] = '-';
                    board[rank][file] = 'p';
                }
                // check en peasant
                if (rank == 2 && board[rank][file] == '-' && board[rank + 1][file] == 'P') {//yes
                    board[rank][file] = 'p';
                    board[rank - 1][file] = '-';
                    board[rank - 1][originFile] = '-';
                } else {
                    if (board[rank - 1][originFile] == 'p') {//check the pawn capturing exists
                        board[rank][file] = 'p';
                        board[rank - 1][originFile] = '-';
                    }
                }
            }
        }
    }

    public void kingMove(String input, boolean whiteToMove) throws Exception {
        //check is castle because of unique notation
        if (input.equalsIgnoreCase("O-O")) {//king side castle
            if(whiteToMove){
                if(whiteCanCastleK && board[0][5]=='-' && board[0][6]=='-'){
                    board[0][4]='-';// empty old piece squares
                    board[0][7]='-';
                    board[0][5]='R';
                    board[0][6]='K';
                }else{
                    throw new Exception("Cannot castle there!");
                }
            }else {//black
                if(blackCanCastleK && board[7][5]=='-' && board[7][6]=='-'){
                    board[7][4]='-';// empty old piece squares
                    board[7][7]='-';
                    board[7][5]='r';
                    board[7][6]='k';
                }else{
                    throw new Exception("Cannot castle there!");
                }
            }
            if(whiteToMove){
                whiteCanCastleK=false;
                whiteCanCastleQ=false;
            }else{
                blackCanCastleK=false;
                blackCanCastleQ=false;
            }
        } else if (input.equalsIgnoreCase("O-O-O")){//queen side castle
            if(whiteToMove){
                if(whiteCanCastleQ && board[0][2]=='-' && board[0][3]=='-'){
                    board[0][0]='-';// empty old piece squares
                    board[0][4]='-';
                    board[0][3]='R';
                    board[0][2]='K';
                }else{
                    throw new Exception("Cannot castle there!");
                }
            }else{//black
                if(blackCanCastleQ && board[7][2]=='-' && board[7][3]=='-'){
                    board[7][0]='-';// empty old piece squares
                    board[7][4]='-';
                    board[7][3]='r';
                    board[7][2]='k';
                }else{
                    throw new Exception("Cannot castle there!");
                }
            }
            if(whiteToMove){
                whiteCanCastleK=false;
                whiteCanCastleQ=false;
            }else{
                blackCanCastleK=false;
                blackCanCastleQ=false;
            }
        }else {
            boolean capture = input.contains("x"); // capture or no
            //letter coordinate to int, 97-104 is a-h
            input = input.replace("x", "");
            int file = input.charAt(input.length() - 2) - 97;// last two are always target coordinates
            int rank = input.charAt(input.length() - 1) - 49;

            if (whiteToMove) {
                //check that no piece is there just in case
                if (capture) {
                    if (whitePieceOnSquare(rank, file)) {
                        System.out.println(rank + ", " + file);
                        throw new Exception("invalid king move: you cannot capture your own pieces!");
                    }
                } else {
                    if (board[rank][file] != '-') {
                        System.out.println(rank + ", " + file);
                        throw new Exception("invalid king move: square already occupied!");
                    }
                }
                int kFile = 0;
                int kRank = 0;
                for (int r = 0; r < dim; r++) {
                    for (int c = 0; c < dim; c++) {
                        if (board[r][c] == 'K') {
                            kFile = c;
                            kRank = r;
                        }
                    }
                }
                if (Math.abs(kRank - rank) <= 1 && Math.abs(kFile - file) <= 1) {
                    board[rank][file] = 'K';
                    board[kRank][kFile] = '-';
                } else {
                    throw new Exception("invalid king move: king not maneuverable to that square");
                }
            } else {
                //check that no piece is there just in case
                if (capture) {
                    if (blackPieceOnSquare(rank, file)) {
                        System.out.println(rank + ", " + file);
                        throw new Exception("invalid king move: you cannot capture your own pieces!");
                    }
                } else {
                    if (board[rank][file] != '-') {
                        System.out.println(rank + ", " + file);
                        throw new Exception("invalid king move: square already occupied!");
                    }
                }
                int kFile = 0;
                int kRank = 0;
                for (int r = 0; r < dim; r++) {
                    for (int c = 0; c < dim; c++) {
                        if (board[r][c] == 'k') {
                            kFile = c;
                            kRank = r;
                        }
                    }
                }
                if (Math.abs(kRank - rank) <= 1 && Math.abs(kFile - file) <= 1) {
                    board[rank][file] = 'k';
                    board[kRank][kFile] = '-';
                } else {
                    throw new Exception("invalid king move: king not maneuverable to that square");
                }
            }
            if (whiteToMove) {
                whiteCanCastleK = false;
                whiteCanCastleQ = false;
            } else {
                blackCanCastleK = false;
                blackCanCastleQ = false;
            }
        }
    }

    public String toString() {
        String ans = "FEN:\t" + boardToFen() + "\n";
//        for(int rank=dim-1; rank>=0; rank--){ //big ting
//            for(int file=0; file<dim; file++){
//                ans+=board[rank][file]+"\t\t";
//            }
//            ans+="\n\n\n";
//        }
        for (int rank = dim - 1; rank >= 0; rank--) {
            for (int file = 0; file < dim; file++) {
                ans += board[rank][file] + "\t";
            }
            ans += "\n";
        }
        return ans;

    }
}
