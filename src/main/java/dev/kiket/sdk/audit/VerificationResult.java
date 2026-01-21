package dev.kiket.sdk.audit;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of a blockchain verification.
 */
public class VerificationResult {
    private boolean verified;

    @JsonProperty("proof_valid")
    private boolean proofValid;

    @JsonProperty("blockchain_verified")
    private boolean blockchainVerified;

    @JsonProperty("content_hash")
    private String contentHash;

    @JsonProperty("merkle_root")
    private String merkleRoot;

    @JsonProperty("leaf_index")
    private int leafIndex;

    @JsonProperty("block_number")
    private Long blockNumber;

    @JsonProperty("block_timestamp")
    private String blockTimestamp;

    private String network;

    @JsonProperty("explorer_url")
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

    // Setters
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setProofValid(boolean proofValid) { this.proofValid = proofValid; }
    public void setBlockchainVerified(boolean blockchainVerified) { this.blockchainVerified = blockchainVerified; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    public void setLeafIndex(int leafIndex) { this.leafIndex = leafIndex; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public void setBlockTimestamp(String blockTimestamp) { this.blockTimestamp = blockTimestamp; }
    public void setNetwork(String network) { this.network = network; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
    public void setError(String error) { this.error = error; }
}
