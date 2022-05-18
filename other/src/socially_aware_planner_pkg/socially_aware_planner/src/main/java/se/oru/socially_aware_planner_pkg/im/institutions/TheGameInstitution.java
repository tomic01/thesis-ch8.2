package se.oru.socially_aware_planner_pkg.im.institutions;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

import se.oru.socially_aware_planner_pkg.im.framework.Institution;
import se.oru.socially_aware_planner_pkg.im.utils.RelationWithType;

public class TheGameInstitution extends Institution<String, String, String> {

	// This should be parsed from the external file - institution specification
	public TheGameInstitution() {
		super();
		
		this.setInstName("TheGame");

		// Create Roles
		Role Runner = new Role("Runner");
		Role Catcher = new Role("Catcher");
		this.RoleSet.put("Runner", Runner);
		this.setRoleCardinality(Runner, 1, 1);
		this.RoleSet.put("Catcher", Catcher);
		this.setRoleCardinality(Catcher, 1, 1);

		// Create Acts (with Goals)
		// TODO: NOTE: ST: ADD SEPARATE SET FOR GOALS!!! 
		// ADMISIBILITY MAY BE BROKEN AND ACTIONS ARE NOT GOALS ANYWAYS!
		Act EscapeMove = new Act("EscapeMove");
		Act SayWord = new Act("SayWord");
		Act Catch = new Act("Catch");
		Act Goal_VisitAllLetters = new Act("Goal_VisitAllLetters");
		Act Goal_Catch = new Act("Goal_Catch");
		this.ActsSet.put("EscapeMove", EscapeMove);
		this.ActsSet.put("SayWord", SayWord);
		this.ActsSet.put("Catch", Catch);
		this.ActsSet.put("Goal_VisitAllLetters", Goal_VisitAllLetters);
		this.ActsSet.put("Goal_Catch", Goal_Catch);

		// Create Artifacts
		Art Letter = new Art("Letter");
		Art Grid = new Art("Grid");
		this.ArtsSet.put("Letter", Letter);
		this.ArtsSet.put("Grid", Grid);

		// Create OBN Relations
		this.OBN.addRelation(Runner, EscapeMove);
		this.OBN.addRelation(Runner, SayWord);
		this.OBN.addRelation(Catcher, Catch);
		this.OBN.addRelation(Runner, Goal_VisitAllLetters); // GOAL TODO theory?
		this.OBN.addRelation(Catcher, Goal_Catch); // GOAL TODO

		// Create USB Relations
		this.USN_ARTS.addRelation(EscapeMove, Letter);
		// this.USN_ARTS.addRelation(EscapeMove, Grid);
		// this.USN_ARTS.addRelation(SayWord, Letter);
		//this.USN_ARTS.addRelation(Catch, Grid);
		// USB_ROLE <none>
		
		// Create PLN norms
		// Temporal Norms  (Those does not include HEAD)
		RelationWithType<Act,Act,AllenIntervalConstraint> temporalPLN = new RelationWithType<Act, Act, AllenIntervalConstraint>();
		
		// 
		AllenIntervalConstraint metByOrAfter = new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy);
		temporalPLN.addRelation(this.ActsSet.get("EscapeMove"), this.ActsSet.get("SayWord"), metByOrAfter);
		//
		Bounds duration1Bound = new Bounds(5000,APSPSolver.INF);
		Bounds durationSayBound = new Bounds(1000,2000);
		AllenIntervalConstraint duratinSay = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, durationSayBound);
		AllenIntervalConstraint duratinMove = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, duration1Bound);
		AllenIntervalConstraint duratinCatch = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, duration1Bound);
		temporalPLN.addRelation(this.ActsSet.get("SayWord"),  this.ActsSet.get("SayWord"), duratinSay);
		temporalPLN.addRelation(this.ActsSet.get("EscapeMove"),  this.ActsSet.get("EscapeMove"), duratinMove);
		temporalPLN.addRelation(this.ActsSet.get("Catch"),  this.ActsSet.get("Catch"), duratinCatch);
		
		// Create Temporal Norms
		this.PLN.put("Temporal", temporalPLN);
		
	}
}
