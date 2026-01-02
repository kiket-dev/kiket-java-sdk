package dev.kiket.sdk.audit;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a blockchain anchor containing a batch of audit records.
 */
public class BlockchainAnchor {
    private long id;

    @SerializedName("merkle_root")
    private String merkleRoot;

    @SerializedName("leaf_count")
    private int leafCount;

    @SerializedName("first_record_at")
    private String firstRecordAt;

    @SerializedName("last_record_at")
    private String lastRecordAt;

    private String network;
    private String status;

    @SerializedName("tx_hash")
    private String txHash;

    @SerializedName("block_number")
    private Long blockNumber;

    @SerializedName("block_timestamp")
    private String blockTimestamp;

    @SerializedName("confirmed_at")
    private String confirmedAt;

    @SerializedName("explorer_url")
    private String explorerUrl;

    @SerializedName("created_at")
    private String createdAt;

    private List<AnchorRecord> records;

    // Getters
    public long getId() { return id; }
    public String getMerkleRoot() { return merkleRoot; }
    public int getLeafCount() { return leafCount; }
    public String getFirstRecordAt() { return firstRecordAt; }
    public String getLastRecordAt() { return lastRecordAt; }
    public String getNetwork() { return network; }
    public String getStatus() { return status; }
    public String getTxHash() { return txHash; }
    public Long getBlockNumber() { return blockNumber; }
    public String getBlockTimestamp() { return blockTimestamp; }
    public String getConfirmedAt() { return confirmedAt; }
    public String getExplorerUrl() { return explorerUrl; }
    public String getCreatedAt() { return createdAt; }
    public List<AnchorRecord> getRecords() { return records; }

    /**
     * Represents a record within an anchor.
     */
    public static class AnchorRecord {
        private long id;
        private String type;

        @SerializedName("leaf_index")
        private int leafIndex;

        @SerializedName("content_hash")
        private String contentHash;

        public long getId() { return id; }
        public String getType() { return type; }
        public int getLeafIndex() { return leafIndex; }
        public String getContentHash() { return contentHash; }
    }
}
