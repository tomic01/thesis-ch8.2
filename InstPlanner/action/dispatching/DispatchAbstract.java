package se.oru.socially_aware_planner_pkg.im.action.dispatching;

import java.util.HashMap;

import org.metacsp.multi.activity.SymbolicVariableActivity;

public abstract class DispatchAbstract {

	// Map associating behaviors ID with the planer Activity
	public static HashMap<String, SymbolicVariableActivity> ID_Activity_Map = new HashMap<String, SymbolicVariableActivity>();

	//protected abstract void dispatch(SymbolicVariableActivity activity);
}
