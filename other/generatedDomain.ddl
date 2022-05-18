##################
# Reserved words #
#################################################################
#                                                               #
#   Head                                                        #
#   Resource                                                    #
#   Sensor                                                      #
#   Actuator                                                    #
#   ContextVariable                                             #
#   SimpleOperator                                              #
#   PlanningOperator                                            #
#   Domain                                                      #
#   Constraint                                                  #
#   RequiredState                                               #
#   AchievedState                                               #
#   RequiredResource                                            #
#   All AllenIntervalConstraint types                           #
#   '[' and ']' should be used only for constraint bounds       #
#   '(' and ')' are used for parsing                            #
#                                                               #
#################################################################

(Domain Institutions)

# SENSORS
(Sensor command)

# UNARY RESOURCES

(Resource Runner_resource 1)
(Resource Catcher_resource 1)

# ACTUATORS
(Actuator Runner)
(Actuator Catcher)

# CONTEXT VARIABLES
(ContextVariable institution_start)

##### INSTITUTION GOALS
(SimpleOperator
(Head institution_start::TheGame)

(RequiredState req_cmd command::TheGame)
(RequiredState goal1 Runner::Goal_VisitAllLetters)
(RequiredState goal2 Catcher::Goal_CatchAllRunners)
)

(SimpleOperator
(Head Catcher::Goal_CatchAllRunners)
(RequiredState req1 Catcher::Catch)
(RequiredState req_res Catcher_mobility::used)
(Constraint MetByOrAfter(Head, req1))
(Constraint Duration[3000,INF](req1))
(Constraint During(req1,req_res))
)

(SimpleOperator
(Head Runner::Goal_VisitAllLetters)

(RequiredState req1 Runner::EscapeMove_A)
(RequiredState req2 Runner::EscapeMove_B)
(RequiredState req3 Runner::EscapeMove_C)
(RequiredState req4 Runner::EscapeMove_D)

(Constraint Duration[5000,INF](req1))
(Constraint Duration[5000,INF](req2))
(Constraint Duration[5000,INF](req3))
(Constraint Duration[5000,INF](req4))
(Constraint MetByOrAfter(Head,req4))
(Constraint MetByOrAfter(Head,req3))
(Constraint MetByOrAfter(Head,req2))
(Constraint MetByOrAfter(Head,req1))
)

##### INSTITUTION NORMS

(SimpleOperator
(Head Runner::EscapeMove_A)
(RequiredState req1 Runner::SayWord)
(RequiredState req_res Runner_mobility::used)
(Constraint MetBy(Head,req1))
(Constraint Duration[1000,2000](req1))
(Constraint During(Head,req_res))
)

(SimpleOperator
(Head Runner::EscapeMove_B)
(RequiredState req1 Runner::SayWord)
(RequiredState req_res Runner_mobility::used)
(Constraint MetBy(Head,req1))
(Constraint Duration[1000,2000](req1))
(Constraint During(Head,req_res))
)

(SimpleOperator
(Head Runner::EscapeMove_C)
(RequiredState req1 Runner::SayWord)
(RequiredState req_res Runner_mobility::used)
(Constraint MetBy(Head,req1))
(Constraint Duration[1000,2000](req1))
(Constraint During(Head,req_res))
)

(SimpleOperator
(Head Runner::EscapeMove_D)
(RequiredState req1 Runner::SayWord)
(RequiredState req_res Runner_mobility::used)
(Constraint MetBy(Head,req1))
(Constraint Duration[1000,2000](req1))
(Constraint During(Head,req_res))
)

#### RESOURCE HANDLE
(SimpleOperator

(Head Runner_mobility::used)

(RequiredResource Runner_resource(1))

)

(SimpleOperator

(Head Catcher_mobility::used)

(RequiredResource Catcher_resource(1))

)
