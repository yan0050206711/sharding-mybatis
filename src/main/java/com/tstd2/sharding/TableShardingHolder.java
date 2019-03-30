package com.tstd2.sharding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分表信息持有者
 */
public class TableShardingHolder {

    /**
     * 使用private修饰，防止被new
     */
    private TableShardingHolder() {
    }

    /**
     * 分表元信息。
     * <p>
     * 这份数据配置在spring配置文件中，通过spring启动过程中 的Bean初始化回调机制来构建数据。
     */
    private static Map<String, TableShardingBean> tableShardingInfos = new HashMap<String, TableShardingBean>();

    /**
     * 初始化分表信息
     */
    public static void initTableShardingInfo(VirtualDataSource vds) {
            int shardingDBCount = vds.getShardingDBCount();
            List<ShardingTableInfo> stInfos = vds.getShardingTableInfos();
            for (ShardingTableInfo info : stInfos) {
                TableShardingBean tableShardingBean = new TableShardingBean(info.getTablePrefix(), info.getShardingTableCount(), shardingDBCount);
                tableShardingInfos.put(info.getTablePrefix(), tableShardingBean);
            }
    }

    /**
     * 获取分表信息Map
     */
    public static Map<String, TableShardingBean> getTableShardingInfos() {
        return tableShardingInfos;
    }

}
