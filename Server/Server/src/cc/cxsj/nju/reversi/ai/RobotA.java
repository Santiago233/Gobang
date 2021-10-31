package cc.cxsj.nju.reversi.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RobotA extends ReversiRobotAI{

	
	@Override
	public String step() {
		// TODO �Զ����ɵķ������
		boolean isThisStep = false;
		
		// find the first empty grid and put down
		if (!isThisStep) {
			isThisStep = randomPutDown();
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
	
	private boolean randomPutDown() {
      
		if(!chessboard.existLazi(ownColor)){
        	return false;
        }
		System.out.println("Robot Travel PutDown");
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
		// System.out.println("Robot Travel PutDown Size"+rows.size());
		int step=rand.nextInt(rows.size());
		int r=rows.get(step);
		int c=cols.get(step);
        System.out.println("robot chess at: " + r + " - " + c);
		putDown(r, c);
		
        return true;
    }

}
