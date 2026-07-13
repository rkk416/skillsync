package skillsync.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Client for the Google Gemini REST API that analyzes extracted resume text
 * and returns a structured career-readiness assessment.
 * <p>
 * This service sends the resume text to Gemini with an instruction to
 * respond with a single strict JSON object, then parses that response into
 * a {@link GeminiResponse}. Network configuration (endpoint, API key) is
 * sourced from {@link GeminiConfig}, keeping credentials and environment
 * details out of this class.
 */
public final class GeminiService {

    /** Media type used for the Gemini API request body. */
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    /** Connection timeout applied to the underlying HTTP client. */
    private static final long CONNECT_TIMEOUT_SECONDS = 15;

    /** Read timeout applied to the underlying HTTP client, generous to accommodate model latency. */
    private static final long READ_TIMEOUT_SECONDS = 60;

    /** Write timeout applied to the underlying HTTP client. */
    private static final long WRITE_TIMEOUT_SECONDS = 15;

    /** Query parameter name Gemini expects the API key under. */
    private static final String API_KEY_QUERY_PARAM = "key";

    /** Instruction sent alongside the resume text, constraining the model to a strict JSON contract. */
    private static final String RESPONSE_FORMAT_INSTRUCTION = """
            You are an AI resume and career-readiness analyzer for a placement platform.
            Analyze the resume text provided below and respond with ONLY a single strict, valid JSON object,
            with no markdown formatting, no code fences, and no explanatory text before or after it.
            The JSON object must strictly follow this exact schema:
            {
              "atsScore": 0,
              "placementScore": 0,
              "skills": [],
              "missingSkills": [],
              "matchedCompanies": [],
              "summary": ""
            }
            Field rules:
            - atsScore: integer from 0 to 100 representing ATS compatibility.
            - placementScore: integer from 0 to 100 representing overall placement readiness.
            - skills: array of technical skill names detected in the resume.
            - missingSkills: array of commonly expected technical skill names not found in the resume.
            - matchedCompanies: array of company names whose typical requirements best match the detected skills.
            - summary: a concise, professional summary of the candidate's readiness, under 60 words.

            Resume text:
            """;

    private final OkHttpClient httpClient;
    private final Gson gson;

    /**
     * Creates a service instance with a dedicated {@link OkHttpClient}
     * configured with production-appropriate timeouts.
     */
    public GeminiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }

    /**
     * Creates a service instance with an explicit {@link OkHttpClient},
     * primarily intended for unit testing with a mocked or instrumented client.
     *
     * @param httpClient the HTTP client to use for Gemini API calls
     */
    public GeminiService(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new GsonBuilder().create();
    }

    /**
     * Sends extracted resume text to the Gemini API and returns a structured
     * analysis of the candidate's placement readiness.
     *
     * @param resumeText plain text extracted from the candidate's resume
     * @return the parsed {@link GeminiResponse}
     * @throws GeminiServiceException if the input is invalid, the request fails,
     *                                the API returns a non-successful status,
     *                                or the response cannot be parsed
     */
    public GeminiResponse analyzeResume(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            throw new GeminiServiceException("Resume text must not be empty.");
        }

        String requestBodyJson = buildRequestBody(resumeText);
        Request request = buildRequest(requestBodyJson);

        String rawModelText = executeRequest(request);
        String cleanedJson = extractJsonPayload(rawModelText);

        return parseGeminiResponse(cleanedJson);
    }

    /**
     * Builds the Gemini {@code generateContent} request payload, embedding
     * the format instruction and resume text as a single user message.
     *
     * @param resumeText the resume text to analyze
     * @return the JSON request body as a string
     */
    private String buildRequestBody(String resumeText) {
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", RESPONSE_FORMAT_INSTRUCTION + resumeText);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);
        JsonObject generationConfig = new JsonObject();
generationConfig.addProperty("temperature", 0.0);
generationConfig.addProperty("topP", 0.1);
generationConfig.addProperty("topK", 1);

requestBody.add("generationConfig", generationConfig);

        return gson.toJson(requestBody);
    }

    /**
     * Builds the outbound HTTP request to the Gemini API, attaching the API
     * key as a query parameter and the request body as JSON.
     *
     * @param requestBodyJson the JSON request body
     * @return a fully configured {@link Request}
     * @throws GeminiServiceException if the configured endpoint is invalid
     */
    private Request buildRequest(String requestBodyJson) {
        String endpoint;
        String apiKey;

        try {
            endpoint = GeminiConfig.getEndpoint();
            apiKey = GeminiConfig.getApiKey();
        } catch (RuntimeException ex) {
            throw new GeminiServiceException("Gemini configuration error: " + ex.getMessage(), ex);
        }

        if (endpoint == null || endpoint.isBlank()) {
            throw new GeminiServiceException("Gemini API endpoint is not configured.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiServiceException("Gemini API key is not configured.");
        }

        HttpUrl baseUrl = HttpUrl.parse(endpoint);
        if (baseUrl == null) {
            throw new GeminiServiceException("Gemini API endpoint is malformed: " + endpoint);
        }

        HttpUrl requestUrl = baseUrl.newBuilder()
                .addQueryParameter(API_KEY_QUERY_PARAM, apiKey)
                .build();

        RequestBody body = RequestBody.create(requestBodyJson, JSON_MEDIA_TYPE);

        return new Request.Builder()
                .url(requestUrl)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
    }

    /**
     * Executes the HTTP request against the Gemini API and extracts the raw
     * text produced by the model from the response envelope.
     *
     * @param request the request to execute
     * @return the model's raw text output
     * @throws GeminiServiceException if the network call fails, the API
     *                                returns a non-successful status, or the
     *                                response envelope is missing expected fields
     */
    private String executeRequest(Request request) {

    int maxRetries = 3;

    for (int attempt = 1; attempt <= maxRetries; attempt++) {

        try (Response response = httpClient.newCall(request).execute()) {

            String responseBody =
                    response.body() != null
                            ? response.body().string()
                            : "";

            if (response.isSuccessful()) {
                return extractModelText(responseBody);
            }

            // Retry only for 503 (Server Busy)
            if (response.code() == 503 && attempt < maxRetries) {

                System.out.println(
                        "Gemini server busy. Retrying... (" +
                                attempt +
                                "/" +
                                maxRetries +
                                ")"
                );

                try {
                    Thread.sleep(2000); // wait 2 seconds
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }

                continue;
            }

            throw new GeminiServiceException(
                    "Gemini API request failed with status "
                            + response.code()
                            + ":\n"
                            + responseBody
            );

        } catch (IOException ex) {

            if (attempt == maxRetries) {

                throw new GeminiServiceException(
                        "Unable to communicate with Gemini API.",
                        ex
                );

            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

        }

    }

    throw new GeminiServiceException(
            "Gemini server is currently busy. Please try again in a few minutes."
    );
}

    /**
     * Navigates the Gemini response envelope to extract the model's
     * generated text from {@code candidates[0].content.parts[0].text}.
     *
     * @param responseBody the raw JSON response body returned by the Gemini API
     * @return the model's raw generated text
     * @throws GeminiServiceException if the envelope structure is unexpected
     */
    private String extractModelText(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new GeminiServiceException("Gemini API response did not contain any candidates.");
            }

            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            JsonObject content = firstCandidate.getAsJsonObject("content");
            JsonArray parts = content.getAsJsonArray("parts");

            if (parts == null || parts.isEmpty()) {
                throw new GeminiServiceException("Gemini API response did not contain any content parts.");
            }

            JsonElement textElement = parts.get(0).getAsJsonObject().get("text");
            if (textElement == null) {
                throw new GeminiServiceException("Gemini API response did not contain generated text.");
            }

            return textElement.getAsString();
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException ex) {
            throw new GeminiServiceException("Gemini API response envelope was malformed.", ex);
        }
    }

    /**
     * Strips markdown code fences and surrounding whitespace from the
     * model's raw text output, isolating the strict JSON object it contains.
     *
     * @param rawModelText the raw text returned by the model
     * @return the cleaned JSON payload
     * @throws GeminiServiceException if no JSON object can be located
     */
    private String extractJsonPayload(String rawModelText) {
        String trimmed = rawModelText.trim();

        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int fenceEnd = trimmed.lastIndexOf("```");
            if (firstNewline >= 0 && fenceEnd > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, fenceEnd).trim();
            }
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');

        if (start < 0 || end < 0 || end < start) {
            throw new GeminiServiceException("Gemini API did not return a recognizable JSON object.");
        }

        return trimmed.substring(start, end + 1);
    }

    /**
     * Parses a cleaned strict JSON payload into a {@link GeminiResponse}.
     *
     * @param json the cleaned JSON payload
     * @return the parsed response
     * @throws GeminiServiceException if the payload cannot be parsed into the expected schema
     */
    private GeminiResponse parseGeminiResponse(String json) {
        try {
            GeminiResponse parsed = gson.fromJson(json, GeminiResponse.class);
            if (parsed == null) {
                throw new GeminiServiceException("Gemini API returned an empty analysis result.");
            }
            return parsed;
        } catch (JsonSyntaxException ex) {
            throw new GeminiServiceException("Failed to parse Gemini API analysis result as JSON.", ex);
        }
    }

    /**
     * Unchecked exception raised for any failure encountered while
     * communicating with, or interpreting responses from, the Gemini API.
     */
    public static final class GeminiServiceException extends RuntimeException {

        /**
         * Creates a new exception with the given descriptive message.
         *
         * @param message human-readable description of the failure
         */
        public GeminiServiceException(String message) {
            super(message);
        }

        /**
         * Creates a new exception with the given descriptive message and root cause.
         *
         * @param message human-readable description of the failure
         * @param cause   the underlying cause of the failure
         */
        public GeminiServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}