package skillsync.ai;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public final class ResumeTextExtractor {

    private ResumeTextExtractor() {
    }

    public static String extractText(File pdfFile) {

        System.out.println("========== ResumeTextExtractor ==========");
        System.out.println(pdfFile);

        if (pdfFile == null) {
            throw new RuntimeException("Resume file not found.");
        }

        System.out.println(pdfFile.getName());

        try (PDDocument document = Loader.loadPDF(pdfFile)) {

            System.out.println("PDF Loaded Successfully");

            PDFTextStripper stripper = new PDFTextStripper();

            String text = stripper.getText(document);

            System.out.println("Characters : " + text.length());

            return text;

        } catch (IOException e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to read PDF : " + e.getMessage(),
                    e
            );
        }

    }

}