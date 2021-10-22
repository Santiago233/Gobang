package cc.cxsj.nju.reversi.ai;

import cc.cxsj.nju.reversi.chess.ChessBoard;
import cc.cxsj.nju.reversi.chess.Square;
import cc.cxsj.nju.reversi.config.ServerProperties;

public abstract class ReversiRobotAI {

	protected static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	protected static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	
	protected ChessBoard chessboard;
	protected int ownColor = -1;
	protected int oppositeColor = -1;
	protected int round = 0;
	
	protected String thisStep;
	
	
	public ReversiRobotAI() {           // ά����������
		chessboard = new ChessBoard();
		chessboard.generateEmptyChessBoard();
	}
	
	protected void putDown(int row1, int col1, int row2, int col2) {
		String step = "SP";
		step += String.format("%02d", row1);
		step += String.format("%02d", col1);
		step += String.format("%02d", row2);
		step += String.format("%02d", col2);
		thisStep = step;
	}
	
	protected void noStep() {
		thisStep = "SP-1-1-1-1";
	}
	
	protected void updateLastStep(String step) {   // P08070
		
    	System.out.println(step);
        int desRow1 = Integer.valueOf(step.substring(0, 2)), desCol1 = Integer.valueOf(step.substring(2, 4));
        int desRow2 = Integer.valueOf(step.substring(4, 6)), desCol2 = Integer.valueOf(step.substring(6, 8));
        int color = step.charAt(8) - '0';
        chessboard.lazi(desRow1, desCol1, color);
        chessboard.lazi(desRow2, desCol2, color);
        //chessboard.printChessBoard();
               
	}
	
	protected void roundStart(int color) {
		// System.out.println("Round Start, I'am " + color);
		this.ownColor = color;
		this.oppositeColor = 1 - color;
	}
	
	protected void roundOver() {
		this.round++;
		chessboard.generateEmptyChessBoard();
	}
	
	protected void gameOver() {
		
	}
	
	/**
	 * receive msg
	 * 
	 * @param msg
	 */
	public void receiveMsg(String msg) {
		System.out.println("Robot message: " + msg);
		switch (msg.charAt(0)) {
            case 'B':
                // one round begin and the piece color assigned
                System.out.println("Receive Msg Round Start~");
                switch (msg.charAt(1)) {
                    case 'B':
                        roundStart(0);
                        break;
                    case 'W':
                        roundStart(1);
                        break;
                    default:
                        break;
                }
                break;
            case 'R':
                // return code
                switch (msg.charAt(2)) {
                    case 'P':
                        // step is valid and update board
                        updateLastStep(msg.substring(3));
                        break;
                    default:
                        System.out.println("Round " + round +  " Error Code " + msg.charAt(1));
                        break;
                }
                break;
            case 'E':     // "E0": game over   "E1": round over
                // round or contest end
                switch (msg.charAt(1)) {
                    case '0':
                        gameOver();
                        break;
                    case '1':
                        roundOver();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
		}
	}
	/**
	 * the next step, you must be sure that the lastStep is returned
	 * 
	 * @return
	 */
	public abstract String step();
}