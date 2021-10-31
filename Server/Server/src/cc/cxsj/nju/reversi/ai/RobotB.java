package cc.cxsj.nju.reversi.ai;

import cc.cxsj.nju.reversi.chess.Square;
import cc.cxsj.nju.reversi.config.ServerProperties;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;


public class RobotB extends ReversiRobotAI{

    private boolean full = false;
    private Square[][] tboard;
    private int[] dx = new int[]{0, 1, 1, 1};
    private int[] dy = new int[]{1, 1, 0, -1};
    private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
    private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
    private String[] path;
    private int depth;
	
	@Override
	public String step() {
		// TODO �Զ����ɵķ������
		boolean isThisStep = false;
		
		// find the first empty grid and put down
		if (!isThisStep) {
			isThisStep = SearchStep();
		}

		// no step is last step
		if (!isThisStep) {
			noStep();
			System.out.println("Robot no step");
		}
		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO 自动生成的 catch 块
//			e.printStackTrace();
//		}
		
		return thisStep;
	}
	
	private boolean SearchStep() {
      
        for (int i = 0; i < ROWS; i++) {
	        for (int j = 0; j < COLS; j++) {
	        	if(chessboard.board[i][j].color == -1)
	        		{chessboard.board[i][j].empty = true;}
	        	else
	        		{chessboard.board[i][j].empty = false;}
	        }
	    }

        tboard = chessboard.board;
		long max_score = - Integer.MAX_VALUE,score1 = 0,score2=0;
		int row = 0,col = 0;
		for(int r=0 ; r<ROWS ; r++){
			for (int c=0 ; c<COLS ; c++){
				if(tboard[r][c].empty) {
					tboard[r][c].color = ownColor;
					tboard[r][c].empty = false;
					score1 = eval1(ownColor);
					if(score1 > max_score){
						row = r;
						col = c;
						max_score = score1;
					}
                    tboard[r][c].color = oppositeColor;
                    score2 = eval2(oppositeColor);
                    if(score2 > max_score){
                        row = r;
                        col = c;
                        max_score = score2;
                    }
					tboard[r][c].color = -1;
					tboard[r][c].empty = true;
				}
			}
		}
        if(max_score == - Integer.MAX_VALUE){
			return false;
		}
        if(max_score == 0){
            Random rand = new Random(System.currentTimeMillis());
		    List<Integer> rows = new ArrayList<>();
		    List<Integer> cols = new ArrayList<>();
		    for(int r=0 ; r<ROWS ; r++){
			    for (int c=0 ; c<COLS ; c++){
				    if(chessboard.board[r][c].color==-1) {
					    rows.add(r);
					    cols.add(c);
				    }
			    }
		    }
		    int step=rand.nextInt(rows.size());
		    row=rows.get(step);
		    col=cols.get(step);
        }
	    System.out.println("robot chess at: " + row + " - " + col);
		putDown(row, col);
		
        return true;
    }

    private LinkedList<LinkedList<Integer> > flat() {
        LinkedList<LinkedList<Integer> > linkedLists = new LinkedList<>();
        // 水平
        for (int i = 0; i < ROWS; i++) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int j = 0; j < COLS; j++) {
                list.add(tboard[i][j].color);
            }
            linkedLists.add(list);
        }
        // 垂直
        for (int i = 0; i < COLS; i++) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int j = 0; j < ROWS; j++) {
                list.add(tboard[j][i].color);
            }
            linkedLists.add(list);
        }
        // 对角
        int dx = 1, dy = 1;
        for (int j = COLS-1, i = 0; j >= 0; j--) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int k = 0; ; k++) {
                int nx = i + dx * k;
                int ny = j + dy * k;
                if (!inBoard(nx, ny))
                    break;
                list.add(tboard[nx][ny].color);
            }
            linkedLists.add(list);
        }
        for (int j = 0, i = 1; i < ROWS; i++) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int k = 0; ; k++) {
                int nx = i + dx * k;
                int ny = j + dy * k;
                if (!inBoard(nx, ny))
                    break;
                list.add(tboard[nx][ny].color);
            }
            linkedLists.add(list);
        }
        // 反对角
        dx = 1;
        dy = -1;
        for (int j = COLS-1, i = 0; j >= 0; j--) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int k = 0; ; k++) {
                int nx = i + dx * k;
                int ny = j + dy * k;
                if (!inBoard(nx, ny))
                    break;
                list.add(tboard[nx][ny].color);
            }
            linkedLists.add(list);
        }
        for (int j = 0, i = 1; i < ROWS; i++) {
            LinkedList<Integer> list = new LinkedList<>();
            for (int k = 0; ; k++) {
                int nx = i + dx * k;
                int ny = j + dy * k;
                if (!inBoard(nx, ny))
                    break;
                list.add(tboard[nx][ny].color);
            }
            linkedLists.add(list);
        }
        return linkedLists;
    }

    public boolean inBoard(int row, int col) {
        return (row >= 0 && row < ROWS && col >= 0 && col < COLS);
    }

    private long eval1(int color){
        LinkedList<LinkedList<Integer> > lines = flat();
        for (int _l = 0; _l < lines.size(); _l++) {
            LinkedList<Integer> list = lines.get(_l);
            //自己成五
            for (int i = 1; i < list.size()-4; i++) {
                if (list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == color
                        && list.get(i+4) == color)
                    return 10000000;
            }
            //自己活四
            for (int i = 1; i < list.size()-4; i++) {
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == color
                        && list.get(i+4) == -1)
                    return 100000;
            }
            //自己活三
            for (int i = 1; i < list.size()-3; i++) {
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == -1)
                    return 1000;
            }
        }
        return 0;
    }

    private long eval2(int color){
        LinkedList<LinkedList<Integer> > lines = flat();
        for (int _l = 0; _l < lines.size(); _l++) {
            LinkedList<Integer> list = lines.get(_l);
            //对手眠五
            for (int i = 0; i < list.size()-4; i++) {
                if ((i >= 1 && list.get(i-1) == -1) && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == color && list.get(i+4) == color && (i+5 == list.size() || list.get(i+5) == 1-color))
                    return 1000000;
            }
            //对手活四
            for (int i = 1; i < list.size()-4; i++) {
                if (list.get(i-1) == -1 && list.get(i) == color && list.get(i+1) == color
                        && list.get(i+2) == color && list.get(i+3) == color
                        && list.get(i+4) == -1)
                    return 10000;
            }
        }
        return 0;
    }

}
