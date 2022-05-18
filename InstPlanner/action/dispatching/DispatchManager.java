package se.oru.socially_aware_planner_pkg.im.action.dispatching;

//import monarch_msgs.KeyValuePairArray;
import inst_msgs.BehResult;
import inst_srvs.position_info_srv;
import inst_srvs.position_info_srvResponse;

import java.util.Map.Entry;

import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.ros.exception.RemoteException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Subscriber;

import se.oru.socially_aware_planner_pkg.im.action.InstitutionNode;

// import org.ros.node.NodeConfiguration;

public class DispatchManager extends DispatchAbstract {

	// On this topic the planner will listen if behavior is finished
	private static final String BehaviorResultTopicName = new String("behaviorToFinish");

	// Avoid active waiting
	// private static final Object ResultLock = new Object();
	// private static Boolean ResultRecieved = false;
	private static final Object ServiceLock = new Object();
	private static Boolean ServiceResponseReceived = false;

	// Behavior Names TODO: Automatically, take from DOMAIN
	private static String SayWordBehaviorName;
	private static String EscapeMoveBehaviorName;
	private static String CatchBehaviorName;
	private static final String RestartActName1 = "__Restart1__";
	private static final String RestartActName2 = "__Restart2__";

	// Planner components
	private final ActivityNetworkSolver activitySolver;
	private final ConnectedNode connectedNode;
	private final String[] actuators;

	// Simulation or testing
	Boolean testingMode = false;

	public DispatchManager(final ActivityNetworkSolver actSolver,
			final ConnectedNode connectedNode, final String[] actuators, Boolean testingMode) {
		this.activitySolver = actSolver;
		this.connectedNode = connectedNode;
		this.actuators = actuators;
		this.testingMode = testingMode;
		// TODO: Take from the grounding
		SayWordBehaviorName = "sayWord";
		EscapeMoveBehaviorName = "moveTo";
		CatchBehaviorName = "gradientMoveTo";
	}

	public void dispatcherSetup() {
		// Get through all actuators (robots)
		for (final String actuator : this.actuators) {
			dispatcherSetup(actuator);
		}

		if (testingMode) {
			monitorResultTest();
		} else {
			monitorBehResults();
		}

	}

	protected void monitorBehResults() {
		Subscriber<inst_msgs.BehResult> subscriberBehaviorResult = connectedNode.newSubscriber(
				BehaviorResultTopicName, inst_msgs.BehResult._TYPE);
		subscriberBehaviorResult.addMessageListener(new MessageListener<BehResult>() {
			@Override
			public void onNewMessage(BehResult behResult) {
				InstitutionNode.logger.info("Result Feedback received: \n");
				InstitutionNode.logger.info("Agent: " + behResult.getAgentName() + "\n");
				InstitutionNode.logger.info("Behav: " + behResult.getBehName() + "\n");
				InstitutionNode.logger.info("Succe: " + behResult.getSuccess() + "\n");

				String uniqueName = behResult.getAgentName() + behResult.getBehName();

				InstitutionNode.logger.info("Stoping unique name: " + uniqueName);
				DispatchUtils.stopExecutingActivity(uniqueName);
			}
		});

	}

	// Automatically finishes behaviors after some delay
	@SuppressWarnings("unused")
	private void monitorResultTest() {
		while (true) {
			// Sleep for X seconds
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Now get through each behavior, find one and stop it
			if (!DispatchAbstract.ID_Activity_Map.isEmpty()) {
				for (Entry<String, SymbolicVariableActivity> entry : DispatchAbstract.ID_Activity_Map
						.entrySet()) {
					String behaviorID = entry.getKey();
					SymbolicVariableActivity act = entry.getValue();
					DispatchUtils.stopExecutingActivity(behaviorID);
					break;
				}
			}
		}
	}

	private void dispatcherSetup(final String agentName) {
		// Dispatching function. Called when the behavior is dispatched.
		final DispatchingFunction dispatchBehavior = new DispatchingFunction(agentName) {
			@Override
			public void dispatch(SymbolicVariableActivity activity) {

				// Get actuator symbol from dispatched activity
				String agentName = activity.getComponent();
				String behaviorName = null;
				String[] params = null;
				String fulleBehaviorName = activity.getSymbolicVariable().getSymbols()[0];
				Integer brackIndex = fulleBehaviorName.lastIndexOf("(");
				if (brackIndex != -1) { // some value returned
					behaviorName = fulleBehaviorName.substring(0, brackIndex);
					String allParams = fulleBehaviorName.substring(brackIndex + 1,
							fulleBehaviorName.length() - 1);
					params = allParams.split(",");

				} else {
					behaviorName = fulleBehaviorName;
				}

				String uniqueName = agentName + behaviorName;
				InstitutionNode.logger.info("\n**********Dispatching********** \nAgent: "
						+ agentName + ";\nBehavior: " + behaviorName + "\nParams: ");
				if (params != null) {
					for (String param : params) {
						InstitutionNode.logger.info(param + ", ");
					}
				}

				if (!(behaviorName.equals(RestartActName1) || behaviorName.equals(RestartActName2))) {
					ID_Activity_Map.put(uniqueName, activity);
				}

				// SETUP DISPATCHING FUNCTINOS
				if (behaviorName.equals(SayWordBehaviorName)) {
					try {
						// Position is calculated inside the behavior
						if (!testingMode) 
							callSayWordService(agentName, behaviorName, 0, 0);
					} catch (ServiceNotFoundException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else if (behaviorName.equals(EscapeMoveBehaviorName)) {
					try {
						if (!testingMode)
							callEscapeMoveService(agentName, behaviorName, params[0]);
					} catch (ServiceNotFoundException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else if (behaviorName.equals(CatchBehaviorName)) {
					try {
						if (!testingMode)
							callCatchService(agentName, behaviorName);
					} catch (ServiceNotFoundException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else if (behaviorName.equals(RestartActName1)) {
					// RESTART THE CONTEXT and STOP THIS BEHAVIOR OR
					// SET 'UNPLANED' corresponding variables...
					InstitutionNode.sensorCommand.postSensorValue("TheGame",
							InstitutionNode.animator.getTimeNow());
					DispatchUtils.stopActivity(activity);

				} else if (behaviorName.equals(RestartActName2)) {
					// RESTART THE CONTEXT and STOP THIS BEHAVIOR
					InstitutionNode.sensorCommand.postSensorValue("None",
							InstitutionNode.animator.getTimeNow());
					DispatchUtils.stopActivity(activity);
				}
			}

			@Override
			public boolean skip(SymbolicVariableActivity act) {
				return false;
			}
		};

		InstitutionNode.animator.addDispatchingFunctions(activitySolver, dispatchBehavior);
	}

	private void callSayWordService(String agentName, String behName, float x, float y)
			throws ServiceNotFoundException, InterruptedException {
		String serviceName = agentName + "/" + behName;
		InstitutionNode.logger.info("Service name: " + serviceName);
		ServiceClient<inst_srvs.position_info_srvRequest, inst_srvs.position_info_srvResponse> behRequest = connectedNode
				.newServiceClient(serviceName, position_info_srv._TYPE);
		inst_srvs.position_info_srvRequest request = behRequest.newMessage();

		request.setPosX(x);
		request.setPosY(y);

		behRequest.call(request, new ServiceResponseListener<position_info_srvResponse>() {
			@Override
			public void onSuccess(position_info_srvResponse arg0) {
				synchronized (ServiceLock) {
					ServiceResponseReceived = true;
					ServiceLock.notifyAll();
				}

			}

			@Override
			public void onFailure(RemoteException arg0) {
				throw new Error(
						"Call to SayWord Service resultet in failure! Behavior is not dispatched!!!");
			}
		});

		if (!ServiceResponseReceived) {
			synchronized (ServiceLock) {
				ServiceLock.wait();
			}
		}

		ServiceResponseReceived = false;
	}

	private void callEscapeMoveService(String agentName, String behName, String param)
			throws ServiceNotFoundException, InterruptedException {

		String serviceName = agentName + "/" + behName;
		ServiceClient<inst_srvs.position_info_str_srvRequest, inst_srvs.position_info_str_srvResponse> behRequest = connectedNode
				.newServiceClient(serviceName, inst_srvs.position_info_str_srv._TYPE);
		inst_srvs.position_info_str_srvRequest request = behRequest.newMessage();
		request.setPosition(param);

		behRequest.call(request,
				new ServiceResponseListener<inst_srvs.position_info_str_srvResponse>() {
					@Override
					public void onSuccess(inst_srvs.position_info_str_srvResponse arg0) {
						synchronized (ServiceLock) {
							ServiceResponseReceived = true;
							ServiceLock.notifyAll();
						}

					}

					@Override
					public void onFailure(RemoteException arg0) {
						throw new Error(
								"Call to SayWord Service resultet in failure! Behavior is not dispatched!!!");
					}
				});

		if (!ServiceResponseReceived) {
			synchronized (ServiceLock) {
				ServiceLock.wait();
			}
		}

		ServiceResponseReceived = false;
	}

	private void callCatchService(String agentName, String behName)
			throws ServiceNotFoundException, InterruptedException {

		String serviceName = agentName + "/" + behName;
		ServiceClient<std_srvs.EmptyRequest, std_srvs.EmptyResponse> behRequest = connectedNode
				.newServiceClient(serviceName, std_srvs.Empty._TYPE);
		std_srvs.EmptyRequest request = behRequest.newMessage();

		behRequest.call(request, new ServiceResponseListener<std_srvs.EmptyResponse>() {
			@Override
			public void onSuccess(std_srvs.EmptyResponse arg0) {
				synchronized (ServiceLock) {
					ServiceResponseReceived = true;
					ServiceLock.notifyAll();
				}

			}

			@Override
			public void onFailure(RemoteException arg0) {
				throw new Error(
						"Call to SayWord Service resultet in failure! Behavior is not dispatched!!!");
			}
		});

		if (!ServiceResponseReceived) {
			synchronized (ServiceLock) {
				ServiceLock.wait();
			}
		}

		ServiceResponseReceived = false;
	}

}
