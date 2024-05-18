# Student API Project

## Overview

The Student API is a Spring Boot application designed to upload and process CSV files containing student information. The application evaluates student eligibility based on predefined criteria and provides the results in a downloadable CSV file. The project also includes Swagger documentation for the API endpoints.

## Features

- Upload CSV files containing student data.
- Process and evaluate student eligibility based on given criteria.
- Download the processed CSV file with eligibility results.
- API documentation using OpenAPI/Swagger.

## Prerequisites

Before running this application, ensure you have the following installed:

- Java 11 or higher
- Maven 3.6.0 or higher

## Setup Instructions

### 1. Clone the Repository

git clone [https://github.com/yourusername/studentapi.git](https://github.com/Tirth2742/student-service-API.git)
cd studentapi

### 2. Build the Project

mvn clean install

### 3. Run the Application

mvn spring-boot:run

### 4. Access the Application

The application will be accessible at http://localhost:8080.
Swagger UI for API documentation can be accessed at http://localhost:8080/swagger-ui.html.

## Using Swagger UI

1. Open your browser and go to http://localhost:8080/swagger-ui.html.
2. Click on the POST /api/csv/upload endpoint to expand it.
3. Click on the Try it out button.
4. Choose the CSV file with the format mentioned above:

    roll number,student name,science,maths,english,computer
    1,John Doe,88,92,78,96
    2,Jane Smith,85,89,75,94

    note: sample csv file (students_data.csv) has been provided for testing the application

5. Click Execute.
6. Download the updated CSV file from the response section.

## API Usage

### Endpoint: Upload CSV File

URL: /api/csv/upload
Method: POST
Content-Type: multipart/form-data
Parameters:
    file (required): The CSV file to be uploaded.

Example Request (using cURL)

curl -X POST "http://localhost:8080/api/csv/upload" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@/path/to/your/students.csv"

### Response

200 OK: File processed successfully. Returns the processed CSV file.
400 Bad Request: Invalid input or file.
500 Internal Server Error: Server error while processing the file.

## CSV File Format

The CSV file should have the following columns:

roll number
student name
science
maths
english
computer
