package se.oru.socially_aware_planner_pkg.im.tests;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.multi.activity.ActivityNetworkSolver;

import se.oru.socially_aware_planner_pkg.im.action.DynamicDomainDomainGoals;
import se.oru.socially_aware_planner_pkg.im.action.DynamicDomainInstitution;
import se.oru.socially_aware_planner_pkg.im.action.DynamicDomainDomain;


public class TestCustomDomain {

	public static void main(String[] args) {
		
		SimplePlanner planner = new SimplePlanner(1000,100000,0);
		//new DynamicDomainDomain(planner);
		new DynamicDomainDomainGoals(planner);
		
		final ActivityNetworkSolver activitySolver = (ActivityNetworkSolver) planner.getConstraintSolvers()[0];
		ConstraintNetwork.draw(activitySolver.getConstraintNetwork());
		
		return;

	}
	

}
