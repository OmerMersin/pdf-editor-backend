package com.pdfeditor.pdfeditor.controller;

import com.pdfeditor.pdfeditor.model.MergePdfRequest;
import com.pdfeditor.pdfeditor.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class PdfController {

    public PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/uploadPdf")
    public String uploadPdf(
            @RequestParam("fileName") String fileName,
            @RequestPart("file") MultipartFile file) throws IOException, ExecutionException, InterruptedException {
        return pdfService.uploadPdf(fileName, file.getBytes());
    }


    @GetMapping("/downloadPdf/{fileName}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("fileName") String fileName) throws IOException {
        byte[] pdfContent = pdfService.downloadPdf(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @GetMapping("/downloadPdfPage/{fileName}/{pageNumber}")
    public ResponseEntity<byte[]> downloadPdfPage(
            @PathVariable("fileName") String fileName,
            @PathVariable("pageNumber") int pageNumber) throws IOException {
        byte[] pdfPageContent = pdfService.downloadPdfPage(fileName, pageNumber);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfPageContent);
    }

    @CrossOrigin(origins = "http://127.0.0.1:5501")
    @PostMapping("/downloadSortedPdf/{fileName}")
    public ResponseEntity<byte[]> downloadSortedPdf(
            @PathVariable("fileName") String fileName,
            @RequestBody List<Integer> pageOrder) {
        try {
            byte[] sortedPdf = pdfService.downloadSortedPdf(fileName, pageOrder);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName + "-sorted.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(sortedPdf);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately based on your application's error handling strategy
            return ResponseEntity.status(500).build(); // Internal Server Error
        }
    }

    @PostMapping("/mergePdf/{fileName1}/{fileName2}")
    @CrossOrigin(origins = "http://127.0.0.1:5501")
    public ResponseEntity<byte[]> mergePdf(@PathVariable("fileName1") String fileName1, @PathVariable("fileName2") String fileName2) {
        try {
            // Call the service method to merge and upload the PDF
            byte[] mergedPdfContent = pdfService.mergePdfAndUpload(fileName1, fileName2);

            // Return the merged PDF content as a response
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(mergedPdfContent);
        } catch (IOException e) {
            // Handle the exception appropriately based on your application's error handling strategy
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/mergeAndReturnPdf/{fileName1}/{fileName2}")
    @CrossOrigin(origins = "http://127.0.0.1:5501")
    public ResponseEntity<byte[]> mergeAndReturnPdf(
            @PathVariable("fileName1") String fileName1,
            @PathVariable("fileName2") String fileName2) {
        try {
            byte[] mergedPdfContent = pdfService.mergePdfAndReturn(fileName1, fileName2);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(mergedPdfContent);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately based on your application's error handling strategy
            return ResponseEntity.status(500).build(); // Internal Server Error
        }
    }

}
