#include "ros/ros.h"
#include "inst_srvs/position_info_srv.h"
#include "inst_msgs/BehResult.h"
#include <math.h>
#include <sound_play/sound_play.h>
#include <unistd.h>
#include <geometry_msgs/PoseWithCovarianceStamped.h>
#include <geometry_msgs/PoseWithCovariance.h>
#include <geometry_msgs/Pose.h>

const static std::string BehaviorName = "sayWord";

std::string CalculateTheWordFromPosition();
bool IsInRadius(float center_x, float center_y);
std::string GetWordFromLetter(std::string letter);
float robotX = 0;
float robotY = 0;
float radius = 0.5f;

static const std::string ResultTopicName = "/behaviorToFinish";
static bool serviceFinished = false;

std::string word = "";

// Constantly update robot position
void positionCallback(const geometry_msgs::PoseWithCovarianceStamped &pose)
{
  robotX = pose.pose.pose.position.x;
  robotY = pose.pose.pose.position.y;
}

bool handleUserRequest(inst_srvs::position_info_srv::Request  &req,
		inst_srvs::position_info_srv::Response &res)
{
	res.success = true;
	serviceFinished = true;
	return true;
}

int main(int argc, char **argv)
{
  ros::init(argc, argv, "say_word_node");
  ros::NodeHandle n;

  ros::Subscriber sub = n.subscribe("amcl_pose", 1000, positionCallback);
  ros::ServiceServer service = n.advertiseService(BehaviorName, handleUserRequest);
  ros::Publisher pubResult = n.advertise<inst_msgs::BehResult>(ResultTopicName, 1000);
  sound_play::SoundClient soundPlay;

  ros::Rate loop(10); // loop at 10Hz // TODO: use it or remove it

  while(ros::ok())
  {
	  ROS_INFO_STREAM("\nBehavior started!");
	  // *** HANDLE THE BEHAVIOR SERVICE ***
	  while (!serviceFinished) {
		  ros::spinOnce();
	  }
	  serviceFinished = false;

	  // *** SAY THE WORD ***
	  std::string letterToSay = CalculateTheWordFromPosition();
	  ROS_INFO_STREAM("\nRobot is on letter: " << letterToSay);

	  if (letterToSay=="error")
	  {
		  ROS_INFO("ERROR, robot is unable to estimate the letter. Is robot standing on the letter?");
		  ROS_INFO_STREAM("ERROR, robot is unable to estimate the letter. Is robot standing on the letter?");
	  }

	  std::string wordToSay = GetWordFromLetter(letterToSay);
	  ROS_INFO_STREAM("The robot is speaking: " << wordToSay);
	  soundPlay.say(wordToSay.c_str());
	  sleep(3);

	  // *** PUBLISH THE RESULT MESSAGE ***
	  std::string ns = ros::this_node::getNamespace();
	  ns = ns.substr(2);

	  ROS_INFO("Publishing the result message");
	  inst_msgs::BehResult behaviorResult;
	  behaviorResult.agent_name = ns;
	  behaviorResult.beh_name = BehaviorName;
	  behaviorResult.success = true;
	  pubResult.publish(behaviorResult);
	  ros::spinOnce();
  }

  return 0;
}

std::string CalculateTheWordFromPosition()
{
	// For different robots different maps --> different coordinates
	// TODO: This could be somewhere else, rosparams maybe? Is it a part of grounding?
	// MBOT MAP
	/*float centerA_x = 0.90;
	float centerA_y = -0.77;

	float centerB_x = 3.8;
	float centerB_y = 1.3;

	float centerC_x = 6.3;
	float centerC_y = 2.84;

	float centerD_x = -0.39;
	float centerD_y = 1.69;*/

	// TURTLEBOT MAP
	float centerA_x = -1.81;
	float centerA_y = 3.95;

	float centerB_x = -1.07;
	float centerB_y = 0.27;

	float centerC_x = -0.96;
	float centerC_y = -2.40;

	float centerD_x = 0.94;
	float centerD_y = 4.04;

	if (IsInRadius(centerA_x,centerA_y))
	{
		return "A";
	} else if (IsInRadius(centerB_x,centerB_y)){
		return "B";
	} else if (IsInRadius(centerC_x,centerC_y)){
		return "C";
	} else if (IsInRadius(centerD_x,centerD_y)){
		return "D";
	}

	return "error";

}

bool IsInRadius(float center_x, float center_y)
{
	float distTmp = pow(robotX - center_x, 2) + pow(robotY - center_y, 2);
	float distance = sqrt(distTmp);

	if (distance > radius)
	{
		//ROS_INFO_STREAM("NOT IN RADIUS: Distance to the center: " << distance << "Radius: " << radius);
		return false;
	}

	return true;
}

std::string GetWordFromLetter(std::string letter)
{
	if (letter == "A")
	{
		return "Apple Apple";
	} else if (letter == "B")
	{
		return "Baby Baby";
	} else if (letter == "C")
	{
		return "Carrot Carrot";
	} else if (letter == "D"){
		 return "Door Door";
	}

	return "No meaningful letter, sorry";
}
