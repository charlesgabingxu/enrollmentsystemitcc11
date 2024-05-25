 VIDEO INSTRUCTION HOW THIS PROGRAM WORKS
https://drive.google.com/drive/folders/1hJf1yjxtkUTz6U1LIBOYfEdGBMWR6cIe?usp=sharing

TEXT INSTRUCTION

Start your mySQL server, in my case, Go launch CMD with directory of MYSQL SERVER BIN
1st.
I use CLI which by typing in cmd
>mysqld --console
this activates my mysql server

2nd.
Open another cmd same directory of MYSQL SERVER BIN
>mysql -u (your username) -p
this allows me to login in mysql

3rd.
Input these commands to create the database necessary for the data storing of the program.
CREATE DATABASE enrollment;

use enrollment;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE enrollments (
    user_id INT,
    name VARCHAR(255),
    date_of_birth DATE,
    age INT,
    gender ENUM('Male', 'Female', 'Other'),
    phone_number VARCHAR(20),
    address TEXT,
    course ENUM('Computer Science', 'Law', 'Medicine', 'Business Administration', 'Agriculture'),
    FOREIGN KEY (user_id) REFERENCES users(id)
);


After all this is done, the code should work perfectly fine.
