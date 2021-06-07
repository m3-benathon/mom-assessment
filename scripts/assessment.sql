USE master
GO

CREATE DATABASE assessment;
GO

USE assessment
GO

CREATE TABLE household (
	id BIGINT PRIMARY KEY,
	housing_type VARCHAR(50) NOT NULL
);
GO

CREATE TABLE family_member (
	id BIGINT PRIMARY KEY,
	household_id BIGINT FOREIGN KEY REFERENCES household(id) NOT NULL,
	name VARCHAR(255) NOT NULL,
	gender VARCHAR(50) NOT NULL,
	marital_status VARCHAR(50) NOT NULL,
	spouse_id BIGINT FOREIGN KEY REFERENCES family_member(id) NULL,
	occupation_type VARCHAR(50) NOT NULL,
	annual_income NUMERIC(18,2) NOT NULL,
	date_of_birth DATE NOT NULL
);
GO