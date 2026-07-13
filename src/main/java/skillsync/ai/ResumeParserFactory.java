package skillsync.ai;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

final class ResumeParserFactory {
    private static final String SUPPORTED_FORMATS = "Supported formats: PDF, DOCX, TXT.";

    private ResumeParserFactory() {
    }

    static ResumeParser parserFor(File resumeFile) {
        validateFile(resumeFile);
        String extension = extensionOf(resumeFile);
        String contentType = probeContentType(resumeFile);

        if (isPdf(extension, contentType)) return new PDFResumeParser();
        if (isDocx(extension, contentType)) return new DOCXResumeParser();
        if (isTxt(extension, contentType)) return new TXTResumeParser();

        throw new ResumeExtractionException("Unsupported resume format. " + SUPPORTED_FORMATS);
    }

    static String extensionOf(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot).toLowerCase(Locale.ROOT) : "";
    }

    private static void validateFile(File resumeFile) {
        if (resumeFile == null) {
            throw new ResumeExtractionException("No resume file was selected. " + SUPPORTED_FORMATS);
        }
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            throw new ResumeExtractionException("The selected resume file could not be found.");
        }
        if (!resumeFile.canRead()) {
            throw new ResumeExtractionException("The selected resume file cannot be read.");
        }
        if (resumeFile.length() == 0) {
            throw new ResumeExtractionException("The selected resume file is empty.");
        }
    }

    private static boolean isPdf(String extension, String contentType) {
        return ".pdf".equals(extension) || "application/pdf".equalsIgnoreCase(contentType);
    }

    private static boolean isDocx(String extension, String contentType) {
        return ".docx".equals(extension)
                || "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(contentType);
    }

    private static boolean isTxt(String extension, String contentType) {
        return ".txt".equals(extension) || extension.isEmpty() && "text/plain".equalsIgnoreCase(contentType);
    }

    private static String probeContentType(File resumeFile) {
        try {
            return Files.probeContentType(resumeFile.toPath());
        } catch (IOException ignored) {
            return null;
        }
    }
}
