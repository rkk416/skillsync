package skillsync.ai;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ResumeTextExtractor {
    private static final Logger LOGGER = Logger.getLogger(ResumeTextExtractor.class.getName());

    private ResumeTextExtractor() {
    }

    public static String extractText(File resumeFile) {
        try {
            String text = ResumeParserFactory.parserFor(resumeFile).extractText(resumeFile).trim();
            if (text.isBlank()) {
                throw new ResumeExtractionException("No readable text was found in the selected resume.");
            }
            return text;
        } catch (ResumeExtractionException exception) {
            LOGGER.warning(exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Unexpected resume extraction failure", exception);
            throw new ResumeExtractionException("Unable to read the selected resume. Please verify the file and try again.", exception);
        }
    }
}
