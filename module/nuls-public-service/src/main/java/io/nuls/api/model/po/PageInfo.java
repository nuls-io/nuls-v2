package io.nuls.api.model.po;

import java.util.ArrayList;
import java.util.List;

public class PageInfo<T> {

    private int pageNumber;

    private int pageSize;

    private long totalCount;

    private List<T> list;

    public PageInfo() {

    }

    public PageInfo(int pageNumber, int pageSize, long totalCount, List<T> list) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.list = list;
    }

    public PageInfo(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;

        this.list = new ArrayList<>();
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
