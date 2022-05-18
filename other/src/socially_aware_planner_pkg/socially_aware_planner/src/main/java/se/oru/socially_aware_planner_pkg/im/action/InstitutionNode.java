/*
 * Copyright (C) 2016 S.Tomic
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package se.oru.socially_aware_planner_pkg.im.action;

import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.meta.simplePlanner.SimplePlannerInferenceCallback;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.sensing.ConstraintNetworkAnimator;
import org.metacsp.sensing.Sensor;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.topic.Publisher;

import se.oru.socially_aware_planner_pkg.im.action.dispatching.DispatchManager;
import se.oru.socially_aware_planner_pkg.im.framework.Grounding;
import se.oru.socially_aware_planner_pkg.im.framework.InstDomain;
import se.oru.socially_aware_planner_pkg.im.framework.Institution;
import se.oru.socially_aware_planner_pkg.im.institutions.TheGameInstitution;
import se.oru.socially_aware_planner_pkg.im.institutions.ThePEIS2Domain;
import se.oru.socially_aware_planner_pkg.im.institutions.groundings.TheGameGrounding01;

/**
 * A simple {@link Publisher} {@link NodeMain}.
 */
public class InstitutionNode extends AbstractNodeMain {

	// Log
	public static Logger logger;

	public static ConstraintNetworkAnimator animator;
	public static DispatchManager dispatchMngr;
	public static Sensor sensorCommand;
	public static Sensor sensorRestart;

	public static void displayTimelineAnimator(
			ActivityNetworkSolver activitySolver, SimpleDomain domain) {

		LinkedList<String> sensorsActuatorsList = new LinkedList<String>();

		// Time
		sensorsActuatorsList.add("Time");

		// Sensors
		Collections.addAll(sensorsActuatorsList, domain.getSensors());

		// Context Variables
		Collections.addAll(sensorsActuatorsList, domain.getContextVars());

		// Actuators
		Collections.addAll(sensorsActuatorsList, domain.getActuators());

		String[] sensorsActuatorsArray = sensorsActuatorsList
				.toArray(new String[sensorsActuatorsList.size()]);
		TimelinePublisher tp = new TimelinePublisher(activitySolver,
				new Bounds(0, 60000), true, sensorsActuatorsArray);
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		tv.startAutomaticUpdate(1000);

	}

	// Private eRobotActions robotActions;
	private String prefix = "/planning";

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of(prefix + "/" + this.getClass().getSimpleName());
	}

	public void inputSensorTraces(String sapPackagePath) {
		// String relativePath = sapPackagePath + "/sensorTraces/experiments/";
		sensorCommand = new Sensor("command", InstitutionNode.animator);
		// sensorCommand.registerSensorTrace(relativePath + "command.st");
		sensorCommand.postSensorValue("None", animator.getTimeNow());
	}

	@Override
	public void onStart(final ConnectedNode connectedNode) {

		// Use ROS parameters to get the name of the planning domain
		ParameterTree params = connectedNode.getParameterTree();
		String domainFilePath = params.getString("sap_params/domain");
		Boolean testingMode = params.getBoolean("sap_params/TestingMode");
		String sapPackagePath = params.getString("sap_params/sap_pkg_path");

		// Initialize the planner
		long origin = 0l;
		final SimplePlanner planner = new SimplePlanner(origin,
				origin + 31000000000l, 0);
		// 32054400 seconds in a year with 53 weeks
		// 40000000 seconds for the planning horizon

		// Logging
		logger = MetaCSPLogging.getLogger(connectedNode.getClass());
		MetaCSPLogging.setLevel(planner.getClass(), Level.OFF);
		MetaCSPLogging.setLevel(connectedNode.getClass(), Level.FINE);
		logger.info("Logger is set");

		// INSTITUTION: Ali's game setup
		//SimpleDomain gameInstitutionDom = theGameInstitutionSetup(domainFilePath, planner);
		
		// theGameInstitutionSetupDynamic(planner);
		// TODO SimpleDomain gameInstitutionDom = createCustomDomain();
		// DynamicDomain dynDomain = new DynamicDomain(planner);
		// DynamicDomainDomain dynDomain = new DynamicDomainDomain(planner);
		DynamicDomainDomainGoals dynDomain = new DynamicDomainDomainGoals(planner);

		// Planner's Callback
		SimplePlannerInferenceCallback cb = new SimplePlannerInferenceCallback(
				planner);
		final ActivityNetworkSolver activitySolver = (ActivityNetworkSolver) planner
				.getConstraintSolvers()[0];
		
		// Active component that animates and updates the constraint network
		animator = new ConstraintNetworkAnimator(activitySolver, 1000, cb);

		// Sensors
		// inputSensorTraces(sapPackagePath);
		sensorCommand = new Sensor("command", InstitutionNode.animator);
		sensorCommand.postSensorValue("TheGame", animator.getTimeNow());
		
		//ConstraintNetwork.draw(activitySolver.getConstraintNetwork());

//		InstitutionNode.displayTimelineAnimator(activitySolver,  gameInstitutionDom);
//		dispatchMngr = new DispatchManager(activitySolver, connectedNode, gameInstitutionDom.getActuators());
		InstitutionNode.displayTimelineAnimator(activitySolver,  dynDomain.getDomain());
		dispatchMngr = new DispatchManager(activitySolver, connectedNode, dynDomain.getDomain().getActuators(), testingMode);
		
		dispatchMngr.dispatcherSetup();
		
		
	}

	private SimpleDomain theGameInstitutionSetup(String domainFilePath,
			final SimplePlanner planner) {
		// Create the institution
		TheGameInstitution theGame = new TheGameInstitution();
		// Create the domain
		ThePEIS2Domain theDomain = new ThePEIS2Domain();
		// Create the grounding
		TheGameGrounding01 theGrounding01 = new TheGameGrounding01(theGame,
				theDomain);

//		// TODO: FIX THE GOALS
//		// Check if the grounding is admissible
//		if (theGrounding01.isAdmissibleGrounding()) {
//			System.out.println("\nThe grounding is Admissible!!!");
//		} else {
//			System.out
//					.println("\nThe grounding is NOT Admissible!!! Stopping...");
//			throw new Error("The institution is not admissible");
//		}

		// Add PLN norms (from the planner domain file)
		// NOTE: In the domain we use only Roles as actuators. However, plans
		// should depend on particular agents, that depend on the grounding
		return SimpleDomain.parseDomain(planner, domainFilePath,
				ProactivePlanningDomain.class);
		// logger.info("Domain: " + domain + ": " + domain.getDescription());
	}

	private void theGameInstitutionSetupDynamic(final SimplePlanner planner) {
		// Create the institution
		TheGameInstitution theGame = new TheGameInstitution();
		// Create the domain
		ThePEIS2Domain theDomain = new ThePEIS2Domain();
		// Create the grounding
		TheGameGrounding01 theGrounding01 = new TheGameGrounding01(theGame,
				theDomain);

		// Check if the grounding is admissible
		if (theGrounding01.isAdmissibleGrounding()) {
			System.out.println("\nThe grounding is Admissible!!!");
		} else {
			System.out
					.println("\nThe grounding is NOT Admissible!!! Stopping...");
			throw new Error("The institution is not admissible");
		}

		// Grounding is admissible, so create a planning domain
		createPlanningDomain(theGame, theDomain, theGrounding01, planner);
	}

	private void createPlanningDomain(final Institution institution,
			final InstDomain theDomain, final Grounding grounding,
			final SimplePlanner planner) {
		// This is a pointer toward the ground constraint network of the planner
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver) planner
				.getConstraintSolvers()[0];

		// INITIAL AND GOAL STATE DEFS

		// Robot::SayWord(Letter)
		SymbolicVariableActivity actuatorRunnerSayWord = (SymbolicVariableActivity) groundSolver
				.createVariable("Runner");
		actuatorRunnerSayWord.setSymbolicDomain("SayWord(Letter)");
		actuatorRunnerSayWord.setMarking(markings.UNJUSTIFIED);
		// .. let's also give it a minimum duration
		AllenIntervalConstraint durationSay = new AllenIntervalConstraint(
				AllenIntervalConstraint.Type.Duration, new Bounds(3000,
						APSPSolver.INF));
		durationSay.setFrom(actuatorRunnerSayWord);
		durationSay.setTo(actuatorRunnerSayWord);

		// Runner::EscapeMove(Letter)
		SymbolicVariableActivity actuatorRunnerEscape = (SymbolicVariableActivity) groundSolver
				.createVariable("Runner");
		actuatorRunnerEscape.setSymbolicDomain("EscapeMove(Letter)");
		actuatorRunnerEscape.setMarking(markings.UNJUSTIFIED);
		// .. let's also give it a minimum duration
		AllenIntervalConstraint durationEscape = new AllenIntervalConstraint(
				AllenIntervalConstraint.Type.Duration, new Bounds(3000,
						APSPSolver.INF));
		durationEscape.setFrom(actuatorRunnerEscape);
		durationEscape.setTo(actuatorRunnerEscape);

		// Add all of the constrains
		groundSolver.addConstraints(new Constraint[] { durationSay,
				durationEscape });
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
