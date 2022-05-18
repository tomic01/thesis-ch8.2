1. Launch the map_server somewhere, localy on turtlebot, or on the server. The map does not have a namespace so that more robots can shere it (this may change, since different maps are required by different robots)

2. Launch turtlebot_agent.launch with robot_name:=<your_robot_name>

3. Launch rviz and load given configuration in case your robot name is 'turtlebot_1'. Otherwise set all paths to your new namespace (choosen robot name)
