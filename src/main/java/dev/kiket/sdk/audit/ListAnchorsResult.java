package dev.kiket.sdk.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Result of listing blockchain anchors.
 */
public class ListAnchorsResult {
    private List<BlockchainAnchor> anchors;
    private PaginationInfo pagination;

    public List<BlockchainAnchor> getAnchors() { return anchors; }
    public PaginationInfo getPagination() { return pagination; }

    public void setAnchors(List<BlockchainAnchor> anchors) { this.anchors = anchors; }
    public void setPagination(PaginationInfo pagination) { this.pagination = pagination; }

    public static class PaginationInfo {
        private int page;

        @JsonProperty("per_page")
        private int perPage;

        private int total;

        @JsonProperty("total_pages")
        private int totalPages;

        public int getPage() { return page; }
        public int getPerPage() { return perPage; }
        public int getTotal() { return total; }
        public int getTotalPages() { return totalPages; }

        public void setPage(int page) { this.page = page; }
        public void setPerPage(int perPage) { this.perPage = perPage; }
        public void setTotal(int total) { this.total = total; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
