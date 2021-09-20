import javax.crypto.BadPaddingException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);
        String in="0";
        String testFen = "1rq3k1/Q1p2p2/3pnbp1/4p2p/2N1P2P/3PB1P1/2P2P2/R5K1 b - - 11 31";

        Board test = new Board("1nbqkbn1/8/3r4/8/2rR4/3R4/8/1NBQKBN1 w - - 0 1");
        Board fenPos1 = new Board(testFen);
        Board fenPos2 = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        System.out.println(test);
        System.out.println(fenPos1);
        System.out.println(fenPos2);

        while (!in.equals("quit")){
            System.out.println(fenPos2);
            in = s.nextLine();
            fenPos2.move(in);
            System.out.flush();
        }
    }
}
