package se.oru.socially_aware_planner_pkg.im.institutions.groundings;

import se.oru.socially_aware_planner_pkg.im.framework.Grounding;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Agent;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Behavior;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Obj;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Act;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Art;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Role;
import se.oru.socially_aware_planner_pkg.im.institutions.TheGameInstitution;
import se.oru.socially_aware_planner_pkg.im.institutions.ThePEIS2Domain;
import se.oru.socially_aware_planner_pkg.im.utils.Relation;

public class TheGameGroundingTurtlebot1 extends Grounding {

	// Create Grounding
	public TheGameGroundingTurtlebot1(TheGameInstitution gameInst, ThePEIS2Domain theDomain) {
		super(gameInst, theDomain);

		// Ga
		addRelationGa("Runner", "turtlebot_1");
		addRelationGa("Catcher", "mbot11");
		// Gb
		addRelationGb("SayWord", "sayWord");
		addRelationGb("EscapeMove", "moveTo");
		addRelationGb("Catch", "gradientMoveTo");
		// Go
		addRelationGo("Letter", "positionA");
		addRelationGo("Letter", "positionB");
		addRelationGo("Letter", "positionC");
		addRelationGo("Grid", "oMap");
		
	}

}
