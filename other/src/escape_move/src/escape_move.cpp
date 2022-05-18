#include <ros/ros.h>
#include <move_base_msgs/MoveBaseAction.h>
#include <actionlib/client/simple_action_client.h>
#include <inst_srvs/position_info_str_srv.h>
#include <inst_msgs/BehResult.h>
#include <stdlib.h>
#include <time.h>

const static std::string BehaviorName = "moveTo";

typedef actionlib::SimpleActionClient<move_base_msgs::MoveBaseAction> MoveBaseClient;

static const std::string ResultTopicName = "/behaviorToFinish";
static bool serviceFinished = false;

std::string positionStr = "none";

void SetGoalCoordsRandomly(move_base_msgs::MoveBaseGoal& goal);
void SetGoalCoordsByPosition(move_base_msgs::MoveBaseGoal& goal);

// TODO:
// NOTE: enum is temporally solution. There should be a
// ros param (a dictionary) for the whole grounding. In this way
// each behavior will have enough information without hard-coding of parameters
enum Letters {
	A = 0, B = 1, C = 2, D = 3
};
int enumSize = 4;

bool handleUserRequest(inst_srvs::position_info_str_srv::Request &req,
		inst_srvs::position_info_str_srv::Response &res) {

	positionStr = req.position;
	ROS_INFO_STREAM("\nPosition received:" + positionStr + "\n");
	serviceFinished = true;
	return true;
}

int main(int argc, char** argv) {
	ros::init(argc, argv, "escape_move_node");
	ros::NodeHandle n;

	ros::ServiceServer service = n.advertiseService(BehaviorName,
			handleUserRequest);
	ros::Publisher pubResult = n.advertise<inst_msgs::BehResult>(
			ResultTopicName, 1000);

	// Tell the action client that we want to spin a thread by default
	MoveBaseClient ac("move_base", true);

	// Wait for the action server to come up
	while (!ac.waitForServer(ros::Duration(5.0))) {
		ROS_INFO("Waiting for the move_base action server to come up");
		ros::spinOnce();
	}

	move_base_msgs::MoveBaseGoal goal;

	while (ros::ok()) {
		// *** HANDLE THE BEHAVIOR SERVICE ***

		while (!serviceFinished) {
			ros::spinOnce();
		}
		serviceFinished = false;

		// *** SET A GOAL AND WAIT FOR RESULTS ***

		goal.target_pose.header.frame_id = "map"; //"base_link";
		goal.target_pose.header.stamp = ros::Time::now();
		// SetGoalCoordsRandomly(goal);
		SetGoalCoordsByPosition(goal);
		ROS_INFO_STREAM("Sending goal... and waiting for the response");
		ac.sendGoal(goal);
		ac.waitForResult();

		// *** WHEN RESULTS ARE RECIEVED, NOTIFY THE PLANNER ***

		if (ac.getState() == actionlib::SimpleClientGoalState::SUCCEEDED) {
			ROS_INFO_STREAM("Destination Reached!\n");
			ROS_INFO_STREAM("Sending the result message...\n");
			inst_msgs::BehResult behaviorResult;
			behaviorResult.agent_name = ros::this_node::getNamespace().substr(
					2);
			behaviorResult.beh_name = BehaviorName;
			behaviorResult.success = true;
			pubResult.publish(behaviorResult);
			ros::spinOnce();
		} else
			ROS_INFO("The base failed to move for unknown reason!!!");
	}

	return 0;
}

void SetGoalCoordsByPosition(move_base_msgs::MoveBaseGoal& goal) {

	geometry_msgs::Point goalPoint;
	geometry_msgs::Quaternion orientationPoint;

	// NOTE: Percepts should be separate set in the inst. domain
	// Here, they are hardcoded in the behavior it self
	// Drawback: this behavior is not generic, but for specific robots
	// This is for turtlebot robots

	if (positionStr == "positionA")
	{
		goalPoint.x = -1.81;
		goalPoint.y = 3.95;
		goalPoint.z = 0.0;
		ROS_INFO_STREAM("A");
	} else if (positionStr == "positionB")
	{
		goalPoint.x = -1.07;
		goalPoint.y = 0.27;
		goalPoint.z = 0.0;
		ROS_INFO_STREAM("B");
	} else if (positionStr == "positionC"){
		goalPoint.x = -0.96;
		goalPoint.y = -2.40;
		goalPoint.z = 0.0;
		ROS_INFO_STREAM("C");
	} else if (positionStr == "positionD") {
		goalPoint.x = 0.94;
		goalPoint.y = 4.04;
		goalPoint.z = 0.0;
		ROS_INFO_STREAM("D");
	} else {
		ROS_ERROR("Unsupported letter!");
	}

	orientationPoint.x = 0.0;
	orientationPoint.y = 0.0;
	orientationPoint.z = 0.1;
	orientationPoint.w = 0.99;

	goal.target_pose.pose.position = goalPoint;
	goal.target_pose.pose.orientation = orientationPoint;

	return;
}

void SetGoalCoordsRandomly(move_base_msgs::MoveBaseGoal& goal) {

	geometry_msgs::Point goalPoint;
	geometry_msgs::Quaternion orientationPoint;

	// Find random letter
	srand(time(NULL));
	int randomLetter = rand() % enumSize;
	Letters letter = static_cast<Letters>(randomLetter);

	// NOTE: position should be a part of 'precepts' sets
	switch (letter) {
	case A:
		// MBOT Map
		//goalPoint.x = 0.90;
		//goalPoint.y = -0.77;
		//goalPoint.z = 0.0;

		// Turtlebot Map
		goalPoint.x = -1.81;
		goalPoint.y = 3.95;
		goalPoint.z = 0.0;

		ROS_INFO_STREAM("A");
		break;
	case B:
		// MBOT MAP
		/*goalPoint.x = 3.8;
		 goalPoint.y = 1.3;
		 goalPoint.z = 0.0;*/

		// TURTLEBOT MAP
		goalPoint.x = -1.07;
		goalPoint.y = 0.27;
		goalPoint.z = 0.0;

		ROS_INFO_STREAM("B");
		break;
	case C:
		// MBOT MAP
		/*goalPoint.x = 6.3;
		 goalPoint.y = 2.84;
		 goalPoint.z = 0.0;*/

		// TURTLEBOT MAP
		goalPoint.x = -0.96;
		goalPoint.y = -2.40;
		goalPoint.z = 0.0;

		ROS_INFO_STREAM("C");
		break;
	case D:
		// MBOT MAP
		/*goalPoint.x = -0.39;
		 goalPoint.y = 1.69;
		 goalPoint.z = 0.0;*/

		// TURTLEBOT MAP
		goalPoint.x = 0.94;
		goalPoint.y = 4.04;
		goalPoint.z = 0.0;

		ROS_INFO_STREAM("D");
		break;
	default:
		ROS_ERROR("Unsupported letter!");
	}

	// TODO: Subscribe to /amcl_position and get the current quaternion
	orientationPoint.x = 0.0;
	orientationPoint.y = 0.0;
	orientationPoint.z = 0.1;
	orientationPoint.w = 0.99;

	goal.target_pose.pose.position = goalPoint;
	goal.target_pose.pose.orientation = orientationPoint;

	return;
}
