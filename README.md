HEALTH AND FITNESS CLUB
COMP 3004 WINTER 2024 GROUP PROJECT

The project's objective is to design and implement a health and fitness club system that executes various functions that would be discussed later in this document. 
It serves different functions for different users such as members, administrative staff and trainers. 
The Health and Fitness Application uses Java (IntelliJ) and PostgreSQL. 

INSTALLATION AND SET UP:
In order to set up the system correctly ensure that the database is created on PGadmin before attempting to interact with the Java subsystem. 
To do this, create a new database named 'HealthAndFitnessClub' and use the DDL file stored in the 'SQL' file to create all the tables. 
After doing this you are almost all set to start running the application. In Java edit the username, password and portnumber fields stored in the FitnessApp class. You would need the correct parameters
in order to communicate with the database. Check for the correct parameters in PGadmin. The username is usually named 'postgres' and the portnumber can be found....

In order to test some functionalities you may need to add some data into the table first. This is because the member, staff and trainer tables have hashpassword attributes that can not be retrieved
from IntelliJ. You may need to register two members, one trainer and one administrative staff
