package cc.cxsj.nju.reversi.chess;

import cc.cxsj.nju.reversi.ui.MainFrame;

import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.config.ServerProperties;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Vector;
import java.util.Random;

public class ChessBoard {
	private static final Logger LOG = Logger.getLogger(Main.class);
	private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	private static final int INTERVAL = Integer.valueOf(ServerProperties.instance().getProperty("play.interval"));
	private static String spliter = "--------------------------------------------";
    // private int[] dx = new int[]{0, 1, 1,  1, 0, -1, -1, -1};
    // private int[] dy = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
	
	// the chess board
	public Square[][] board = new Square[ROWS][COLS];
    // private int lastStepRow = -1, lastStepCol = -1;

	public ChessBoard() {}

	public void generateEmptyChessBoard() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				if(i==ROWS/2&&j==COLS/2) this.board[i][j] = new Square(1);
				else this.board[i][j] = new Square(-1);
            }
        }
        MainFrame.instance().ClearChessBoardUI();
    }

    public boolean inBoard(int row, int col) {
        return (row >= 0 && row < ROWS && col >=0 && col < COLS);
    }
    
	/**
	 * 
	 * @param
	 * @param color 0 is black, 1 is white
	 * @return if
	 * 		   code true : success;
	 * 		   code false : cannot lazi at position (x, y);
	 */
    private boolean step(int x, int y, int color){
    	if(board[x][y].color == -1){
    		lazi(x , y, color);
    		
    		return true;
    	}
    	System.out.println("board[x][y].color != -1"+x+" "+y);
    	return false;
    }
    
	public String step(String step, int stepNum, int color) {
		try {
			Thread.sleep(INTERVAL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("The " + stepNum + " of color " + (color == 0 ? "Black" : "White") + " Step "
				+ " with message: " + step);

		// pos lazi of client
		int desRow = -1, desCol = -1;
		if (((step.charAt(2) == '0' || step.charAt(2) == '1') && step.charAt(3) >= '0' && step.charAt(3) <= '9')
				&& ((step.charAt(4) == '0' || step.charAt(4) == '1') && step.charAt(5) >= '0'
						&& step.charAt(5) <= '9')) {
			desRow = Integer.valueOf(step.substring(2, 4));
			desCol = Integer.valueOf(step.substring(4, 6));
		}

		boolean canLazi = existLazi(color);
		// 客户端判断无棋可下
		if (desRow < 0 || desCol < 0) {
			System.out.println("Client say it cant lazi");
			if (canLazi == false) {
				MainFrame.instance().updateStepInfo((color == 0 ? "Black " : "White ") + "SYNoStep", stepNum);
				return "RYN" + String.valueOf(color); // 客户端对于无法下棋判断正确
			} else {
				// 客户端实际有棋可以下，系统随机找一个可以下棋的位置
				String lazimsg = randomStep(color);
				int randomRow = Integer.valueOf(lazimsg.substring(2, 4));
				int randomCol = Integer.valueOf(lazimsg.substring(4, 6));
				step(randomRow, randomCol, color); // 下棋
				MainFrame.instance()
						.updateStepInfo((color == 0 ? "Black " : "White ") + "SW" + lazimsg.substring(2, 10), stepNum);
				updateUIChessboard();
				return "RWP" + lazimsg.substring(2, 10) + String.valueOf(color);
			}
		} else {
			// 客户端判断有棋可下
			if (canLazi == false) {
				MainFrame.instance().updateStepInfo((color == 0 ? "Black " : "White ") + "SWNoStep", stepNum);
				return "RWN" + String.valueOf(color);
			} else {
				boolean RWP = false;
				String RWPstr = "";
				if ((desRow >= ROWS || desRow < 0) || (desCol >= COLS || desCol < 0)
						|| step(desRow, desCol, color) == false) {
					// System.out.println("Wrong place 1 " + desRow1 + " " + desCol2);
					// 客户端判断的下棋位置不能落子 或者下棋的位置不属于棋盘所在位置
					RWP = true;
					String lazimsg = randomStep(color);
					int randomRow = Integer.valueOf(lazimsg.substring(2, 4));
					int randomCol = Integer.valueOf(lazimsg.substring(4, 6));
					step(randomRow, randomCol, color); // 下棋
					RWPstr += lazimsg.substring(2, 6);
					// System.out.println("Wrong place 1 " + RWPstr);
				} else {
					RWPstr += step.substring(2, 6);
				}
				if (RWP) {
					MainFrame.instance().updateStepInfo((color == 0 ? "Black " : "White ") + "SW" + RWPstr, stepNum);
					updateUIChessboard();
					return "RWP" + RWPstr + String.valueOf(color);
				} else {
					// 客户端判断的下棋位置可以合法落子
					MainFrame.instance()
							.updateStepInfo((color == 0 ? "Black " : "White ") + "SY" + step.substring(2, 6), stepNum);
					updateUIChessboard();

					return "RYP" + step.substring(2, 6) + String.valueOf(color);
				}
			}
		}
	}

	/**
	 * exist an position that can reversi chessman
	 *  
	 */
	public boolean isGameEnd(){
		int cnt=0;
		for(int i=0;i<ROWS;i++) {
			for(int j=0;j<ROWS;j++) {
				if(board[i][j].color==-1) 
					cnt++;	
			}
		}
		if(cnt<2) return true;
		int blackCount=0;
		for(int i=0;i<ROWS;i++) {
			int temp=0;
			for(int j=0;j<COLS;j++) {
				if(board[i][j].color == 0) {
					temp ++;
				}
				else {
					if(temp>blackCount) {
						blackCount = temp;
					}
					temp = 0;
				}
			}
			if(temp>blackCount) {
				blackCount = temp;
			}
		}
		for(int j=0;j<COLS;j++) {
			int temp=0;
			for(int i=0;i<ROWS;i++) {
				if(board[i][j].color == 0) {
					temp ++;
				}
				else {
					if(temp>blackCount) {
						blackCount = temp;
					}
					temp = 0;
				}
			}
			if(temp>blackCount) {
				blackCount = temp;
			}
		}
		
		for(int t=0;t<ROWS;t++) {
			int temp=0;
			for(int i=t;i>=0;i--) {
				if(board[i][t-i].color == 0) {
					temp ++;
				}
				else {
					if(temp>blackCount) {
						blackCount = temp;
					}
					temp = 0;
				}
			}
			if(temp>blackCount) {
				blackCount = temp;
			}
		}
		for(int t=1;t<COLS;t++) {
			int temp=0;
			for(int j=t;j<COLS;j++) {
				if(board[(ROWS-1)-(j-t)][j].color == 0) {
					temp ++;
				}
				else {
					if(temp>blackCount) {
						blackCount = temp;
					}
		        	temp = 0;
				}
			}
			if(temp>blackCount) {
				blackCount = temp;
			}
		}
		for(int t=0;t<COLS;t++) {
			int temp=0;
			for(int j=t;j>=0;j--) {
				if(board[(COLS-1)-(t-j)][j].color == 0) {
					temp ++;
				}
				else {
					if(temp>blackCount) {
						blackCount = temp;
					}
					temp = 0;
				}
			}
			if(temp>blackCount) {
				blackCount = temp;
			}
		}
		for(int t=ROWS-2;t>=0;t--) {
			int temp=0;
			for(int i=t;i>=0;i--) {
				if(board[i][(ROWS-1)-(t-i)].color == 0) {
					temp ++;
				}
				else {
					if(temp>blackCount) {
						blackCount = temp;
					}
		        	temp = 0;
				}
			}
			if(temp>blackCount) {
				blackCount = temp;
			}
		}
		
		// System.out.println("blackCount:"+blackCount);
        if(blackCount>=5) return true;
		
		//////////////
        int whiteCount=0;
		for(int i=0;i<ROWS;i++) {
			int temp=0;
			for(int j=0;j<COLS;j++) {
				if(board[i][j].color == 1) {
					temp ++;
				}
				else {
					if(temp>whiteCount) {
						whiteCount = temp;
					}
					temp = 0;
				}
			}
			if(temp>whiteCount) {
				whiteCount = temp;
			}
		}
		for(int j=0;j<COLS;j++) {
			int temp=0;
			for(int i=0;i<ROWS;i++) {
				if(board[i][j].color == 1) {
					temp ++;
				}
				else {
					if(temp>whiteCount) {
						whiteCount = temp;
					}
					temp = 0;
				}
			}
			if(temp>whiteCount) {
				whiteCount = temp;
			}
		}
		
		for(int t=0;t<ROWS;t++) {
			int temp=0;
			for(int i=t;i>=0;i--) {
				if(board[i][t-i].color == 1) {
					temp ++;
				}
				else {
					if(temp>whiteCount) {
						whiteCount = temp;
					}
					temp = 0;
				}
			}
			if(temp>whiteCount) {
				whiteCount = temp;
			}
		}
		for(int t=1;t<COLS;t++) {
			int temp=0;
			for(int j=t;j<COLS;j++) {
				if(board[(ROWS-1)-(j-t)][j].color == 1) {
					temp ++;
				}
				else {
					if(temp>whiteCount) {
						whiteCount = temp;
					}
		        	temp = 0;
				}
			}
			if(temp>whiteCount) {
				whiteCount = temp;
			}
		}
		for(int t=0;t<COLS;t++) {
			int temp=0;
			for(int j=t;j>=0;j--) {
				if(board[(COLS-1)-(t-j)][j].color == 1) {
					temp ++;
				}
				else {
					if(temp>whiteCount) {
						whiteCount = temp;
					}
					temp = 0;
				}
			}
			if(temp>whiteCount) {
				whiteCount = temp;
			}
		}
		for(int t=ROWS-2;t>=0;t--) {
			int temp=0;
			for(int i=t;i>=0;i--) {
				if(board[i][(ROWS-1)-(t-i)].color == 1) {
					temp ++;
				}
				else {
					if(temp>whiteCount) {
						whiteCount = temp;
					}
		        	temp = 0;
				}
			}
			if(temp>whiteCount) {
				whiteCount = temp;
			}
		}
		// System.out.println("whiteCount:"+whiteCount);
		if(whiteCount>=5) return true;
		// System.out.println("go on");
		return false;
	}


	/**
	 * place a chessman on the position (x, y) 
	 * reversi chessman
	 */
	
	public void lazi(int x, int y, int chessmanColor){
		board[x][y].color = chessmanColor;		
	}
	
	/**
	 * -1 has not winnner, 0 winner is black, 1 winner is white, 2 is draw
	 * 
	 * @return
	 */
	public int isGeneratedWinner(){
		if(isGameEnd()){
			int blackCount = 0, whiteCount = 0;
			
			//count black chessman and white chessman
			for(int i=0;i<ROWS;i++) {
				int temp=0;
				for(int j=0;j<COLS;j++) {
					if(board[i][j].color == 0) {
						temp ++;
					}
					else {
						if(temp>blackCount) {
							blackCount = temp;
						}
						temp = 0;
					}
				}
				if(temp>blackCount) {
					blackCount = temp;
				}
			}
			for(int j=0;j<COLS;j++) {
				int temp=0;
				for(int i=0;i<ROWS;i++) {
					if(board[i][j].color == 0) {
						temp ++;
					}
					else {
						if(temp>blackCount) {
							blackCount = temp;
						}
						temp = 0;
					}
				}
				if(temp>blackCount) {
					blackCount = temp;
				}
			}
			
			for(int t=0;t<ROWS;t++) {
				int temp=0;
				for(int i=t;i>=0;i--) {
					if(board[i][t-i].color == 0) {
						temp ++;
					}
					else {
						if(temp>blackCount) {
							blackCount = temp;
						}
						temp = 0;
					}
				}
				if(temp>blackCount) {
					blackCount = temp;
				}
			}
			for(int t=1;t<COLS;t++) {
				int temp=0;
				for(int j=t;j<COLS;j++) {
					if(board[(ROWS-1)-(j-t)][j].color == 0) {
						temp ++;
					}
					else {
						if(temp>blackCount) {
							blackCount = temp;
						}
			        	temp = 0;
					}
				}
				if(temp>blackCount) {
					blackCount = temp;
				}
			}
			for(int t=0;t<COLS;t++) {
				int temp=0;
				for(int j=t;j>=0;j--) {
					if(board[(COLS-1)-(t-j)][j].color == 0) {
						temp ++;
					}
					else {
						if(temp>blackCount) {
							blackCount = temp;
						}
						temp = 0;
					}
				}
				if(temp>blackCount) {
					blackCount = temp;
				}
			}
			for(int t=ROWS-2;t>=0;t--) {
				int temp=0;
				for(int i=t;i>=0;i--) {
					if(board[i][(ROWS-1)-(t-i)].color == 0) {
						temp ++;
					}
					else {
						if(temp>blackCount) {
							blackCount = temp;
						}
			        	temp = 0;
					}
				}
				if(temp>blackCount) {
					blackCount = temp;
				}
			}
			
			//////////////
			for(int i=0;i<ROWS;i++) {
				int temp=0;
				for(int j=0;j<COLS;j++) {
					if(board[i][j].color == 1) {
						temp ++;
					}
					else {
						if(temp>whiteCount) {
							whiteCount = temp;
						}
						temp = 0;
					}
				}
				if(temp>whiteCount) {
					whiteCount = temp;
				}
			}
			for(int j=0;j<COLS;j++) {
				int temp=0;
				for(int i=0;i<ROWS;i++) {
					if(board[i][j].color == 1) {
						temp ++;
					}
					else {
						if(temp>whiteCount) {
							whiteCount = temp;
						}
						temp = 0;
					}
				}
				if(temp>whiteCount) {
					whiteCount = temp;
				}
			}
			
			for(int t=0;t<ROWS;t++) {
				int temp=0;
				for(int i=t;i>=0;i--) {
					if(board[i][t-i].color == 1) {
						temp ++;
					}
					else {
						if(temp>whiteCount) {
							whiteCount = temp;
						}
						temp = 0;
					}
				}
				if(temp>whiteCount) {
					whiteCount = temp;
				}
			}
			for(int t=1;t<COLS;t++) {
				int temp=0;
				for(int j=t;j<COLS;j++) {
					if(board[(ROWS-1)-(j-t)][j].color == 1) {
						temp ++;
					}
					else {
						if(temp>whiteCount) {
							whiteCount = temp;
						}
			        	temp = 0;
					}
				}
				if(temp>whiteCount) {
					whiteCount = temp;
				}
			}
			for(int t=0;t<COLS;t++) {
				int temp=0;
				for(int j=t;j>=0;j--) {
					if(board[(COLS-1)-(t-j)][j].color == 1) {
						temp ++;
					}
					else {
						if(temp>whiteCount) {
							whiteCount = temp;
						}
						temp = 0;
					}
				}
				if(temp>whiteCount) {
					whiteCount = temp;
				}
			}
			for(int t=ROWS-2;t>=0;t--) {
				int temp=0;
				for(int i=t;i>=0;i--) {
					if(board[i][(ROWS-1)-(t-i)].color == 1) {
						temp ++;
					}
					else {
						if(temp>whiteCount) {
							whiteCount = temp;
						}
			        	temp = 0;
					}
				}
				if(temp>whiteCount) {
					whiteCount = temp;
				}
			}
			
			if(blackCount>=5&&whiteCount<5){
				MainFrame.instance().log("This round BLACK CHESSMAN more than WHITE CHESSMAN");
				System.out.println("This round BLACK CHESSMAN more than WHITE CHESSMAN");

			}
			else if(blackCount<5&&whiteCount>=5){
				MainFrame.instance().log("This round WHITE CHESSMAN more than BLACK CHESSMAN");
				System.out.println("This round WHITE CHESSMAN more than BLACK CHESSMAN");

			}
			else{
				MainFrame.instance().log("This round BLACK AND WHITE CHESSMAN EQUAL");
				System.out.println("This round BLACK AND WHITE CHESSMAN EQUAL");

			}
			if(blackCount>=5&&whiteCount<5) return 1;
			else if(blackCount<5&&whiteCount>=5) return -1;
			else return 0;
		}
		return -100;
	}

	public String toStringToDisplay() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder(spliter);
		sb.append("\n");
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				sb.append(board[i][j].toStringToDisplay());
				sb.append(" ");
			}
			sb.append("\n");
		}
		sb.append(spliter);
		return sb.toString();
	}
	
	public Square[][] getSquares(){
		return this.board;
	}

	/*
	随机下下一步可以下的棋，返回行和列 如row = 2 , col = 3 , 返回 "0203"
	 */
	
	public String randomStep(int color){
		List<String> list = new ArrayList<>();
		for(int r=0 ; r<ROWS ; r++){
			for (int c=0 ; c<COLS ; c++){
				if(board[r][c].color==-1) list.add(int2String(r) + int2String(c));
			}
		}
		if(list.size() == 0){
			return "SN";
		}
		
		Random ran = new Random(System.currentTimeMillis());
		int step=ran.nextInt(list.size());
		
		return "SP"+list.get(step);
	}
	
	// 将整数转化为2位数字符串
	public String int2String(int x){
		if(x<10) return "0"+String.valueOf(x);
		else
			return String.valueOf(x);
	}
	
	public boolean existLazi(int color){
		int cnt=0;
		for(int i = 0; i < ROWS; i ++){
			for(int j = 0; j < COLS; j ++){
				if(board[i][j].color==-1){
					cnt++;
					if (cnt>=2) return true;
				}

			}
		}
		return false;
	}

	// 打印board
	public void printChessBoard(){
//		for (int i=0; i<ROWS; i++) {
//////			for (int j=0; j<COLS; j++) {
//////				if (this.board[i][j].color != -1)
//////					System.out.print(this.board[i][j].color + " ");
//////				else
//////					System.out.print("-" + " ");
//////			}
//////			System.out.println();
//////		}
		System.out.println(toStringToDisplay());
	}

	
	public static void main(String [] args) {
		ChessBoard cb = new ChessBoard();
		cb.generateEmptyChessBoard();
		for(int i = 0 ; i<ChessBoard.ROWS ; i++){
			for(int j=0 ; j<ChessBoard.COLS ; j++){
				cb.board[i][j].reset();
			}
		}
		cb.board[0][0].color = 0;
		cb.board[0][1].color = cb.board[1][0].color = 1;
		cb.board[0][2].color = cb.board[2][0].color = 2;
		System.out.println("here");
		System.out.println(cb.isGameEnd());
	}

	// 更新整个棋盘
	public void updateUIChessboard(){
		for(int i=0 ; i<ChessBoard.ROWS ; i++){
			for(int j=0 ; j<ChessBoard.COLS ; j++){
				MainFrame.instance().updateChessboardOneSquare(i, j, board[i][j].color);
			}
		}
	}
}



