package skillsync.ai;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

final class PDFResumeParser implements ResumeParser {
    @Override
    public String extractText(File resumeFile) {
        try (PDDocument document = Loader.loadPDF(resumeFile)) {
            if (document.isEncrypted()) {
                throw new ResumeExtractionException("The selected PDF is encrypted. Please upload an unlocked resume.");
            }
            String text = new PDFTextStripper().getText(document);
            if (text == null || text.isBlank()) {
                throw new ResumeExtractionException("No readable text was found in the selected PDF.");
            }
            return text;
        } catch (ResumeExtractionException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ResumeExtractionException("Unable to read the selected PDF. The file may be corrupted.", exception);
        }
    }
}
