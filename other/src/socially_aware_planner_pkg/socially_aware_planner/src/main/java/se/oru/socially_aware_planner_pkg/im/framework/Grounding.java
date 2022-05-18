package se.oru.socially_aware_planner_pkg.im.framework;

import java.util.Collection;
import java.util.List;

import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Agent;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Behavior;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain.Obj;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Act;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Art;
import se.oru.socially_aware_planner_pkg.im.framework.Institution.Role;
import se.oru.socially_aware_planner_pkg.im.institutions.TheGameInstitution;
import se.oru.socially_aware_planner_pkg.im.institutions.ThePEIS2Domain;
import se.oru.socially_aware_planner_pkg.im.utils.Relation;

@SuppressWarnings("rawtypes")
public class Grounding {

	public Relation<Role, Agent> Ga;
	public Relation<Act, Behavior> Gb;
	public Relation<Art, Obj> Go;

	private Institution institution = null;
	private InstDomain instDomain = null;

	private Grounding() {
	}

	public Grounding(Institution institution, InstDomain instDomain) {
		super();
		this.institution = institution;
		this.instDomain = instDomain;

		Ga = new Relation<Role, Agent>();
		Gb = new Relation<Act, Behavior>();
		Go = new Relation<Art, Obj>();
	}

	// Get the existing role and agent from the institution by the name and add
	protected void addRelationGa(String roleToGet, String agentToGet) {
		Role role = (Role) institution.getRoleSet().get(roleToGet);
		Agent ag = (Agent) instDomain.getAgentSet().get(agentToGet);

		if (role == null) {
			throw new Error("There is no Role with the name: " + roleToGet);
		} else if (ag == null) {
			throw new Error("There is no Agent with the name: " + agentToGet);
		}

		Ga.addRelation(role, ag);
	}

	protected void addRelationGb(String actToGet, String behToGet) {
		Act act = (Act) this.institution.getActsSet().get(actToGet);
		Behavior beh = (Behavior) this.instDomain.getBehaviorSet().get(behToGet);

		if (act == null) {
			throw new Error("There is no Role with the name: " + actToGet);
		} else if (beh == null) {
			throw new Error("There is no Agent with the name: " + behToGet);
		}

		Gb.addRelation(act, beh);
	}

	protected void addRelationGo(String artToGet, String objToGet) {
		Art art = (Art) this.institution.getArtsSet().get(artToGet);
		Obj obj = (Obj) this.instDomain.getObjectSet().get(objToGet);

		if (art == null) {
			throw new Error("There is no Role with the name" + artToGet);
		} else if (obj == null) {
			throw new Error("There is no Agent with the name" + objToGet);
		}

		Go.addRelation(art, obj);
	}

	// PROBLEM: If there is no grounding at all, Affords condition returns TRUE
	private boolean AffordsArt0(Act act, Art art) {
		if (institution.getUSN_ARTS().containsPair(act, art)) {
			Collection<Behavior> behCollection = instDomain.getBehaviorSet().values();
			for (Behavior beh : behCollection) {
				// For each object in D.Objects
				Collection<Obj> objCollection = instDomain.getObjectSet().values();
				for (Obj obj : objCollection) {
					if (Gb.containsPair(act, beh))
						if (Go.containsPair(art, obj))
							if (instDomain.Affords_obj.containsPair(beh, obj)) {
								;
							} else {
								System.out.println("AffordsArt failed: \nAct: " + act.getAct().toString() + "\nArt: "
										+ art.getArt().toString() + "\nBeh: " + beh.getBeh().toString() + "\nObj: "
										+ obj.getObj().toString());
								return false;
							}
				}
			}
		}

		return true;
	}

	// PROBLEM: Grounding must exists for all objects and all behaviors
	private boolean AffordsArt1(Act act, Art art) {
		if (institution.getUSN_ARTS().containsPair(act, art)) {
			Collection<Behavior> behCollection = instDomain.getBehaviorSet().values();
			for (Behavior beh : behCollection) {
				// For each object in D.Objects
				Collection<Obj> objCollection = instDomain.getObjectSet().values();
				for (Obj obj : objCollection) {
					if (Gb.containsPair(act, beh) && Go.containsPair(art, obj) && instDomain.Affords_obj.containsPair(beh, obj)) {
						;
					} else {
						System.out.println("AffordsArt failed: \nAct: " + act.getAct().toString() + "\nArt: " + art.getArt().toString()
								+ "\nBeh: " + beh.getBeh().toString() + "\nObj: " + obj.getObj().toString());
						return false;
					}
				}
			}
		}

		return true;
	}

	// PROBLEM: An artifact is grounded as e.g. 5 objects.
	// We want that all 5 objects to afford grounded behavior/s.
	// In below alg., we need to have only one objects in Go and Affords_obj.
	private boolean AffordsArt2(Act act, Art art) {
		if (institution.getUSN_ARTS().containsPair(act, art)) {
			Collection<Behavior> behCollection = instDomain.getBehaviorSet().values();
			for (Behavior beh : behCollection) {
				Collection<Obj> objCollection = instDomain.getObjectSet().values();
				for (Obj obj : objCollection) {
					if (Gb.containsPair(act, beh) && Go.containsPair(art, obj) && instDomain.Affords_obj.containsPair(beh, obj)) {
						return true;
					}
				}
			}
		}

		System.out.println("\nAffords Artifacs failed. Could not find corresponding affordances");
		return false;
	}

	// CORRECT:
	private boolean AffordsArt3(Act act, Art art) {
		Boolean existsGb = false;
		Boolean existsGo = false;

		if (institution.getUSN_ARTS().containsPair(act, art)) {
			Collection<Behavior> behCollection = instDomain.getBehaviorSet().values();
			for (Behavior beh : behCollection) {
				Collection<Obj> objCollection = instDomain.getObjectSet().values();
				for (Obj obj : objCollection) {
					if (Gb.containsPair(act, beh)) {
						existsGb = true;
						if (Go.containsPair(art, obj)) {
							existsGo = true;
							if (!instDomain.Affords_obj.containsPair(beh, obj)) {
								System.out.println("AffordsArt failed: \nAct: " + act.getAct().toString() + "\nArt: "
										+ art.getArt().toString() + "\nBeh: " + beh.getBeh().toString() + "\nObj: "
										+ obj.getObj().toString());
								return false;
							}
						}
					}
				}
			}
		}

		if (!existsGb || !existsGo) {
			System.out.println("AffordsArt failed: \nexistsGb = " + existsGb.toString() + "\nexistsGo = " + existsGo.toString());
			return false;
		}

		return true;
	}

	private boolean AffordsRole(Act act, Role urole) {
		Boolean existsGb = false;
		Boolean existsGa = false;

		if (institution.getUSN_ROLES().containsPair(act, urole)) {
			Collection<Behavior> behCollection = instDomain.getBehaviorSet().values();
			for (Behavior beh : behCollection) {
				Collection<Agent> agentCollection = instDomain.getAgentSet().values();
				for (Agent uagent : agentCollection) {
					if (Gb.containsPair(act, beh)) {
						existsGb = true;
						if (Ga.containsPair(urole, uagent)) {
							existsGa = true;
							if (!instDomain.Affords_ag.containsPair(beh, uagent)) {
								System.out.println("AffordsRole failed: \nAct: " + act.getAct().toString() + "\nURole: "
										+ urole.getRole().toString() + "\nBeh: " + beh.getBeh().toString() + "\nUAgent: "
										+ uagent.getAg().toString());
								return false;
							}
						}
					}
				}
			}
		}

		if (!existsGb || !existsGa) {
			System.out.println("AffordsArt failed: \nexistsGb = " + existsGb.toString() + "\nexistsGa = " + existsGa.toString());
			return false;
		}

		return true;
	}

	private boolean checkCardinality(Role role) {
		Integer roleCard = Ga.countKeyCardinality(role);
		Integer minCard = institution.getRoleMinCard(role);
		Integer maxCard = institution.getRoleMaxCard(role);

		if (roleCard < minCard || roleCard > maxCard) {
			System.out.println("Cardinality fails! \nRole: " + role.getRole().toString() + "\nRole Card: " + roleCard.toString()
					+ "\nMin Card: " + minCard.toString() + "\nMax Card: " + maxCard.toString());
			return false;
		}

		return true;
	}
	public boolean isAdmissibleGrounding() {

		// For each role in Roles
		Collection<Role> rolesCollection = institution.getRoleSet().values();
		for (Role role : rolesCollection) {

			// Cardinality Condition
			if (!checkCardinality(role))
				return false;

			// For each agent in Agents
			Collection<Agent> agentsCollection = instDomain.getAgentSet().values();
			for (Agent agent : agentsCollection) {
				if (Ga.containsPair(role, agent)) {
					// Well-Formed Condition
					if (!isWellFormed(role, agent)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private boolean isCapable(Agent agent, Role role, Act act) {
		// If (institution.getOBN().containsPair(role, act))
		boolean bExists = false;
		Collection<Behavior> behCollection = instDomain.getBehaviorSet().values();
		for (Behavior beh : behCollection) {
			if (Gb.containsPair(act, beh)) {
				if (instDomain.Capabilities.containsPair(agent, beh)) {
					bExists = true;
					break;
				}
			}
		}

		if (!bExists) {
			System.out.println("isCapable failed: \nAgent: " + agent.getAg().toString() + " \nRole: " + role.getRole().toString()
					+ "\nAct:" + act.getAct().toString());
		}

		return bExists;
	}
	
	@SuppressWarnings("unchecked")
	private boolean isWellFormed(Role role, Agent agent) {
		// For each act in I.Acts
		Collection<Act> actCollection = institution.getActsSet().values();
		for (Act act : actCollection) {
			if (institution.getOBN().containsPair(role, act)) {

				// CAPABLE CONDITION
				if (!isCapable(agent, role, act))
					return false;

				// For each art in I.Arts
				Collection<Art> artCollection = institution.getArtsSet().values();
				for (Art art : artCollection) {
					if (institution.getUSN_ARTS().containsPair(act, art)) {
						// AFORDS Artifacts Condition
						if (!AffordsArt3(act, art)) {
							return false;
						}
					}
				}

				// For each urole in I.Roles
				Collection<Role> roleCollection = institution.getRoleSet().values();
				for (Role urole : roleCollection) {
					if (institution.getUSN_ROLES().containsPair(act, urole)) {
						// AFORDS uRoles Condition
						if (!AffordsRole(act, urole)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}
	
	public List<Agent> getAgentsFromGa(Role rol){
		return Ga.getAllSeconds(rol);
	}
	
	public List<Role> getRolesFromGa(Agent ag){
		return Ga.getAllFirsts(ag);
	}
	
	public List<Behavior> getBehFromGb(Act act){
		return Gb.getAllSeconds(act);
	}
	
	public List<Act> getActFromGb(Behavior beh){
		return Gb.getAllFirsts(beh);
	}
	
	public List<Obj> getObjFromGo(Art art){
		return Go.getAllSeconds(art);
	}
	
	public List<Art> getArtFromGo(Obj obj){
		return Go.getAllFirsts(obj);
	}
	
}