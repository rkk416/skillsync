package skillsync.ai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeTextExtractorTest {
    @TempDir Path tempDir;

    @Test void extractsUtf8TextResume() throws Exception {
        Path file = tempDir.resolve("resume.txt");
        Files.writeString(file, "Java SQL PostgreSQL", StandardCharsets.UTF_8);

        String text = ResumeTextExtractor.extractText(file.toFile());

        assertTrue(text.contains("Java"));
        assertTrue(text.contains("PostgreSQL"));
    }

    @Test void extractsDocxResume() throws Exception {
        Path file = tempDir.resolve("resume.docx");
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream output = new FileOutputStream(file.toFile())) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.createRun().setText("JavaFX developer with SQL experience");
            document.write(output);
        }

        String text = ResumeTextExtractor.extractText(file.toFile());

        assertTrue(text.contains("JavaFX"));
        assertTrue(text.contains("SQL"));
    }

    @Test void extractsPdfResume() throws Exception {
        Path file = tempDir.resolve("resume.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(72, 720);
                content.showText("Backend developer with Java and SQL");
                content.endText();
            }
            document.save(file.toFile());
        }

        String text = ResumeTextExtractor.extractText(file.toFile());

        assertTrue(text.contains("Java"));
        assertTrue(text.contains("SQL"));
    }

    @Test void rejectsUnsupportedResumeFormat() throws Exception {
        Path file = tempDir.resolve("resume.rtf");
        Files.writeString(file, "{\\rtf1 Java}", StandardCharsets.UTF_8);

        ResumeExtractionException exception = assertThrows(ResumeExtractionException.class,
                () -> ResumeTextExtractor.extractText(file.toFile()));

        assertTrue(exception.getMessage().contains("Supported formats"));
    }
}
