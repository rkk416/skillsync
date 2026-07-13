package skillsync.ai;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

final class DOCXResumeParser implements ResumeParser {
    @Override
    public String extractText(File resumeFile) {
        try (FileInputStream input = new FileInputStream(resumeFile);
             XWPFDocument document = new XWPFDocument(input);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            if (text == null || text.isBlank()) {
                throw new ResumeExtractionException("No readable text was found in the selected DOCX resume.");
            }
            return text;
        } catch (ResumeExtractionException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new ResumeExtractionException("Unable to read the selected DOCX resume. The file may be corrupted or unsupported.", exception);
        }
    }
}
