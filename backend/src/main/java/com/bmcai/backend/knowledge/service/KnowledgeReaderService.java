package com.bmcai.backend.knowledge.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class KnowledgeReaderService {

    public String readMarkdown(Path path) {

        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al leer el archivo Markdown: " + path,
                    e
            );
        }
    }

    public String readPdf(Path path) {

        try (var document = Loader.loadPDF(path.toFile())) {

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error al leer el archivo PDF: " + path,
                    e
            );
        }
    }

    public String read(Path path) {

        String fileName = path.getFileName()
                .toString()
                .toLowerCase();

        if (fileName.endsWith(".md")) {
            return readMarkdown(path);
        }

        if (fileName.endsWith(".pdf")) {
            return readPdf(path);
        }

        throw new IllegalArgumentException(
                "Tipo de archivo no soportado: " + path
        );
    }
}