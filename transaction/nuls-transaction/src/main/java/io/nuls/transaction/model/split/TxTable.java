package io.nuls.transaction.model.split;

/**
 * @author: Charlie
 * @date: 2018/11/18
 */
public class TxTable {

    private String tableName;

    private String indexName;

    public TxTable(){

    }

    public TxTable(String tableName, String indexName) {
        this.tableName = tableName;
        this.indexName = indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
