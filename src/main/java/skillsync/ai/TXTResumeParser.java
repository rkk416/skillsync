package skillsync.ai;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

final class TXTResumeParser implements ResumeParser {
    @Override
    public String extractText(File resumeFile) {
        try {
            String text = Files.readString(resumeFile.toPath(), StandardCharsets.UTF_8);
            if (text.isBlank()) {
                throw new ResumeExtractionException("No readable text was found in the selected text resume.");
            }
            return text;
        } catch (ResumeExtractionException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ResumeExtractionException("Unable to read the selected text resume.", exception);
        }
    }
}
