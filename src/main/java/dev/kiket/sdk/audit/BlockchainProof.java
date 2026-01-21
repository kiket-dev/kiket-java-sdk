package dev.kiket.sdk.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a Merkle proof for an audit record.
 */
public class BlockchainProof {
    @JsonProperty("record_id")
    private long recordId;

    @JsonProperty("record_type")
    private String recordType;

    @JsonProperty("content_hash")
    private String contentHash;

    @JsonProperty("anchor_id")
    private long anchorId;

    @JsonProperty("merkle_root")
    private String merkleRoot;

    @JsonProperty("leaf_index")
    private int leafIndex;

    @JsonProperty("leaf_count")
    private int leafCount;

    private List<String> proof;
    private String network;

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("block_number")
    private Long blockNumber;

    @JsonProperty("block_timestamp")
    private String blockTimestamp;

    private boolean verified;

    @JsonProperty("verification_url")
    private String verificationUrl;

    // Getters
    public long getRecordId() { return recordId; }
    public String getRecordType() { return recordType; }
    public String getContentHash() { return contentHash; }
    public long getAnchorId() { return anchorId; }
    public String getMerkleRoot() { return merkleRoot; }
    public int getLeafIndex() { return leafIndex; }
    public int getLeafCount() { return leafCount; }
    public List<String> getProof() { return proof; }
    public String getNetwork() { return network; }
    public String getTxHash() { return txHash; }
    public Long getBlockNumber() { return blockNumber; }
    public String getBlockTimestamp() { return blockTimestamp; }
    public boolean isVerified() { return verified; }
    public String getVerificationUrl() { return verificationUrl; }

    // Setters
    public void setRecordId(long recordId) { this.recordId = recordId; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public void setAnchorId(long anchorId) { this.anchorId = anchorId; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    public void setLeafIndex(int leafIndex) { this.leafIndex = leafIndex; }
    public void setLeafCount(int leafCount) { this.leafCount = leafCount; }
    public void setProof(List<String> proof) { this.proof = proof; }
    public void setNetwork(String network) { this.network = network; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public void setBlockTimestamp(String blockTimestamp) { this.blockTimestamp = blockTimestamp; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setVerificationUrl(String verificationUrl) { this.verificationUrl = verificationUrl; }
}
