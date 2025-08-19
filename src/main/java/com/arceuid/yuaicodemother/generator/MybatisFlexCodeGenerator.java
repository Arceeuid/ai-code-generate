package com.arceuid.yuaicodemother.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class MybatisFlexCodeGenerator {

    //需要生成的表名
    public static final String[] TABLE_NAMES = {
            "chat_history"
    };

    public static void main(String[] args) {
        //获取数据源
        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String, Object> dataSourceMap = dict.getByPath("spring.datasource");
        String url = (String) dataSourceMap.get("url");
        String username = (String) dataSourceMap.get("username");
        String password = (String) dataSourceMap.get("password");

        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }


    public static GlobalConfig createGlobalConfig() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包,生成到临时目录下
        globalConfig.getPackageConfig()
                .setBasePackage("com.arceuid.yuaicodemother.genecode");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                //设置逻辑删除的字段名称
                .setLogicDeleteColumn("isDelete");


        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();

        //设置生成mapperXML
        globalConfig.enableMapperXml();

        //设置生成 service
        globalConfig.enableService();

        //设置生成 service 实现类
        globalConfig.enableServiceImpl();

        //设置生成 controller
        globalConfig.enableController();

        //设置生成作者名称
        globalConfig.getJavadocConfig()
                .setAuthor("arceuid");

        //可以单独配置某个列
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setColumnName("tenant_id");
        columnConfig.setLarge(true);
        columnConfig.setVersion(true);
        globalConfig.getStrategyConfig()
                .setColumnConfig("tb_account", columnConfig);

        return globalConfig;
    }
}
