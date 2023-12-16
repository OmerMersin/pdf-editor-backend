package com.pdfeditor.pdfeditor.service;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Service
public class PdfService {

    public String createPdf(String id) {
        return "Successfully created " + id;
    }

    public String uploadPdf(String fileName, byte[] content) throws IOException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                .build()
                .getService();

        // Read PDF file
//        Path path = Paths.get("/Users/omermersin/Developer/final/pdf-js/pdf/sa.pdf");
//        byte[] fileContent = Files.readAllBytes(path);

        // Upload PDF to Firebase Storage
        BlobId blobId = BlobId.of("pdf-editor-5f9cc.appspot.com", "any" + "/" + fileName + ".pdf");
        Blob blob = storage.create(BlobInfo.newBuilder(blobId).build(), content);

        System.out.println("File uploaded to: " + blob.getMediaLink());
        return "Successfully uploaded " + fileName;
    }

    public static byte[] downloadPdf(String fileName) throws IOException {

//        String filePath = "/Users/omermersin/Developer/final/pdf-js/pdf/sample.pdf";
//
//        Path path = Paths.get(filePath);
//
//        return Files.readAllBytes(path);

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                .build()
                .getService();

        // Download PDF from Firebase Storage
        BlobId blobId = BlobId.of("pdf-editor-5f9cc.appspot.com", "any" + "/" + fileName + ".pdf");
        Blob blob = storage.get(blobId);

        return blob.getContent();
    }

    public byte[] downloadPdfPage(String fileName, int pageNumber) throws IOException {
        try (PDDocument pdfDocument = Loader.loadPDF(new ByteArrayInputStream(downloadPdf(fileName)))) {
            // Validate the page number
            int totalPages = pdfDocument.getNumberOfPages();
            if (pageNumber < 1 || pageNumber > totalPages) {
                throw new IllegalArgumentException("Invalid page number");
            }

            // Create a new document and add the specific page
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PDDocument newDocument = new PDDocument();
                newDocument.addPage(new PDPage(pdfDocument.getPage(pageNumber - 1).getCOSObject()));

                // Save the modified document to the output stream
                newDocument.save(outputStream);
                newDocument.close();

                return outputStream.toByteArray();
            }
        }
    }

    public byte[] downloadSortedPdf(String fileName, List<Integer> pageOrder) throws IOException {
        // Download the original PDF content
        byte[] originalPdfContent = downloadPdf(fileName);

        // Create a new document based on the sorted page order
        try (PDDocument originalDocument = Loader.loadPDF(new ByteArrayInputStream(originalPdfContent))) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PDDocument newDocument = new PDDocument();

                for (int pageNumber : pageOrder) {
                    // Validate the page number
                    int totalPages = originalDocument.getNumberOfPages();
                    if (pageNumber < 1 || pageNumber > totalPages) {
                        throw new IllegalArgumentException("Invalid page number");
                    }

                    // Add the specific page to the new document
                    newDocument.addPage(new PDPage(originalDocument.getPage(pageNumber - 1).getCOSObject()));
                }

                // Save the modified document to the output stream
                newDocument.save(outputStream);
                newDocument.close();

                return outputStream.toByteArray();
            }
        }
    }

    public byte[] mergePdf(byte[] pdfContent1, byte[] pdfContent2) throws IOException {
        // Load the existing PDFs
        try (PDDocument pdf1Doc = Loader.loadPDF(new ByteArrayInputStream(pdfContent1));
             PDDocument pdf2Doc = Loader.loadPDF(new ByteArrayInputStream(pdfContent2))) {

            // Iterate through the pages of the second PDF and add each page to the first PDF
            for (PDPage page : pdf2Doc.getPages()) {
                pdf1Doc.addPage(page);
            }

            // Save the merged document to the output stream
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                pdf1Doc.save(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    public byte[] mergePdfAndUpload(String fileName1, String fileName2) throws IOException {
        try {
            // Download individual PDFs
            byte[] pdfContent1 = downloadPdf(fileName1);
            byte[] pdfContent2 = downloadPdf(fileName2);

            // Merge PDFs
            byte[] mergedPdfContent = mergePdf(pdfContent1, pdfContent2);

            // Upload the merged PDF back to Firebase Storage
            String mergedFileName = "merged-" + fileName1 + "-" + fileName2;
            uploadPdf(mergedFileName, mergedPdfContent);

            return mergedPdfContent;
        } catch (IOException e) {
            throw new RuntimeException("Error merging and uploading PDFs", e);
        }
    }

    public byte[] mergePdfAndReturn(String fileName1, String fileName2) throws IOException {
        try {
            // Download individual PDFs
            byte[] pdfContent1 = downloadPdf(fileName1);
            byte[] pdfContent2 = downloadPdf(fileName2);

            // Merge PDFs
            byte[] mergedPdfContent = mergePdf(pdfContent1, pdfContent2);

            return mergedPdfContent;
        } catch (IOException e) {
            throw new RuntimeException("Error merging PDFs", e);
        }
    }



    @Scheduled(fixedRate = 3600000) // Run every hour
    public void deleteOldFiles() {
        try {
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/serviceAccountKey.json")))
                    .build()
                    .getService();

            // Specify the folder or bucket where the files are stored
            String folder = "pdf-editor-5f9cc.appspot.com";
            String prefix = "any/"; // Adjust this based on your file structure

            // List files in the specified folder
            Page<Blob> blobs = storage.list(folder, Storage.BlobListOption.prefix(prefix));
            for (Blob blob : blobs.iterateAll()) {
                // Check the creation time of each file
                long createTimeMillis = blob.getCreateTime();
                Instant createTime = Instant.ofEpochMilli(createTimeMillis);
                Instant twoHoursAgo = Instant.now().minusSeconds(2 * 3600); // 2 hours ago

                // Delete the file if it's older than 2 hours
                if (createTime.isBefore(twoHoursAgo)) {
                    storage.delete(blob.getBlobId());
                    System.out.println("File deleted: " + blob.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately based on your application's error handling strategy
        }
    }
}
