package se.oru.socially_aware_planner_pkg.im.framework;

import java.util.HashMap;
import java.util.Map;

import se.oru.socially_aware_planner_pkg.im.utils.Pair;
import se.oru.socially_aware_planner_pkg.im.utils.Relation;
import se.oru.socially_aware_planner_pkg.im.utils.RelationWithType;

// Institution holds set of roles, norms, acts, artifacts and cardinality relations 
// It should be translated into SAP domain (not institution domain). 
public class Institution<T_ROLE, T_ACT, T_ART> {

	public class Act {
		private T_ACT act;

		public Act(T_ACT act) {
			this.act = act;
		}

		public T_ACT getAct() {
			return act;
		}
	}

	public class Art {
		private T_ART art;

		public Art(T_ART art) {
			this.art = art;
		}

		public T_ART getArt() {
			return art;
		}

	}

	public class Role {
		private T_ROLE role;

		public Role(T_ROLE role) {
			this.role = role;
		}

		public T_ROLE getRole() {
			return role;
		}
	}
	
	// Name
	protected String instName;
	
	// Sets
	protected Map<String, Role> RoleSet;
	protected Map<String, Act> ActsSet;
	protected Map<String, Art> ArtsSet;

	// Relations (NORMS) NOTE: should be separate structure
	protected Relation<Role, Act> OBN;
	protected Relation<Act, Art> USN_ARTS;
	protected Relation<Act, Role> USN_ROLES;
	
	// Planning norms, there can me many different types
	// E.g. Allen interval constraints. 
	protected HashMap<String, RelationWithType<Act, Act, ?>> PLN;
	
	// Cardinality as a type of norm
	protected Map<Role, Pair<Integer, Integer>> roleCardinality;

	public Institution() {
		super();

		this.RoleSet = new HashMap<String, Role>();
		this.roleCardinality = new HashMap<Role, Pair<Integer, Integer>>();
		this.ActsSet = new HashMap<String, Act>();
		this.ArtsSet = new HashMap<String, Art>();

		this.OBN = new Relation<Role, Act>();
		this.USN_ARTS = new Relation<Act, Art>();
		this.USN_ROLES = new Relation<Act, Role>();
		
		// Allans Interval PLN Norms
		// this.PLN = new RelationWithType<Act, Role, AllenIntervalConstraint>();
		this.PLN = new HashMap<String, RelationWithType<Act,Act,?>>();
		

	}
	
	public String getInstName() {
		return instName;
	}

	public void setInstName(String instName) {
		this.instName = instName;
	}

	public Map<String, Act> getActsSet() {
		return ActsSet;
	}

	public Map<String, Art> getArtsSet() {
		return ArtsSet;
	}

	public Relation<Role, Act> getOBN() {
		return OBN;
	}
	
	public HashMap<String, RelationWithType<Act, Act, ?>> getPLN() {
		return PLN;
	}
	
	public Integer getRoleMaxCard(Role role) {
		Pair<Integer, Integer> cardPair = roleCardinality.get(role);
		return cardPair.getSecond();
	}
	public Integer getRoleMinCard(Role role) {
		Pair<Integer, Integer> cardPair = roleCardinality.get(role);
		return cardPair.getFirst();
	}
	public Map<String, Role> getRoleSet() {
		return RoleSet;
	}
	public Relation<Act, Art> getUSN_ARTS() {
		return USN_ARTS;
	}
	public Relation<Act, Role> getUSN_ROLES() {
		return USN_ROLES;
	}
	public void setRoleCardinality(Role role, Integer minCardinality, Integer maxCardinality) {
		if (minCardinality <= maxCardinality) {
			Pair<Integer, Integer> cardinality = new Pair<Integer, Integer>(minCardinality, maxCardinality);
			roleCardinality.put(role, cardinality);
		} else
			throw new Error("MinCardinality is higher than the max cardinality");
	}

	// Planning norms are omitted:
	// 1. They are not used for checking admissible grounding property
	// 2. They are encoded explicitly in the planning domain

}
