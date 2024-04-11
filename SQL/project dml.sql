
INSERT INTO members (firstname, lastname, email, join_date, password_hash)
VALUES
    ('John', 'Doe', 'johndoe@example.com', '2023-01-15', 'johndoe'),
    ('Jane', 'Smith', 'janesmith@example.com', '2022-02-20', 'password1'),
    ('Alice', 'Johnson', 'alicejohnson@example.com', '2023-03-10', 'alicepassword.');

-- Inserting data into the staff table
INSERT INTO staff (firstname, lastname, email, password_hash)
VALUES
	('Donald', 'Williams', 'williams@example.com', 'donaldstaff'),
	('Daniel', 'Gafford', 'daniel21@example.com', 'gafford21'),
	('Bukayo','Saka','saka7@example.com','spicysaka');

-- Inserting data into the trainers table
INSERT INTO trainers (firstname, lastname, email, speciality, password_hash)
VALUES
	('Steve','Johnson','johnson@example.com', 'STRENGTH', 'stevenson'),
	('Kevin', 'Paul', 'paul0@example.com', 'CARDIO', 'thekevinpaul'),
	('Rico','Lewis','lewishealth@example.com', 'WATER-BASED', 'rico222');

-- Inserting data into the rooms table
INSERT INTO rooms (equipment_type, need_maintenance, staff_id)
VALUES
    ('WATER-BASED', true, 1),
    ('STRENGTH', false, 1); 

-- Inserting data into the sessions table
INSERT INTO sessions (room_id, trainer_id, date, starting_time, end_time, status, type)
VALUES
	('1','1','2024-01-04', '12:30', '13:30', 'staff_confirmed','PUBLIC'),
    ('1','1','2024-01-04', '15:30', '16:30', 'staff_confirmed','PRIVATE');


	

-- Inserting data into the session_members table
INSERT INTO session_members (session_id, member_id)
VALUES
	(1, 1);

--Inserting data into the trainerschedule table
INSERT INTO trainerschedule (trainer_id, dayOfWeek, starting_time, end_time)
VALUES
	('1', 'MONDAY', '8:00','9:00'),
    ('1', 'MONDAY', '12:30','13:30');



--Inserting data into the bills table
INSERT INTO bills (member_id, amount, status)
VALUES
	('1', '80.00', 'unpaid');
	
-- Inserting data into the goals table
INSERT INTO goals (member_id, goal_type, goal_value, deadline,status)
VALUES
    (1, 'Weight Loss', 'Lose 10 pounds', '2024-06-30', 'not yet achieved'),
    (2, 'Muscle Gain', 'Increase muscle mass by 5%', '2024-07-15', 'not yet achieved' );

-- Inserting data into the healthMetrics table
INSERT INTO healthMetrics (member_id, weight, height, blood_pressure, record_date)
VALUES
    (1, 70.5, 175.0, '120.0', '2024-04-05'),
    (2, 65.2, 168.0, '110.0', '2024-04-06');

-- WATER-BASED exercises
INSERT INTO exercise_routine (routine_name, category, duration_minutes, intensity_level)
VALUES 
    ('Swimming Laps', 'WATER-BASED', 45, 6),
    ('Aqua Aerobics Class', 'WATER-BASED', 60, 4),
    ('Water Polo Practice', 'WATER-BASED', 90, 7),
    ('Scuba Diving Training', 'WATER-BASED', 120, 8);

-- STRENGTH exercises
INSERT INTO exercise_routine (routine_name, category, duration_minutes, intensity_level)
VALUES 
    ('Deadlifts', 'STRENGTH', 40, 8),
    ('Barbell Squats', 'STRENGTH', 30, 7),
    ('Bench Press', 'STRENGTH', 45, 6),
    ('Dumbbell Lunges', 'STRENGTH', 35, 7);

-- CARDIO exercises
INSERT INTO exercise_routine (routine_name, category, duration_minutes, intensity_level)
VALUES 
    ('Morning Run', 'CARDIO', 45, 6),
    ('Indoor Cycling Class', 'CARDIO', 60, 5),
    ('HIIT Workout', 'CARDIO', 30, 8),
    ('Jump Rope Session', 'CARDIO', 20, 7);

-- MINDBODY exercises
INSERT INTO exercise_routine (routine_name, category, duration_minutes, intensity_level)
VALUES 
    ('Guided Meditation', 'MINDBODY', 20, 2),
    ('Vinyasa Yoga Flow', 'MINDBODY', 60, 3),
    ('Tai Chi Practice', 'MINDBODY', 45, 4),
    ('Pilates Reformer Class', 'MINDBODY', 50, 5);

