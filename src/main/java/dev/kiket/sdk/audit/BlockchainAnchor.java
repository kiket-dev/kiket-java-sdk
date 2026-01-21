package dev.kiket.sdk.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a blockchain anchor containing a batch of audit records.
 */
public class BlockchainAnchor {
    private long id;

    @JsonProperty("merkle_root")
    private String merkleRoot;

    @JsonProperty("leaf_count")
    private int leafCount;

    @JsonProperty("first_record_at")
    private String firstRecordAt;

    @JsonProperty("last_record_at")
    private String lastRecordAt;

    private String network;
    private String status;

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("block_number")
    private Long blockNumber;

    @JsonProperty("block_timestamp")
    private String blockTimestamp;

    @JsonProperty("confirmed_at")
    private String confirmedAt;

    @JsonProperty("explorer_url")
    private String explorerUrl;

    @JsonProperty("created_at")
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

    // Setters
    public void setId(long id) { this.id = id; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    public void setLeafCount(int leafCount) { this.leafCount = leafCount; }
    public void setFirstRecordAt(String firstRecordAt) { this.firstRecordAt = firstRecordAt; }
    public void setLastRecordAt(String lastRecordAt) { this.lastRecordAt = lastRecordAt; }
    public void setNetwork(String network) { this.network = network; }
    public void setStatus(String status) { this.status = status; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public void setBlockTimestamp(String blockTimestamp) { this.blockTimestamp = blockTimestamp; }
    public void setConfirmedAt(String confirmedAt) { this.confirmedAt = confirmedAt; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setRecords(List<AnchorRecord> records) { this.records = records; }

    /**
     * Represents a record within an anchor.
     */
    public static class AnchorRecord {
        private long id;
        private String type;

        @JsonProperty("leaf_index")
        private int leafIndex;

        @JsonProperty("content_hash")
        private String contentHash;

        public long getId() { return id; }
        public String getType() { return type; }
        public int getLeafIndex() { return leafIndex; }
        public String getContentHash() { return contentHash; }

        public void setId(long id) { this.id = id; }
        public void setType(String type) { this.type = type; }
        public void setLeafIndex(int leafIndex) { this.leafIndex = leafIndex; }
        public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    }
}
