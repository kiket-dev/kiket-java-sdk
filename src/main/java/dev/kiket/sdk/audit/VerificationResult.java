package dev.kiket.sdk.audit;

import com.google.gson.annotations.SerializedName;

/**
 * Result of a blockchain verification.
 */
public class VerificationResult {
    private boolean verified;

    @SerializedName("proof_valid")
    private boolean proofValid;

    @SerializedName("blockchain_verified")
    private boolean blockchainVerified;

    @SerializedName("content_hash")
    private String contentHash;

    @SerializedName("merkle_root")
    private String merkleRoot;

    @SerializedName("leaf_index")
    private int leafIndex;

    @SerializedName("block_number")
    private Long blockNumber;

    @SerializedName("block_timestamp")
    private String blockTimestamp;

    private String network;

    @SerializedName("explorer_url")
    private String explorerUrl;

    private String error;

    // Getters
    public boolean isVerified() { return verified; }
    public boolean isProofValid() { return proofValid; }
    public boolean isBlockchainVerified() { return blockchainVerified; }
    public String getContentHash() { return contentHash; }
    public String getMerkleRoot() { return merkleRoot; }
    public int getLeafIndex() { return leafIndex; }
    public Long getBlockNumber() { return blockNumber; }
    public String getBlockTimestamp() { return blockTimestamp; }
    public String getNetwork() { return network; }
    public String getExplorerUrl() { return explorerUrl; }
    public String getError() { return error; }
}
