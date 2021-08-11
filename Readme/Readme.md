INTRODUCTION

This was an assignment for my Spring 2021 Intro to Software Engineering class. No starter code was given, and the assignment description can be found as a separate file in this repo.

ENVIRONMENT

The project was completed in Java and is supposed to be launched in Eclipse.

INSTRUCTIONS

To run this program, you will be setting up a server on your local computer. You must first run the Server program and supply it with a schedule of trades as will as a list of traders (schedule.csv and traders.csv have been provided as an example). The Server is configure to run on port 3456 of the machine.

Then, you must run 2 instances of the Client program (simulating traders). Each instance will prompt the user for hostname (which should be 'localhost') and port number (which should be '3456').

After these steps have been completed, the server will begin going through the schedule of trades. The server will "release" trades at their specified execution time and traders will "execute" these trades when they are available.
