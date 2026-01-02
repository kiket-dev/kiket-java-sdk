package dev.kiket.sdk.audit;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a Merkle proof for an audit record.
 */
public class BlockchainProof {
    @SerializedName("record_id")
    private long recordId;

    @SerializedName("record_type")
    private String recordType;

    @SerializedName("content_hash")
    private String contentHash;

    @SerializedName("anchor_id")
    private long anchorId;

    @SerializedName("merkle_root")
    private String merkleRoot;

    @SerializedName("leaf_index")
    private int leafIndex;

    @SerializedName("leaf_count")
    private int leafCount;

    private List<String> proof;
    private String network;

    @SerializedName("tx_hash")
    private String txHash;

    @SerializedName("block_number")
    private Long blockNumber;

    @SerializedName("block_timestamp")
    private String blockTimestamp;

    private boolean verified;

    @SerializedName("verification_url")
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
}
