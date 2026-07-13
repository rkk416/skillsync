package skillsync.ai;

import java.io.File;

interface ResumeParser {
    String extractText(File resumeFile);
}
