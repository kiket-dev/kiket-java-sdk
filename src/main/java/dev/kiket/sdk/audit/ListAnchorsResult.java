package dev.kiket.sdk.audit;

import java.util.List;

/**
 * Result of listing blockchain anchors.
 */
public class ListAnchorsResult {
    private List<BlockchainAnchor> anchors;
    private PaginationInfo pagination;

    public List<BlockchainAnchor> getAnchors() { return anchors; }
    public PaginationInfo getPagination() { return pagination; }

    public static class PaginationInfo {
        private int page;
        private int per_page;
        private int total;
        private int total_pages;

        public int getPage() { return page; }
        public int getPerPage() { return per_page; }
        public int getTotal() { return total; }
        public int getTotalPages() { return total_pages; }
    }
}
