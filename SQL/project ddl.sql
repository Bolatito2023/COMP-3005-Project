
CREATE TYPE CATEGORY AS ENUM ('WATER-BASED', 'STRENGTH', 'CARDIO', 'MINDBODY');
CREATE TYPE SESSION_TYPE AS ENUM ('PRIVATE', 'PUBLIC');

CREATE TABLE members (
    member_id SERIAL PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    join_date DATE,
    password_hash VARCHAR(255)
);

CREATE TABLE trainers (
    trainer_id SERIAL PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    speciality VARCHAR(255) NOT NULL ,
    password_hash VARCHAR(255)
);

CREATE TABLE staff (
	staff_id SERIAL PRIMARY KEY,
	firstname VARCHAR(255) NOT NULL,
    	lastname VARCHAR(255) NOT NULL,
	email VARCHAR(255) UNIQUE,
	password_hash VARCHAR(255)
);

CREATE TABLE rooms (
    room_id SERIAL PRIMARY KEY,
    equipment_type CATEGORY NOT NULL,
    need_maintenance BOOLEAN NOT NULL,
    staff_id INT,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

CREATE TABLE sessions (
    session_id SERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    trainer_id INT NOT NULL,
    date DATE NOT NULL,
    starting_time TIME NOT NULL,
    end_time TIME NOT NULL CHECK (end_time > starting_time),
    status VARCHAR(255) NOT NULL,
    type SESSION_TYPE NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainers(trainer_id) ON DELETE CASCADE
);

CREATE TABLE session_members (
    session_id INT NOT NULL,
    member_id INT NOT NULL,
    PRIMARY KEY (session_id, member_id),
    FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

CREATE TABLE trainerschedule (
    schedule_id SERIAL PRIMARY KEY,
    trainer_id INT NOT NULL,
    dayOfWeek VARCHAR(255) NOT NULL,
    starting_time TIME NOT NULL,
    end_time TIME NOT NULL CHECK (end_time > starting_time),
    FOREIGN KEY (trainer_id) REFERENCES trainers(trainer_id) ON DELETE CASCADE
);

CREATE TABLE bills (
    bill_id SERIAL PRIMARY KEY,
    member_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'unpaid',
    FOREIGN KEY (member_id) REFERENCES members(member_id)

    );
-- Creating the Goal table
CREATE TABLE goals (
    goal_id SERIAL PRIMARY KEY,
    member_id INT,
    goal_type VARCHAR(50),
    goal_value VARCHAR(100), 
    deadline VARCHAR(50),
    status VARCHAR(255) DEFAULT 'not yet achieved',
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);

-- Creating the HealthMetrics table
CREATE TABLE healthMetrics (
    metric_id SERIAL PRIMARY KEY,
    member_id INT NOT NULL,
    weight DECIMAL(5,2) NOT NULL, 
    height DECIMAL(5,2) NOT NULL, 
    blood_pressure VARCHAR(20) NOT NULL,
    record_date DATE NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);
CREATE TABLE exercise_routine (
    routine_id SERIAL PRIMARY KEY,
    routine_name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    duration_minutes VARCHAR(255) NOT NULL,
    intensity_level VARCHAR(255) NOT NULL
    );
 CREATE TABLE my_exercise_routine (
    member_id INT NOT NULL,
    routine_name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    duration_minutes INT NOT NULL,
    intensity_level INT NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(member_id)
    );