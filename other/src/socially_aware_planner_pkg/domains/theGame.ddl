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
# someone's request to change contexts (institutions)
(Sensor command)
# (Sensor restart)

# ACTUATORS
(Actuator Runner)
(Actuator Catcher)
(Actuator IM)

# CONTEXT VARIABLES
(ContextVariable institution_start)
# (ContextVariable institution_end)
# (ContextVariable institution_restart)

# PLANNING NORMS

(SimpleOperator
(Head institution_start::None)

(RequiredState req_sens command::None)
(RequiredState req1 IM::__Restart1__)

(Constraint During(Head,req_sens))
)


(SimpleOperator
(Head institution_start::TheGame)

(RequiredState req_start command::TheGame)
(RequiredState req1 Runner::SayWord(Letter))
(RequiredState req2 Runner::EscapeMove(Letter))
(RequiredState req3 Catcher::Catch())
(RequiredState req4 IM::__Restart2__)

(Constraint Starts(req_start,Head))
(Constraint Duration[3000,INF](Head))
(Constraint Duration[3000,INF](req1))
(Constraint Duration[3000,INF](req2))
(Constraint Duration[3000,INF](req3))
(Constraint MetByOrAfter(req2,req1))
(Constraint MetByOrAfter(req4,req2))
(Constraint Finishes(req4,Head))
# Try that the end of sensors finishes context...
)

# (SimpleOperator
# (Head institution_start::None)
# (RequiredState req_restart restart::restart)
# (RequiredState req1 IM::__Restart3__)
# (Constraint Starts(req_restart, Head))
# (Constraint During(req1, Head))
# )


# (SimpleOperator
# (Head institution_restart::TheGame)
# (RequiredState req_restart restart::restart)
# (RequiredState req1 IM::__Restart2__)
# (Constraint Starts(req_restart, Head))
# (Constraint During(req1, Head))
# )