# MyBatis Plus 开发规范

## 一、实体类规范

### 1.1 基础规范

- 【强制】所有实体类必须继承 `BaseEntity`
- 【强制】实体类使用 `@TableName` 注解指定表名
- 【强制】实体类字段使用驼峰命名，数据库字段使用下划线命名
- 【强制】实体类必须添加 Javadoc 注释
- 【强制】继承 `BaseEntity` 的子类禁止重复定义 BaseEntity 中已有的字段（id、creatorCode、updaterCode、creatorName、updaterName、createTime、updateTime、deleted），避免字段覆盖导致功能异常

```java
/**
 * 账单实体类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ledger")
public class Ledger extends BaseEntity {
    
    /**
     * 类型：INCOME-收入，EXPENSE-支出
     */
    private String type;
    
    /**
     * 金额
     */
    private BigDecimal amount;
}

// 反例：禁止重复定义 BaseEntity 中的字段
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ledger")
public class Ledger extends BaseEntity {
    
    private Long id;  // 错误：BaseEntity 中已有 id 字段
    
    private String type;
    private BigDecimal amount;
}
```

### 1.2 BaseEntity 公共字段

所有实体类继承 `BaseEntity` 后自动拥有以下字段：

| 字段 | 类型 | 说明 | 注解 |
|------|------|------|------|
| id | Long | 主键ID | @TableId(type = IdType.AUTO) |
| creatorCode | String | 创建人编码 | @TableField(fill = FieldFill.INSERT) |
| updaterCode | String | 更新人编码 | @TableField(fill = FieldFill.INSERT_UPDATE) |
| creatorName | String | 创建人姓名 | @TableField(fill = FieldFill.INSERT) |
| updaterName | String | 更新人姓名 | @TableField(fill = FieldFill.INSERT_UPDATE) |
| createTime | LocalDateTime | 创建时间 | @TableField |
| updateTime | LocalDateTime | 更新时间 | @TableField |
| deleted | String | 逻辑删除标识 | @TableLogic |

### 1.3 字段注解

- 【强制】主键使用 `@TableId(type = IdType.AUTO)` 自增策略
- 【强制】逻辑删除字段使用 `@TableLogic` 注解
- 【推荐】需要自动填充的字段使用 `@TableField(fill = FieldFill.XXX)` 注解
- 【推荐】不存在的字段使用 `@TableField(exist = false)` 注解

```java
// 自动填充
@TableField(fill = FieldFill.INSERT)
private String creatorCode;

// 不存在的字段
@TableField(exist = false)
private String categoryName;
```

---

## 二、Mapper 规范

### 2.1 基础规范

- 【强制】Mapper 接口必须继承 `BaseMapper<T>`
- 【强制】Mapper 接口使用 `@Mapper` 注解或在启动类使用 `@MapperScan`
- 【强制】Mapper 接口必须添加 Javadoc 注释
- 【推荐】自定义方法命名遵循规范：select/insert/update/delete 前缀

```java
/**
 * 账单 Mapper
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Mapper
public interface LedgerMapper extends BaseMapper<Ledger> {
    
    /**
     * 根据类型统计金额
     *
     * @param type 类型
     * @return 总金额
     */
    BigDecimal selectAmountByType(@Param("type") String type);
}
```

### 2.2 BaseMapper 内置方法

继承 `BaseMapper<T>` 后自动拥有以下方法：

**插入：**
- `insert(T entity)` - 插入一条记录

**删除：**
- `deleteById(Serializable id)` - 根据ID删除
- `deleteByMap(Map<String, Object> columnMap)` - 根据条件删除
- `delete(Wrapper<T> wrapper)` - 根据条件删除
- `deleteBatchIds(Collection<? extends Serializable> idList)` - 批量删除

**更新：**
- `updateById(T entity)` - 根据ID更新
- `update(T entity, Wrapper<T> updateWrapper)` - 根据条件更新

**查询：**
- `selectById(Serializable id)` - 根据ID查询
- `selectBatchIds(Collection<? extends Serializable> idList)` - 批量查询
- `selectByMap(Map<String, Object> columnMap)` - 根据条件查询
- `selectOne(Wrapper<T> queryWrapper)` - 查询一条记录
- `selectCount(Wrapper<T> queryWrapper)` - 查询总记录数
- `selectList(Wrapper<T> queryWrapper)` - 查询列表
- `selectMaps(Wrapper<T> queryWrapper)` - 查询列表（返回Map）
- `selectPage(IPage<T> page, Wrapper<T> queryWrapper)` - 分页查询

---

## 三、Service 规范

### 3.1 基础规范

- 【强制】Service 接口继承 `IService<T>`
- 【强制】Service 实现类继承 `ServiceImpl<M, T>` 并实现 Service 接口
- 【强制】Service 方法命名规范：
  - 查询单个：`getXxx`
  - 查询列表：`listXxx`
  - 查询统计：`countXxx`
  - 保存：`save` 或 `create`
  - 更新：`update` 或 `modify`
  - 删除：`remove` 或 `delete`

```java
/**
 * 账单 Service 接口
 */
public interface LedgerService extends IService<Ledger> {
    
    /**
     * 根据类型查询列表
     */
    List<Ledger> listByType(String type);
}

/**
 * 账单 Service 实现类
 */
@Service
public class LedgerServiceImpl extends ServiceImpl<LedgerMapper, Ledger> 
        implements LedgerService {
    
    @Override
    public List<Ledger> listByType(String type) {
        return lambdaQuery()
                .eq(Ledger::getType, type)
                .list();
    }
}
```

### 3.2 IService 内置方法

继承 `IService<T>` 后自动拥有以下方法：

**保存：**
- `save(T entity)` - 插入一条记录
- `saveBatch(Collection<T> entityList)` - 批量插入
- `saveOrUpdate(T entity)` - 插入或更新
- `saveOrUpdateBatch(Collection<T> entityList)` - 批量插入或更新

**删除：**
- `removeById(Serializable id)` - 根据ID删除
- `removeByMap(Map<String, Object> columnMap)` - 根据条件删除
- `remove(Wrapper<T> queryWrapper)` - 根据条件删除
- `removeByIds(Collection<? extends Serializable> idList)` - 批量删除

**更新：**
- `updateById(T entity)` - 根据ID更新
- `update(Wrapper<T> updateWrapper)` - 根据条件更新
- `update(T entity, Wrapper<T> updateWrapper)` - 根据条件更新
- `updateBatchById(Collection<T> entityList)` - 批量更新

**查询：**
- `getById(Serializable id)` - 根据ID查询
- `getOne(Wrapper<T> queryWrapper)` - 查询一条记录
- `getMap(Wrapper<T> queryWrapper)` - 查询一条记录（返回Map）
- `count()` - 查询总记录数
- `count(Wrapper<T> queryWrapper)` - 根据条件查询总记录数
- `list()` - 查询所有
- `list(Wrapper<T> queryWrapper)` - 根据条件查询列表
- `listByIds(Collection<? extends Serializable> idList)` - 批量查询
- `listByMap(Map<String, Object> columnMap)` - 根据条件查询列表
- `listMaps(Wrapper<T> queryWrapper)` - 查询列表（返回Map）
- `page(IPage<T> page)` - 分页查询
- `page(IPage<T> page, Wrapper<T> queryWrapper)` - 根据条件分页查询

---

## 四、条件构造器规范

### 4.1 LambdaQueryWrapper（推荐）

- 【推荐】使用 Lambda 表达式构造查询条件，避免字段名硬编码
- 【推荐】使用 `lambdaQuery()` 方法快速构建查询

```java
// 推荐：使用 Lambda
List<Ledger> list = lambdaQuery()
        .eq(Ledger::getType, "INCOME")
        .ge(Ledger::getAmount, 100)
        .orderByDesc(Ledger::getCreateTime)
        .list();

// 不推荐：使用字符串
List<Ledger> list = list(new QueryWrapper<Ledger>()
        .eq("type", "INCOME")
        .ge("amount", 100)
        .orderByDesc("create_time"));
```

### 4.2 常用条件方法

**比较：**
- `eq` - 等于 =
- `ne` - 不等于 <>
- `gt` - 大于 >
- `ge` - 大于等于 >=
- `lt` - 小于 <
- `le` - 小于等于 <=
- `between` - BETWEEN 值1 AND 值2
- `notBetween` - NOT BETWEEN 值1 AND 值2

**模糊查询：**
- `like` - LIKE '%值%'
- `notLike` - NOT LIKE '%值%'
- `likeLeft` - LIKE '%值'
- `likeRight` - LIKE '值%'

**空值判断：**
- `isNull` - IS NULL
- `isNotNull` - IS NOT NULL

**集合：**
- `in` - IN (值1, 值2, ...)
- `notIn` - NOT IN (值1, 值2, ...)

**排序：**
- `orderBy` - ORDER BY 字段
- `orderByAsc` - ORDER BY 字段 ASC
- `orderByDesc` - ORDER BY 字段 DESC

**分组：**
- `groupBy` - GROUP BY 字段
- `having` - HAVING 条件

---

## 五、分页查询规范

### 5.1 配置分页插件

```java
@Configuration
public class MybatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### 5.2 使用分页

```java
// Service 层
public IPage<Ledger> page(Integer current, Integer size, String type) {
    Page<Ledger> page = new Page<>(current, size);
    return lambdaQuery()
            .eq(StringUtils.isNotBlank(type), Ledger::getType, type)
            .orderByDesc(Ledger::getCreateTime)
            .page(page);
}

// Controller 层
@PostMapping("/page")
public Response<IPage<LedgerVO>> page(@RequestBody LedgerQueryDTO dto) {
    IPage<Ledger> page = ledgerService.page(dto.getCurrent(), dto.getSize(), dto.getType());
    return Response.success(page);
}
```

---

## 六、逻辑删除规范

### 6.1 配置

```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted  # 全局逻辑删除字段名
      logic-delete-value: 1        # 逻辑已删除值
      logic-not-delete-value: 0    # 逻辑未删除值
```

### 6.2 使用

- 【强制】实体类逻辑删除字段使用 `@TableLogic` 注解
- 【强制】删除操作使用 `removeById` 等方法，自动转换为逻辑删除
- 【强制】查询操作自动过滤已删除数据

```java
// 逻辑删除（UPDATE t_ledger SET deleted = 1 WHERE id = ?）
ledgerService.removeById(1L);

// 查询自动过滤已删除数据（WHERE deleted = 0）
List<Ledger> list = ledgerService.list();
```

---

## 七、自动填充规范

### 7.1 配置 MetaObjectHandler

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "creatorCode", String.class, "system");
        this.strictInsertFill(metaObject, "creatorName", String.class, "系统");
        this.strictInsertFill(metaObject, "updaterCode", String.class, "system");
        this.strictInsertFill(metaObject, "updaterName", String.class, "系统");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updaterCode", String.class, "system");
        this.strictUpdateFill(metaObject, "updaterName", String.class, "系统");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 7.2 使用

- 【强制】需要自动填充的字段使用 `@TableField(fill = FieldFill.XXX)` 注解
- 【推荐】创建时填充：`FieldFill.INSERT`
- 【推荐】更新时填充：`FieldFill.UPDATE`
- 【推荐】创建和更新都填充：`FieldFill.INSERT_UPDATE`

---

## 八、批量操作规范

### 8.1 基础规范

- 【强制】批量操作必须限制单次处理的记录数，推荐每批次 500-1000 条
- 【强制】对于长集合，必须分组处理，避免一次性提交大量数据
- 【推荐】使用 MyBatis Plus 提供的 `saveBatch`、`updateBatchById` 等方法

### 8.2 分组处理示例

```java
// 正例：分组批量插入
public void batchInsert(List<Ledger> list) {
    if (CollectionUtils.isEmpty(list)) {
        return;
    }
    
    // 每批次处理 1000 条
    int batchSize = 1000;
    int total = list.size();
    
    for (int i = 0; i < total; i += batchSize) {
        int end = Math.min(i + batchSize, total);
        List<Ledger> subList = list.subList(i, end);
        saveBatch(subList);
    }
}

// 正例：使用 Stream 分组
public void batchInsert(List<Ledger> list) {
    if (CollectionUtils.isEmpty(list)) {
        return;
    }
    
    // 使用 Guava 或自定义工具类分组
    Lists.partition(list, 1000).forEach(this::saveBatch);
}

// 反例：一次性处理所有数据
public void batchInsert(List<Ledger> list) {
    // 错误：如果 list 有 10000 条数据，会导致性能问题
    saveBatch(list);
}
```

### 8.3 注意事项

- 批量操作前应先检查集合是否为空
- 批量大小根据实际情况调整，一般 500-1000 条较为合适
- 分组处理可以避免 SQL 语句过长、内存溢出等问题
- 建议使用工具类（如 Guava 的 `Lists.partition`）进行分组

---

## 九、注意事项

1. 【强制】继承 `BaseEntity` 的实体类禁止重复定义父类中的字段，避免字段覆盖
2. 【强制】批量操作必须限制单次记录数，对长集合进行分组处理
3. 【强制】禁止在 Mapper XML 中使用 `SELECT *`，必须明确指定字段
4. 【强制】禁止使用物理删除，必须使用逻辑删除
5. 【强制】复杂 SQL 必须在 Mapper XML 中编写，不要在代码中拼接
6. 【推荐】优先使用 MyBatis Plus 提供的方法，减少自定义 SQL
7. 【推荐】使用 Lambda 条件构造器，避免字段名硬编码
8. 【推荐】分页查询必须指定排序字段，避免数据重复或遗漏
