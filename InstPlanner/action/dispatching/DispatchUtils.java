package se.oru.socially_aware_planner_pkg.im.action.dispatching;

import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.multi.activity.SymbolicVariableActivity;

import se.oru.socially_aware_planner_pkg.im.action.InstitutionNode;

public class DispatchUtils {
	// Calculates the unique identifier for currently executing behavior on the
	// specified robot.
//	public static Integer calcBehaviorUID(Byte robotID, RobotBehaviors robotBehavior) {
//		return robotID.intValue() * 1000 + robotBehavior.getCode() % 1000;
//	}

	public static void stopActivity(SymbolicVariableActivity act) {
		DispatchingFunction functToStop = InstitutionNode.animator.getDispatcher().getDispatchingFunction(act.getComponent());
		functToStop.finish(act);
	}

	public static void stopExecutingActivity(String uniqueBehName) {
		if (DispatchAbstract.ID_Activity_Map.containsKey(uniqueBehName)) {
			SymbolicVariableActivity executingAct = DispatchAbstract.ID_Activity_Map.get(uniqueBehName);
			stopActivity(executingAct);
			DispatchAbstract.ID_Activity_Map.remove(uniqueBehName);
		}
	}

}
