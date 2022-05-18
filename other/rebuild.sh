#! /bin/bash
rm -rf devel
find . -name "build" | xargs rm -rf
catkin_make --pkg inst_msgs inst_srvs
catkin_make
