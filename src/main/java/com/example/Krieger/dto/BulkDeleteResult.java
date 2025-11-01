package com.example.Krieger.dto;

import java.util.List;

public class BulkDeleteResult {
    private int requested;
    private int enqueued;
    private List<Long> missing;

    public BulkDeleteResult() { }

    public BulkDeleteResult(int requested, int enqueued, List<Long> missing) {
        this.requested = requested;
        this.enqueued = enqueued;
        this.missing = missing;
    }

    public int getRequested() { return requested; }
    public int getEnqueued() { return enqueued; }
    public List<Long> getMissing() { return missing; }

    public void setRequested(int requested) { this.requested = requested; }
    public void setEnqueued(int enqueued) { this.enqueued = enqueued; }
    public void setMissing(List<Long> missing) { this.missing = missing; }
}
