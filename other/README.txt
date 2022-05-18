TURTLEBOT SETTINGS:

ssh turtlebot@10.0.0.7


This will start the map server, globaly on topic /map:

roslaunch turtlebot_launch map_server.launch  


This will start the turtlebot and lunch all needed behaviors:

roslaunch turtlebot_launch turtlebot_behaviors.launch robot_name:=turtlebot_1

Additionally: Turn the device on he turtleot, so that the other robot can follow it. 

MBOT11 SETTINGS:
Launch rfid gradient descent behavior gradientMoveTo
<TODO>

MCENTRAL SETTINGS:

Monitor turtlebot (moveTo):
roslaunch turtlebot_launch turtlebot_rviz.launch

Monitor mbot11 (gradientMoveTo)
<TODO>

Launch the institution:
roslaunch socially_aware_planning_pkg sap_inst.launch 



