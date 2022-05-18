package se.oru.socially_aware_planner_pkg.im.tests;

import se.oru.socially_aware_planner_pkg.im.institutions.TheGameInstitution;
import se.oru.socially_aware_planner_pkg.im.institutions.ThePEIS2Domain;
import se.oru.socially_aware_planner_pkg.im.institutions.groundings.TheGameGrounding01;
import se.oru.socially_aware_planner_pkg.im.institutions.groundings.TheGameGroundingTurtlebot1;

public class TestAdmissability {

	public static void main(String[] args) {
		// Create the institution
		TheGameInstitution theGame = new TheGameInstitution();
		// Create the domain
		ThePEIS2Domain theDomain = new ThePEIS2Domain();
		// Create the grounding
		TheGameGrounding01 theGrounding01 = new TheGameGrounding01(theGame, theDomain);
		// Create another grounding
		TheGameGroundingTurtlebot1 theGrounding02 = new TheGameGroundingTurtlebot1(theGame, theDomain);

		// Check if the grounding is admissible
		if (theGrounding01.isAdmissibleGrounding()) {
			System.out.println("\nAdmissible!!!");
		} else {
			System.out.println("\nNOT Admissible!!!");
		}

		if (theGrounding02.isAdmissibleGrounding()) {
			System.out.println("\nAdmissible 02!!!");
		} else {
			System.out.println("\nNOT Admissible 02!!!");
		}

	}
	
}
