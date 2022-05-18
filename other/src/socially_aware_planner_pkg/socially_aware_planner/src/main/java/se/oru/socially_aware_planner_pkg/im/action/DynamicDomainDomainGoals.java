package se.oru.socially_aware_planner_pkg.im.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

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
import se.oru.socially_aware_planner_pkg.im.institutions.groundings.TheGameGroundingTurtlebot1;
import se.oru.socially_aware_planner_pkg.im.utils.MapUtil;
import se.oru.socially_aware_planner_pkg.im.utils.Pair;
import se.oru.socially_aware_planner_pkg.im.utils.RelationWithType;

@SuppressWarnings("rawtypes")
public class DynamicDomainDomainGoals {

	SimpleDomain dom;

	public DynamicDomainDomainGoals(MetaConstraintSolver sp) {

		// Create the institution
		TheGameInstitution theGame = new TheGameInstitution();
		// Create the domain
		ThePEIS2Domain theDomain = new ThePEIS2Domain();
		// Create the grounding
		TheGameGroundingTurtlebot1 theGroundingTurtlebot = new TheGameGroundingTurtlebot1(theGame,
				theDomain);

		createCustomDomain(sp, theGame, theDomain, theGroundingTurtlebot);
	}

	public SimpleDomain getDomain() {
		return dom;
	}

	private void createEmptyDomain(List<String> actuatorList) {
		// Internal mechanism for actuator resources, each actuator has a
		// default mobility resource
		List<String> resourceList = new ArrayList<String>(actuatorList);
		// All actuators are also resources and name is changed accordingly
		int[] resourceCaps = new int[resourceList.size()];
		for (int i = 0; i < resourceList.size(); i++) {
			String actuator = resourceList.get(i);
			actuator = actuator + "_resource";
			resourceList.set(i, actuator);
			resourceCaps[i] = 1;
		}

		String[] resourceNames = resourceList.toArray(new String[0]);
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

	private void createVariables(Grounding theGrounding, List<String> actuatorList) {
		// Internal mechanism, START
		dom.addSensor("command");
		dom.addContextVar("institution_start");

		// Actuators
		for (String actuator : actuatorList) {
			dom.addActuator(actuator);
		}
	}

	// Create operator with the required goals and return the list of goals
	// Create also the start conditions and a list of all other requirements
	private List<String> createGoals(Institution inst, Grounding theGrounding) {

		List<String> goals = new ArrayList<String>();

		String instName = inst.getInstName();
		String head = "institution_start::" + instName;

		Map<String, Integer> indicesRequrementMap = new HashMap<String, Integer>();
		indicesRequrementMap.put("command::" + instName, 0);

		Integer counter = 1; // Since we already have 'command::<inst_name>"
		List<Pair<Role, Act>> oblNorms = inst.getOBN().getRelation();
		for (Pair<Role, Act> pair : oblNorms) {
			Act act = pair.getSecond();
			String actStr = (String) act.getAct();
			// if act is GOAL
			if (isActGoal(actStr)) {
				Role role = pair.getFirst();
				List<Agent> rolAgents = theGrounding.getAgentsFromGa(role);
				for (Agent ag : rolAgents) {
					String requirement = ((String) ag.getAg()) + "::" + actStr;

					indicesRequrementMap.put(requirement, counter);
					counter++;
					goals.add(requirement);
				}
			}
		}

		// Create operator with the goals that have to be satisfied
		createOperatorHelper(head, indicesRequrementMap, null, null, null);
		return goals;
	}

	private Boolean isActGoal(String actName) {
		Integer colIndex = actName.lastIndexOf("Goal_");
		if (colIndex == 0) {
			return true;
		}

		return false;
	}

	// For every goal, create an operator...
	// Here the goal is special ACT, that does not have grounding.
	// In this example, goal is the same as 'EscapeMove' action.
	private List<String> createGoalOperators(List<String> goals, Institution inst,
			Grounding theGrounding) {

		Set<String> required_behaviors_set = new HashSet<String>();

		for (String goal : goals) {
			String head = goal;
			String agentStr = getRobotNameFromRequirement(goal);
			String goalActStr = getBehNameFromRequirement(goal);
			Integer counter = 0;

			// TODO: QA DISSCUSION This should be written in the institution?
			// GOAL COORESPONDS TO THE INSTITUIONS ACT
			// IDEA IS TO EXPRESS THIS GOAL TO ALL GROUNDED ELEMENTS
			// TODO HARD
			if (goalActStr.equals("Goal_VisitAllLetters")) {
				goalActStr = "EscapeMove";
			} else if (goalActStr.equals("Goal_Catch")) {
				goalActStr = "Catch";
			}

			// Create requirements from given act and grounding.
			Map<String, Integer> indicesRequrementMap = requirementsFromAct(goalActStr, counter,
					inst, theGrounding);

			// CREATE CONSTRAINTS (HEAD -> REQUIREMENTS)
			String[] requirementStrings = indicesRequrementMap.keySet().toArray(new String[0]);
			AllenIntervalConstraint[] consFromHeadtoReq = new AllenIntervalConstraint[requirementStrings.length];

			// From head to ALL requirements
			for (Entry<String, Integer> entry : indicesRequrementMap.entrySet()) {
				AllenIntervalConstraint con = new AllenIntervalConstraint(
						AllenIntervalConstraint.Type.MetByOrAfter);
				String requirementName = entry.getKey();
				consFromHeadtoReq[indicesRequrementMap.get(requirementName)] = con;
			}

			// CREATE ADDITIONAL CONSTRAINTS
			Vector<AdditionalConstraint> additionalConstraints = createConstraints(
					indicesRequrementMap, inst, theGrounding);

			createOperatorHelper(head, indicesRequrementMap, consFromHeadtoReq,
					additionalConstraints, null);

			// Use the requirementStrings to fill in list of all behavior
			// requirements
			// no duplicates
			required_behaviors_set.addAll(Arrays.asList(requirementStrings));
		}

		return new ArrayList<String>(required_behaviors_set);
	}

	private Map<String, Integer> requirementsFromAct(String actStr, Integer counter,
			Institution inst, Grounding theGrounding) {
		// We choose one grounded behavior, to add in the domain
		Act goalAct = (Act) inst.getActsSet().get(actStr);
		List<Behavior> behToUseList = theGrounding.getBehFromGb(goalAct);
		Behavior behToUse = behToUseList.get(0);

		// ADD REQUIREMETNS

		// Find all related agents to this goalACT
		List<Role> relatedRoles = inst.getOBN().getAllFirsts(goalAct);
		List<Agent> relatedAgents = new ArrayList<Agent>();
		for (Role rol : relatedRoles) {
			List<Agent> agents = theGrounding.getAgentsFromGa(rol);
			relatedAgents.addAll(agents);
		}

		// Find all related objects to this goalAct
		List<Art> relatedArts = inst.getUSN_ARTS().getAllSeconds(goalAct);
		List<Obj> relatedObj = new ArrayList<Obj>();
		for (Art art : relatedArts) {
			List<Obj> objects = theGrounding.getObjFromGo(art);
			relatedObj.addAll(objects);
		}

		// Add requirements
		Map<String, Integer> indicesRequrementMap = new HashMap<String, Integer>();
		for (Agent relAg : relatedAgents) {
			String requirement = ((String) relAg.getAg()) + "::" + ((String) behToUse.getBeh());

			if (!relatedObj.isEmpty()) {
				for (Obj relObj : relatedObj) {
					String requirementExtended = requirement + "(" + ((String) relObj.getObj())
							+ ")";
					indicesRequrementMap.put(requirementExtended, counter);
					counter++;
				}
			} else {
				indicesRequrementMap.put(requirement, counter);
				counter++;
			}
		}
		return indicesRequrementMap;
	}

	// This will search through PLN norms, and add constraints between given
	// requirements (excluding HEAD->REQUIRMENT)
	private Vector<AdditionalConstraint> createConstraints(
			Map<String, Integer> indicesRequrementMap, Institution inst, Grounding theGrounding) {

		// Create additional constraints, they will be filled in and returned
		Vector<AdditionalConstraint> additionalConstr = new Vector<AdditionalConstraint>();

		// Get PLN norms (Temporal Planning norms)
		RelationWithType<Act, Act, AllenIntervalConstraint> temporalPLN = (RelationWithType<Act, Act, AllenIntervalConstraint>) inst
				.getPLN().get("Temporal");

		// For each planning norm:
		List<Pair<Pair<Act, Act>, AllenIntervalConstraint>> temporalRelations = temporalPLN
				.getRelation();
		for (Pair<Pair<Act, Act>, AllenIntervalConstraint> wholePair : temporalRelations) {
			Pair<Act, Act> actPair = wholePair.getFirst();
			AllenIntervalConstraint con = wholePair.getSecond();
			String actFrom = (String) actPair.getFirst().getAct();
			String actTo = (String) actPair.getSecond().getAct();

			Boolean unaryCon = false;
			if (actFrom == actTo) {
				unaryCon = true;
			}
			// Find two requirements (from and to)
			for (String requirementFrom : indicesRequrementMap.keySet()) {
				// Take all behaviors from act
				if (checkIfGroundedActIsInRequirement(requirementFrom, actFrom, theGrounding)) {
					for (String requirementTo : indicesRequrementMap.keySet()) {
						if (checkIfGroundedActIsInRequirement(requirementTo, actTo, theGrounding)) {
							// Add constraint, BUT with some RULES:

							// 1. NO CONSTRAINT, if DIFFERENT AGENTS, SAME ROLES
							String agentNameFrom = getRobotNameFromRequirement(requirementFrom);
							String agentNameTo = getRobotNameFromRequirement(requirementTo);
							if (!agentNameFrom.equals(agentNameTo)) {
								// Get the roles, to see if they are the same...
								Boolean sameRoles = areTheSameRoles(agentNameFrom, agentNameTo,
										theGrounding);
								if (sameRoles)
									continue;
							}

							// 2. NO CONSTRAINTS, if UNARY and different
							// requirement names
							if (unaryCon && !requirementFrom.equals(requirementTo)) {
								continue;
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

	private Boolean areTheSameRoles(String agent1, String agent2, Grounding grounding) {

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

					if (pairAgent2.equals(agent2)) {
						// Second found, Check if they have the same roles?
						if (pairRole1.equals(pairRole2)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private Boolean checkIfGroundedActIsInRequirement(String requirement, String act,
			Grounding grounding) {

		// Parse behavior name
		String beh;
		Integer colIndex = requirement.lastIndexOf("::");
		if (requirement.contains("(")) {
			Integer brackIndex = requirement.lastIndexOf("(");
			beh = requirement.substring(colIndex + 2, brackIndex);
		} else {
			beh = requirement.substring(colIndex + 2);
		}

		// Check if the requirement contains grounded act (as beh).
		List<Pair<Act, Behavior>> relationsGb = grounding.Gb.getRelation();
		for (Pair<Act, Behavior> gbPair : relationsGb) {
			String pairAct = (String) gbPair.getFirst().getAct();
			String pairBeh = (String) gbPair.getSecond().getBeh();
			if (act.equals(pairAct) && beh.equals(pairBeh))
				return true;
		}

		return false;
	}

	private String getRobotNameFromRequirement(String requirement) {

		Integer colIndex = requirement.lastIndexOf("::");
		String agent = requirement.substring(0, colIndex);

		return agent;
	}

	private String getBehNameFromRequirement(String requirement) {
		Integer colIndex = requirement.lastIndexOf("::");
		String beh = requirement.substring(colIndex + 2);

		return beh;
	}

	private Boolean isReqAct(String requirement, String act) {
		// Gives the index before ::, so increase it with 2
		Integer colIndex = requirement.lastIndexOf("::");
		String reqAct = requirement.substring(colIndex + 2);

		if (reqAct.equals(act))
			return true;

		return false;
	}

	private SimpleDomain createCustomDomain(MetaConstraintSolver sp, Institution inst,
			InstDomain theDomain, Grounding theGrounding) {

		// TODO: SKIPING ADMISIBILITY CHECK AT THE MOMENT! GOALS AS ACTS PROBLEM
		// // Check if the grounding is admissible
		// if (theGrounding.isAdmissibleGrounding()) {
		// System.out.println("\nThe grounding is Admissible!!!");
		// } else {
		// System.out.println("\nThe grounding is NOT Admissible!!! Stopping...");
		// throw new Error("The institution is not admissible");
		// }

		// Get all actuators (used to create variables, and add resources)
		List<String> actuatorList = getAllActuators(theGrounding);

		// Create an empty domain (with resource requirements)
		createEmptyDomain(actuatorList);

		// Create sensors, actuators, context variables
		createVariables(theGrounding, actuatorList);

		// Create resource mobility operators
		createResourceMobilityOperators(actuatorList);

		// Operator that defines goals, and expand them
		// from institution level to inst. domain level
		List<String> goals = createGoals(inst, theGrounding);

		// FOR EACH GOAL CREATE AN OPERATOR (RECEPIE)
		// HERE THE ABSTRACT RECEPIE IS: EscapeMove to Letter ..
		// BUT the grounding will lead to
		// escapeMove -> letter_A, escapeMove -> letter_B, escapeMove ->letter_C
		// Issue: Can not distinguish between 'visit all grounded letters', and
		// 'visit only one letter'
		List<String> required_behaviors = createGoalOperators(goals, inst, theGrounding);

		// ADD EACH BEHAVIOR FROM THE PLAN- recipe, WITH POSSIBLE NORMATIVE
		// CONSTRAINTS
		createBehaviorOperators(required_behaviors, inst, theGrounding);

		// TODO: Add until all planning norms are not null
		// createAllOthersConstraints();

		// ... and we also add all its resources as separate meta-constraints
		for (Schedulable sch : dom.getSchedulingMetaConstraints())
			sp.addMetaConstraint(sch);
		sp.addMetaConstraint(dom);
		return dom;
	}

	private void createBehaviorOperators(List<String> required_behaviors, Institution inst,
			Grounding theGrounding) {
		// For each behavior create an operator
		for (String requirement : required_behaviors) {

			// Head is the requirement
			String head = requirement;

			// Requirements
			Map<String, Integer> indicesRequrementMap = new HashMap<String, Integer>();
			Map<Integer, AllenIntervalConstraint> headToCon = new HashMap<Integer, AllenIntervalConstraint>();

			// Resource requirements (internal mechanics)
			String currentAgent = getRobotNameFromRequirement(requirement);
			String res_actuator = currentAgent + "_mobility::used";
			indicesRequrementMap.put(res_actuator, 0);
			Integer reqIndex = 1;

			// Add PLN norms related to the current requirement in
			// indicesRequrementMap
			// TODO: PROBLEM: only one level deep (solution: while
			createPLN(head, indicesRequrementMap, headToCon, reqIndex, inst, theGrounding);

			// CREATE CONSTRAINTS (HEAD -> REQUIREMENTS) (internal mechanics)
			String[] requirementStrings = indicesRequrementMap.keySet().toArray(new String[0]);
			AllenIntervalConstraint[] consFromHeadtoReq = new AllenIntervalConstraint[requirementStrings.length];
			AllenIntervalConstraint con = new AllenIntervalConstraint(
					AllenIntervalConstraint.Type.During);
			consFromHeadtoReq[indicesRequrementMap.get(res_actuator)] = con;

			// OTER HEAD->REQUIREMENTS CONSTREAINTS
			for (Entry<Integer, AllenIntervalConstraint> entry : headToCon.entrySet()) {
				Integer requirementIndex = entry.getKey();
				AllenIntervalConstraint requirementConstraint = entry.getValue();
				consFromHeadtoReq[requirementIndex] = requirementConstraint;
			}

			// CREATE REQUIREMENT -> REQUIREMENT (or HEAD) CONSTRAINTS
			Vector<AdditionalConstraint> additionalConstraints = createConstraints(
					indicesRequrementMap, inst, theGrounding);

			createOperatorHelper(head, indicesRequrementMap, consFromHeadtoReq,
					additionalConstraints, null);
		}
	}

	private void createPLN(String head, Map<String, Integer> indicesRequrementMap,
			Map<Integer, AllenIntervalConstraint> headToCon, Integer reqIndex, Institution inst,
			Grounding theGrounding) {
		RelationWithType<Act, Act, AllenIntervalConstraint> temporalPLN = (RelationWithType<Act, Act, AllenIntervalConstraint>) inst
				.getPLN().get("Temporal");
		List<Pair<Pair<Act, Act>, AllenIntervalConstraint>> temporalRelations = temporalPLN
				.getRelation();
		for (Pair<Pair<Act, Act>, AllenIntervalConstraint> wholePair : temporalRelations) {
			Pair<Act, Act> actPair = wholePair.getFirst();
			String actFrom = (String) actPair.getFirst().getAct();
			String actTo = (String) actPair.getSecond().getAct();
			AllenIntervalConstraint con = wholePair.getSecond();
			if (!actFrom.equals(actTo)) {
				Map<String, Integer> requirementsFrom = requirementsFromAct(actFrom, 0, inst,
						theGrounding);
				Map<String, Integer> requirementsTo = requirementsFromAct(actTo, 0, inst,
						theGrounding);

				if (requirementsFrom.containsKey(head)) {
					for (String reqTo : requirementsTo.keySet()) { 
						// TODO:rules! same robot, same roles?
						indicesRequrementMap.put(reqTo, reqIndex);
						AllenIntervalConstraint newConstraint = con;
						headToCon.put(reqIndex, newConstraint);
						reqIndex++;
					}
				} else if (requirementsTo.containsKey(head)) {
					for (String reqFrom : requirementsFrom.keySet()) {
						if (!reqFrom.equals(head)) {
							indicesRequrementMap.put(reqFrom, reqIndex);
							reqIndex++;
						}
					}
				}
			}
		}
	}

	private void createResourceMobilityOperators(List<String> actuatorList) {
		for (int i = 0; i < actuatorList.size(); i++) {
			String head = actuatorList.get(i) + "_mobility::used";

			// Resource usages
			int[] resourceUsage = new int[actuatorList.size()];
			resourceUsage[i] = 1;

			createOperatorHelper(head, null, null, null, resourceUsage);
		}
	}

	private List<String> getAllActuators(Grounding theGrounding) {
		// Get All actuators
		ArrayList<Agent> addedAgents = new ArrayList<Agent>();
		List<String> actuatorList = new ArrayList<String>();
		List<Pair<Role, Agent>> relationsGa = theGrounding.Ga.getRelation();
		for (Pair<Role, Agent> gaPair : relationsGa) {
			Role role = gaPair.getFirst();
			Agent ag = gaPair.getSecond();

			if (!addedAgents.contains(ag)) {
				actuatorList.add((String) ag.getAg());
				addedAgents.add(ag);
			}
		}

		return actuatorList;
	}

	private void createOperatorHelper(String head, Map<String, Integer> indicesRequrementMap,
			AllenIntervalConstraint[] consFromHeadtoReq,
			Vector<AdditionalConstraint> additionalConstr, int[] resourceUsages) {

		// Sort by value (indexes)
		if (indicesRequrementMap != null) {
			indicesRequrementMap = MapUtil.sortByValue(indicesRequrementMap);
			String[] requirementStrings = indicesRequrementMap.keySet().toArray(new String[0]);
			if (consFromHeadtoReq == null) {
				consFromHeadtoReq = new AllenIntervalConstraint[requirementStrings.length];
			}

			if (resourceUsages == null)
				resourceUsages = new int[2]; // TODO: get length from actuators

			SimpleOperator instOp = new SimpleOperator(head, consFromHeadtoReq, requirementStrings,
					resourceUsages);
			if (additionalConstr != null) {
				for (AdditionalConstraint ac : additionalConstr) {
					ac.addAdditionalConstraint(instOp);
				}
			}

			dom.addOperator(instOp);
		} else {
			if (resourceUsages == null)
				throw new Error("Resource usages must be set at this point");
			consFromHeadtoReq = new AllenIntervalConstraint[0];
			String[] requirementStrings = new String[0];
			SimpleOperator instOp = new SimpleOperator(head, consFromHeadtoReq, requirementStrings,
					resourceUsages);
			dom.addOperator(instOp);
		}
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
