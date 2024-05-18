package com.studentapi.sapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/csv")
public class CsvController {

    private static final Logger logger = LoggerFactory.getLogger(CsvController.class);
    private List<String[]> processedRecords = new ArrayList<>();

    @Operation(summary = "Upload CSV file", description = "Uploads a CSV file and processes it to check student eligibility.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processed successfully", content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "400", description = "Invalid input or file", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.error("No file uploaded.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
        }
        try {
            InputStream inputStream = file.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            List<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader).getRecords();

            List<String[]> updatedRecords = new ArrayList<>();

            for (CSVRecord record : records) {
                String rollNumber = record.get("roll number");
                String studentName = record.get("student name");
                int science = Integer.parseInt(record.get("science"));
                int maths = Integer.parseInt(record.get("maths"));
                int english = Integer.parseInt(record.get("english"));
                int computer = Integer.parseInt(record.get("computer"));

                String originalEligible = record.get("Eligible");

                String eligible = (science > 85 && maths > 90 && english > 75 && computer > 95) ? "YES" : "NO";

                if (!originalEligible.equalsIgnoreCase("ToBeChecked")) {
                    eligible = originalEligible;
                }
                updatedRecords.add(new String[]{rollNumber, studentName, String.valueOf(science), String.valueOf(maths), String.valueOf(english), String.valueOf(computer), eligible});
            }

            processedRecords.clear();
            processedRecords.addAll(updatedRecords);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("roll number", "student name", "science", "maths", "english", "computer", "Eligible"))) {
                for (String[] record : processedRecords) {
                    csvPrinter.printRecord((Object[]) record);
                }
            }

            byte[] csvBytes = byteArrayOutputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "updated_students.csv");

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            logger.error("Error processing file: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        } catch (NumberFormatException e) {
            logger.error("Error parsing CSV file: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing CSV file: " + e.getMessage());
        }
    }

    @Operation(summary = "Get student eligibility", description = "Checks if a student is eligible based on roll number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eligibility status found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @GetMapping(value = "/eligibility/{rollNumber}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEligibility(@PathVariable("rollNumber") String rollNumber) {
        for (String[] record : processedRecords) {
            if (record[0].equalsIgnoreCase(rollNumber.trim())) { 
                String eligibility = record[6];
                return ResponseEntity.ok("{\"rollNumber\": \"" + record[0] + "\", \"eligible\": \"" + eligibility + "\"}");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
    }
}
