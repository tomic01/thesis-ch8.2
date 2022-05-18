package se.oru.socially_aware_planner_pkg.im.institutions;

import se.oru.socially_aware_planner_pkg.im.framework.InstDomain;

public class ThePEIS2Domain extends InstDomain<String, String, String> {

	// This should be populated by some registration protocol
	// Internet of things idea
	public ThePEIS2Domain() {
		super();

		// Create Agents
		Agent aMbot14 = new Agent("mbot14");
		Agent aMbot11 = new Agent("mbot11");
		Agent turtlebot1 = new Agent("turtlebot_1");
		Agent aMaja = new Agent("aMaja");
		Agent aStevan = new Agent("aStevan");
		Agent aNao1 = new Agent("aNao1");
		this.AgentSet.put("mbot14", aMbot14);
		this.AgentSet.put("mbot11", aMbot11);
		this.AgentSet.put("aMaja", aMaja);
		this.AgentSet.put("aStevan", aStevan);
		this.AgentSet.put("aNao1", aNao1);
		this.AgentSet.put("turtlebot_1", turtlebot1);

		// Create Behaviors
		Behavior moveTo = new Behavior("moveTo");
		Behavior gradientMoveTo = new Behavior("gradientMoveTo");
		Behavior sayWord = new Behavior("sayWord");
		this.BehaviorSet.put("moveTo", moveTo);
		this.BehaviorSet.put("gradientMoveTo", gradientMoveTo);
		this.BehaviorSet.put("sayWord", sayWord);

		// Create Objects
		Obj positionA = new Obj("positionA");
		Obj positionB = new Obj("positionB");
		Obj positionC = new Obj("positionC");
		Obj oMap = new Obj("oMap");
		this.ObjectSet.put("positionA", positionA);
		this.ObjectSet.put("positionB", positionB);
		this.ObjectSet.put("positionC", positionC);
		this.ObjectSet.put("oMap", oMap);

		// Create Capabilities
		this.Capabilities.addRelation(aMbot14, moveTo);
		this.Capabilities.addRelation(aMbot14, sayWord);
		this.Capabilities.addRelation(aMbot11, moveTo);
		this.Capabilities.addRelation(aMbot11, gradientMoveTo);
		this.Capabilities.addRelation(turtlebot1, sayWord);
		this.Capabilities.addRelation(turtlebot1, moveTo);
		// this.Capabilities.addRelation(aNao1, bSayWord);
		this.Capabilities.addRelation(aMaja, moveTo);
		this.Capabilities.addRelation(aMaja, gradientMoveTo);
		this.Capabilities.addRelation(aMaja, sayWord);
		this.Capabilities.addRelation(aStevan, moveTo);
		this.Capabilities.addRelation(aStevan, gradientMoveTo);
		this.Capabilities.addRelation(aStevan, sayWord);
		
		// Create
		this.Affords_obj.addRelation(moveTo, positionA);
		this.Affords_obj.addRelation(moveTo, positionB); // remove to test
		this.Affords_obj.addRelation(moveTo, positionC);
		// this.Affords_obj.addRelation(escapeMove, oMap);
		//this.Affords_obj.addRelation(sayWord, oPosition_A);
		//this.Affords_obj.addRelation(sayWord, oPosition_B);
		//this.Affords_obj.addRelation(sayWord, oPosition_C);
	}
}
