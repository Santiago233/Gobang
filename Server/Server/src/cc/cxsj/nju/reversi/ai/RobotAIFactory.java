package cc.cxsj.nju.reversi.ai;

import cc.cxsj.nju.reversi.Main;
import org.apache.log4j.Logger;

public class RobotAIFactory {
	
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	public static ReversiRobotAI produceRobotAIof(RobotAIModel model) {
		switch (model) {
            case RobotA:
            	return new RobotA();
            case RobotB:
            	return new RobotB();
            case RobotC:
            	return new RobotC();
			default:
				LOG.info("Robot Factory can not produce this model Robot!");
				System.exit(0);
			}
		return null;
	}
}
