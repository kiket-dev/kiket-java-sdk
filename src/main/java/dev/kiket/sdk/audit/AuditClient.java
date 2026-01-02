package dev.kiket.sdk.audit;

import dev.kiket.sdk.client.HttpClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Client for blockchain audit verification operations.
 */
public class AuditClient {
    private final HttpClient httpClient;
    private final Gson gson;

    public AuditClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    /**
     * List blockchain anchors for the organization.
     */
    public ListAnchorsResult listAnchors(ListAnchorsOptions options) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(options.getPage()));
        params.put("per_page", String.valueOf(options.getPerPage()));

        if (options.getStatus() != null) {
            params.put("status", options.getStatus());
        }
        if (options.getNetwork() != null) {
            params.put("network", options.getNetwork());
        }
        if (options.getFrom() != null) {
            params.put("from", options.getFrom().toString());
        }
        if (options.getTo() != null) {
            params.put("to", options.getTo().toString());
        }

        String response = httpClient.get("/api/v1/audit/anchors", params);
        return gson.fromJson(response, ListAnchorsResult.class);
    }

    /**
     * Get details of a specific anchor by merkle root.
     */
    public BlockchainAnchor getAnchor(String merkleRoot, boolean includeRecords) throws Exception {
        Map<String, String> params = new HashMap<>();
        if (includeRecords) {
            params.put("include_records", "true");
        }

        String response = httpClient.get("/api/v1/audit/anchors/" + merkleRoot, params);
        return gson.fromJson(response, BlockchainAnchor.class);
    }

    /**
     * Get the blockchain proof for a specific audit record.
     */
    public BlockchainProof getProof(long recordId) throws Exception {
        return getProof(recordId, "AuditLog");
    }

    /**
     * Get the blockchain proof for a specific audit record.
     * @param recordId The ID of the audit record
     * @param recordType Type of record ("AuditLog" or "AIAuditLog")
     */
    public BlockchainProof getProof(long recordId, String recordType) throws Exception {
        Map<String, String> params = null;
        if (!"AuditLog".equals(recordType)) {
            params = new HashMap<>();
            params.put("record_type", recordType);
        }
        String response = httpClient.get("/api/v1/audit/records/" + recordId + "/proof", params);
        return gson.fromJson(response, BlockchainProof.class);
    }

    /**
     * Verify a blockchain proof via the API.
     */
    public VerificationResult verify(BlockchainProof proof) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content_hash", proof.getContentHash());
        payload.put("merkle_root", proof.getMerkleRoot());
        payload.put("proof", proof.getProof());
        payload.put("leaf_index", proof.getLeafIndex());
        payload.put("tx_hash", proof.getTxHash());

        String response = httpClient.post("/api/v1/audit/verify", gson.toJson(payload));
        return gson.fromJson(response, VerificationResult.class);
    }

    /**
     * Compute the content hash for a record (for local verification).
     */
    public static String computeContentHash(Map<String, Object> data) throws NoSuchAlgorithmException {
        TreeMap<String, Object> sorted = new TreeMap<>(data);
        Gson gson = new Gson();
        String canonical = gson.toJson(sorted);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder("0x");
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Verify a Merkle proof locally without making an API call.
     */
    public static boolean verifyProofLocally(
            String contentHash,
            List<String> proofPath,
            int leafIndex,
            String merkleRoot) throws NoSuchAlgorithmException {

        byte[] current = normalizeHash(contentHash);
        int idx = leafIndex;

        for (String siblingHex : proofPath) {
            byte[] sibling = normalizeHash(siblingHex);
            if (idx % 2 == 0) {
                current = hashPair(current, sibling);
            } else {
                current = hashPair(sibling, current);
            }
            idx = idx / 2;
        }

        byte[] expected = normalizeHash(merkleRoot);
        return Arrays.equals(current, expected);
    }

    private static byte[] normalizeHash(String h) {
        String hex = h.startsWith("0x") ? h.substring(2) : h;
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static byte[] hashPair(byte[] left, byte[] right) throws NoSuchAlgorithmException {
        // Sort for consistent ordering
        if (compareBytes(left, right) > 0) {
            byte[] temp = left;
            left = right;
            right = temp;
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(left);
        digest.update(right);
        return digest.digest();
    }

    private static int compareBytes(byte[] a, byte[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int cmp = Byte.compareUnsigned(a[i], b[i]);
            if (cmp != 0) return cmp;
        }
        return Integer.compare(a.length, b.length);
    }
}
