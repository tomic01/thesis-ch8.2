package se.oru.socially_aware_planner_pkg.im.framework;

import java.util.HashMap;
import java.util.Map;

import se.oru.socially_aware_planner_pkg.im.utils.Relation;

// Domain holds set of agents, their behaviors and objects in the environment
// It also holds relations between those sets, capabilities and affordances 
// Each set can have elements with arbitrary types
public class InstDomain<T_A, T_B, T_O> {

	public class Agent {
		private T_A ag;

		public Agent(T_A agent) {
			this.ag = agent;
		}

		public T_A getAg() {
			return ag;
		}
	}
	public class Behavior {
		private T_B beh;

		public Behavior(T_B beh) {
			this.beh = beh;
		}

		public T_B getBeh() {
			return beh;
		}
	}

	public class Obj {
		private T_O obj;

		public Obj(T_O obj) {
			this.obj = obj;
		}

		public T_O getObj() {
			return obj;
		}
	}

	// SETS -- implemented as MAP for convenience
	protected Map<String, Agent> AgentSet;
	protected Map<String, Behavior> BehaviorSet;
	protected Map<String, Obj> ObjectSet;

	// RELATIONS
	protected Relation<Agent, Behavior> Capabilities;
	protected Relation<Behavior, Obj> Affords_obj;
	protected Relation<Behavior, Agent> Affords_ag;

	public InstDomain() {
		super();

		// TODO: use diamond operator (java 7)
		this.AgentSet = new HashMap<String, Agent>();
		this.BehaviorSet = new HashMap<String, Behavior>();
		this.ObjectSet = new HashMap<String, Obj>();
		// RELATIONS
		this.Capabilities = new Relation<Agent, Behavior>();
		this.Affords_obj = new Relation<Behavior, Obj>();
		this.Affords_ag = new Relation<Behavior, Agent>();
	}
	public Map<String, Agent> getAgentSet() {
		return AgentSet;
	}
	public Map<String, Behavior> getBehaviorSet() {
		return BehaviorSet;
	}
	public Map<String, Obj> getObjectSet() {
		return ObjectSet;
	}

}
