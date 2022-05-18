package se.oru.socially_aware_planner_pkg.im.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

import se.oru.socially_aware_planner_pkg.im.framework.Grounding;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Agent;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Behavior;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Obj;
import se.oru.socially_aware_planner_pkg.im.framework.Institution;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Act;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Art;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Role;
import se.oru.socially_aware_planner_pkg.im.institutions.TheGameInstitution;
import se.oru.socially_aware_planner_pkg.im.institutions.ThePEIS2Domain;
import se.oru.socially_aware_planner_pkg.im.institutions.groundings.TheGameGrounding01;
import se.oru.socially_aware_planner_pkg.im.institutions.groundings.TheGameGroundingTurtlebot1;
import se.oru.socially_aware_planner_pkg.im.utils.MapUtil;
import se.oru.socially_aware_planner_pkg.im.utils.Pair;
import se.oru.socially_aware_planner_pkg.im.utils.RelationWithType;

@SuppressWarnings("rawtypes")
public class DynamicDomainDomain {

	SimpleDomain dom;

	public DynamicDomainDomain(MetaConstraintSolver sp) {

		// Create the institution
		TheGameInstitution theGame = new TheGameInstitution();
		// Create the domain
		ThePEIS2Domain theDomain = new ThePEIS2Domain();
		// Create the grounding
		TheGameGroundingTurtlebot1 theGroundingTurtlebot = new TheGameGroundingTurtlebot1(theGame, theDomain);

		createCustomDomain(sp, theGame, theDomain, theGroundingTurtlebot);
	}

	public SimpleDomain getDomain() {
		return dom;
	}

	private void createEmptyDomain() {
		int[] resourceCaps = new int[0];
		String[] resourceNames = new String[0];
		this.dom = new ProactivePlanningDomain(resourceCaps, resourceNames, "Institutions", null);
		ValueOrderingH valOH = new ValueOrderingH() {
			@Override
			public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
				// Return unifications first
				if (arg0.getAnnotation() != null && arg1.getAnnotation() != null) {
					if (arg0.getAnnotation() instanceof Integer
							&& arg1.getAnnotation() instanceof Integer) {
						int annotation1 = ((Integer) arg0.getAnnotation()).intValue();
						int annotation2 = ((Integer) arg1.getAnnotation()).intValue();
						return annotation2 - annotation1;
					}
				}
				// Return unifications first
				// TODO: maybe this is superfluous...
				return arg0.getVariables().length - arg1.getVariables().length;
			}
		};

		// No variable ordering
		VariableOrderingH varOH = new VariableOrderingH() {
			@Override
			public int compare(ConstraintNetwork o1, ConstraintNetwork o2) {
				return 0;
			}

			@Override
			public void collectData(ConstraintNetwork[] allMetaVariables) {
			}
		};

		dom.setValOH(valOH);
		dom.setVarOH(varOH);
	}

	private void createVariables(Grounding theGrounding) {

		// Internal mechanism - implementing 'always'
		dom.addSensor("command");
		dom.addActuator("IM");
		dom.addContextVar("institution_start");

		// From grounding
		ArrayList<Agent> addedAgents = new ArrayList<Agent>();
		ArrayList<Role> addedRoles = new ArrayList<Role>();
		List<Pair<Role, Agent>> relationsGa = theGrounding.Ga.getRelation();
		for (Pair<Role, Agent> gaPair : relationsGa) {
			Role role = gaPair.getFirst();
			Agent ag = gaPair.getSecond();

			if (!addedAgents.contains(ag)) {
				dom.addActuator((String)ag.getAg());
				addedAgents.add(ag);
			}
		}

	}

	private Map<String, Integer> crateRequirementsList(Institution inst, Grounding theGrounding) {

		Map<String, Integer> indicesRequrementMap = new HashMap<String, Integer>();

		String instName = inst.getInstName();

		// Internal mechanism
		indicesRequrementMap.put("command::" + instName, 0);
		indicesRequrementMap.put("IM::__Restart2__", 1);

		// Use the grounding to create requirements
		List<Pair<Role, Agent>> relationsGa = theGrounding.Ga.getRelation();
		List<Pair<Act, Behavior>> relationsGb = theGrounding.Gb.getRelation();
		List<Pair<Art, Obj>> relationsGo = theGrounding.Go.getRelation();
		Integer counter = 2; // one more after the internal requirements
		for (Pair<Role, Agent> gaPair : relationsGa) {
			for (Pair<Act, Behavior> gbPair : relationsGb) {
				for (Pair<Art, Obj> goPair : relationsGo) {

					Role role = gaPair.getFirst();
					Agent ag = gaPair.getSecond();
					Act act = gbPair.getFirst();
					Behavior beh = gbPair.getSecond();
					Art art = goPair.getFirst();
					Obj obj = goPair.getSecond();

					String requirement = "";
					if (inst.getOBN().containsPair(role, act)) {
						requirement = ((String) ag.getAg()) + "::" + ((String) beh.getBeh()); // DIFFERENCE
						if (inst.getUSN_ARTS().containsPair(act, art)) {
							// TODO: requirement = requirement + "(" + ((String)art.getArt()) + ")";
						}
					}

					if (!requirement.isEmpty() && !indicesRequrementMap.containsKey(requirement)) {
						indicesRequrementMap.put(requirement, counter);
						counter++;
					}
				}
			}
		}

		indicesRequrementMap = MapUtil.sortByValue(indicesRequrementMap);
		return indicesRequrementMap;
	}

	private void createInternalOperator() {
		// --- Internal mechanism OPERATOR ---
		String head = "institution_start::None";
		Map<String, Integer> indicesRequrementMap = new HashMap<String, Integer>();
		indicesRequrementMap.put("command::None", 0);
		indicesRequrementMap.put("IM::__Restart1__", 1);
		indicesRequrementMap = MapUtil.sortByValue(indicesRequrementMap);
		String[] requirementStrings = indicesRequrementMap.keySet().toArray(new String[0]);

		// Constraints (strange ones)
		// Bounds bound1 = new Bounds(1, 3000000);
		// Bounds bound2 = new Bounds(1, 3000000);
		AllenIntervalConstraint[] consFromHeadtoReq = new AllenIntervalConstraint[requirementStrings.length];
		AllenIntervalConstraint con = new AllenIntervalConstraint(
				AllenIntervalConstraint.Type.During /* , bound1, bound2 */);
		consFromHeadtoReq[indicesRequrementMap.get("command::None")] = con;

		SimpleOperator internalOp = new SimpleOperator(head, consFromHeadtoReq, requirementStrings,
				new int[0]);
		dom.addOperator(internalOp);
	}

	private Vector<AdditionalConstraint> createConstraints(Map<String, Integer> indicesRequrementMap, Institution inst,
			Grounding theGrounding) {

		Vector<AdditionalConstraint> additionalConstr = new Vector<AdditionalConstraint>();

		// Internal mechanism
		AllenIntervalConstraint cStarts = new AllenIntervalConstraint(
				AllenIntervalConstraint.Type.Starts);
		AdditionalConstraint ac1 = new AdditionalConstraint(cStarts,
				indicesRequrementMap.get("command::" + inst.getInstName()) + 1, 0);
		additionalConstr.add(ac1);
		// After the last action (how to know which is the last action?)
		// TODO: problem, hardcoded here
		AllenIntervalConstraint cMetByOrAfter = new AllenIntervalConstraint(
				AllenIntervalConstraint.Type.MetByOrAfter);
		AdditionalConstraint ac2 = new AdditionalConstraint(cMetByOrAfter,
				indicesRequrementMap.get("IM::__Restart2__") + 1,
				indicesRequrementMap.get("turtlebot_1::EscapeMove") + 1); // TODO: PROBLEM:
		additionalConstr.add(ac2);
		AllenIntervalConstraint cFinishes = new AllenIntervalConstraint(
				AllenIntervalConstraint.Type.Finishes);
		AdditionalConstraint ac3 = new AdditionalConstraint(cFinishes,
				indicesRequrementMap.get("IM::__Restart2__") + 1, 0);
		additionalConstr.add(ac3);
		Bounds duration1Bound = new Bounds(3000, APSPSolver.INF);
		AllenIntervalConstraint cDuration = new AllenIntervalConstraint(
				AllenIntervalConstraint.Type.Duration, duration1Bound);
		AdditionalConstraint ac4 = new AdditionalConstraint(cDuration, 0, 0);
		additionalConstr.add(ac4);

		RelationWithType<Act, Act, AllenIntervalConstraint> temporalPLN = (RelationWithType<Act, Act, AllenIntervalConstraint>) inst
				.getPLN().get("Temporal");
		
		// For each planning norm:
		List<Pair<Pair<Act, Act>, AllenIntervalConstraint>> temporalRelations = temporalPLN.getRelation();
		for (Pair<Pair<Act, Act>, AllenIntervalConstraint> wholePair : temporalRelations) {
			Pair<Act, Act> actPair = wholePair.getFirst();
			AllenIntervalConstraint con = wholePair.getSecond();
			String actFrom = (String) actPair.getFirst().getAct();
			String actTo = (String) actPair.getSecond().getAct();
			
			// Find two requirements (from and to)
			for (String requirementFrom : indicesRequrementMap.keySet()){
				// Take all behaviors from act TODO and check with behaviors
				if (checkIfGroundedActIsInRequirement(requirementFrom, actFrom, theGrounding)) {
					for (String requirementTo : indicesRequrementMap.keySet()){
						if (checkIfGroundedActIsInRequirement(requirementTo, actTo, theGrounding)){
							// Add constraint, BUT with some RULES:
							// 1. NO CONSTRAINT, if DIFFERENT AGENTS, SAME ROLES
							String agentNameFrom = getRobotNameFromRequirement(requirementFrom);
							String agentNameTo = getRobotNameFromRequirement(requirementTo);
							if (!agentNameFrom.equals(agentNameTo)) {
								// Get the roles, to see if they are the same...
								Boolean sameRoles = areTheSameRoles(agentNameFrom, agentNameTo, theGrounding);
								if (sameRoles) continue;
							}
							
							AdditionalConstraint addCon = new AdditionalConstraint(con,
									indicesRequrementMap.get(requirementFrom) + 1,
									indicesRequrementMap.get(requirementTo) + 1);
							additionalConstr.add(addCon);
						}
					}
				}
			}
		}

		return additionalConstr;
	}
	
	private Boolean areTheSameRoles(String agent1, String agent2, Grounding grounding){
		
		List<Pair<Role, Agent>> relationsGa = grounding.Ga.getRelation();
		
		// Find the first agent;
		for (Pair<Role, Agent> gaPair : relationsGa) {
			String pairRole1 = (String) gaPair.getFirst().getRole();
			String pairAgent1 = (String) gaPair.getSecond().getAg();
			
			if (pairAgent1.equals(agent1)) {
				// First found, find the second agent
				for (Pair<Role, Agent> gaPair2 : relationsGa) {
					String pairRole2 = (String) gaPair2.getFirst().getRole();
					String pairAgent2 = (String) gaPair2.getSecond().getAg();
					
					if (pairAgent2.equals(agent2)){
						// Second found, Check if they have the same roles?
						if (pairRole1.equals(pairRole2)){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private Boolean checkIfGroundedActIsInRequirement(String requirement, String act, Grounding grounding) {
		
		// Get behavior name
		Integer colIndex = requirement.lastIndexOf("::");
		String beh = requirement.substring(colIndex+2);
		
		// Check if the requirement contains grounded act (as beh).
		List<Pair<Act, Behavior>> relationsGb = grounding.Gb.getRelation();
		for (Pair<Act, Behavior> gbPair : relationsGb){
			String pairAct = (String) gbPair.getFirst().getAct();
			String pairBeh = (String) gbPair.getSecond().getBeh();
			if (act.equals(pairAct) && beh.equals(pairBeh))
				return true;
		}
		
		return false;
	}
	
	private String getRobotNameFromRequirement(String requirement)	{
		
		Integer colIndex = requirement.lastIndexOf("::");
		String agent = requirement.substring(0, colIndex);
		
		return agent;
	}
	
	private Boolean isReqAct(String requirement, String act){
		// Gives the index before ::, so increase it with 2
		Integer colIndex = requirement.lastIndexOf("::");
		String reqAct = requirement.substring(colIndex+2);
		
		if (reqAct.equals(act))
			return true;
		
		return false;
	}

	private SimpleDomain createCustomDomain(MetaConstraintSolver sp, Institution inst,
			InstDomain theDomain, Grounding theGrounding) {

		// Check if the grounding is admissible
		if (theGrounding.isAdmissibleGrounding()) {
			System.out.println("\nThe grounding is Admissible!!!");
		} else {
			System.out.println("\nThe grounding is NOT Admissible!!! Stopping...");
			throw new Error("The institution is not admissible");
		}

		// Create an empty domain
		createEmptyDomain();

		// Create sensors, actuators, context variables
		createVariables(theGrounding);

		String head = "institution_start::" + inst.getInstName();
		Map<String, Integer> indicesRequrementMap = crateRequirementsList(inst, theGrounding);

		Vector<AdditionalConstraint> additionalConstr = createConstraints(indicesRequrementMap, inst, theGrounding);

		// Requirements string NOTE: careful with the indices
		String[] requirementStrings = indicesRequrementMap.keySet().toArray(new String[0]);
		AllenIntervalConstraint[] consFromHeadtoReq = new AllenIntervalConstraint[requirementStrings.length];

		SimpleOperator instOp = new SimpleOperator(head, consFromHeadtoReq, requirementStrings,
				new int[0]);
		
		for (AdditionalConstraint ac : additionalConstr) {
			ac.addAdditionalConstraint(instOp);
		}

		dom.addOperator(instOp);

		createInternalOperator();

		sp.addMetaConstraint(dom);
		return dom;
	}

	class AdditionalConstraint {
		AllenIntervalConstraint con;
		int from, to;

		public AdditionalConstraint(AllenIntervalConstraint con, int from, int to) {
			this.con = con;
			this.from = from;
			this.to = to;
		}

		public void addAdditionalConstraint(SimpleOperator op) {
			op.addConstraint(con, from, to);
		}
	}

}
