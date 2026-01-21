package dev.kiket.sdk.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kiket.sdk.client.KiketClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Client for blockchain audit verification operations.
 */
public class AuditClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KiketClient client;

    public AuditClient(KiketClient client) {
        this.client = client;
    }

    /**
     * List blockchain anchors for the organization.
     */
    public ListAnchorsResult listAnchors(ListAnchorsOptions options) {
        String url = buildListAnchorsUrl(options);
        return client.get(url, ListAnchorsResult.class).block();
    }

    /**
     * Get details of a specific anchor by merkle root.
     */
    public BlockchainAnchor getAnchor(String merkleRoot, boolean includeRecords) {
        StringBuilder url = new StringBuilder("/api/v1/audit/anchors/")
            .append(encode(merkleRoot));

        if (includeRecords) {
            url.append("?include_records=true");
        }

        return client.get(url.toString(), BlockchainAnchor.class).block();
    }

    /**
     * Get the blockchain proof for a specific audit record.
     */
    public BlockchainProof getProof(long recordId) {
        return getProof(recordId, "AuditLog");
    }

    /**
     * Get the blockchain proof for a specific audit record.
     * @param recordId The ID of the audit record
     * @param recordType Type of record ("AuditLog" or "AIAuditLog")
     */
    public BlockchainProof getProof(long recordId, String recordType) {
        StringBuilder url = new StringBuilder("/api/v1/audit/records/")
            .append(recordId)
            .append("/proof");

        if (!"AuditLog".equals(recordType)) {
            url.append("?record_type=").append(encode(recordType));
        }

        return client.get(url.toString(), BlockchainProof.class).block();
    }

    /**
     * Verify a blockchain proof via the API.
     */
    public VerificationResult verify(BlockchainProof proof) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content_hash", proof.getContentHash());
        payload.put("merkle_root", proof.getMerkleRoot());
        payload.put("proof", proof.getProof());
        payload.put("leaf_index", proof.getLeafIndex());
        payload.put("tx_hash", proof.getTxHash());

        return client.post("/api/v1/audit/verify", payload, VerificationResult.class).block();
    }

    /**
     * Compute the content hash for a record (for local verification).
     */
    public static String computeContentHash(Map<String, Object> data) throws NoSuchAlgorithmException, JsonProcessingException {
        TreeMap<String, Object> sorted = new TreeMap<>(data);
        String canonical = MAPPER.writeValueAsString(sorted);

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

    private String buildListAnchorsUrl(ListAnchorsOptions options) {
        List<String> query = new ArrayList<>();
        query.add("page=" + options.getPage());
        query.add("per_page=" + options.getPerPage());

        if (options.getStatus() != null) {
            query.add("status=" + encode(options.getStatus()));
        }
        if (options.getNetwork() != null) {
            query.add("network=" + encode(options.getNetwork()));
        }
        if (options.getFrom() != null) {
            query.add("from=" + encode(options.getFrom().toString()));
        }
        if (options.getTo() != null) {
            query.add("to=" + encode(options.getTo().toString()));
        }

        return "/api/v1/audit/anchors?" + String.join("&", query);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
