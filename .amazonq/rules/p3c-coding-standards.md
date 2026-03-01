# OMS代码开发规范（基于阿里巴巴P3C规范）

本规范基于阿里巴巴Java开发手册（黄山版），结合OMS项目实际情况制定。

## 一、命名规约

### 1.1 基本规则

- 【强制】代码中的命名均不能以下划线或美元符号开始，也不能以下划线或美元符号结束
- 【强制】代码中的命名严禁使用拼音与英文混合的方式，更不允许直接使用中文
- 【强制】类名使用UpperCamelCase风格，DO/BO/DTO/VO/AO/PO/UID等除外
- 【强制】方法名、参数名、成员变量、局部变量都统一使用lowerCamelCase风格
- 【强制】常量命名全部大写，单词间用下划线隔开，力求语义表达完整清楚

```java
// 正例
public static final int MAX_STOCK_COUNT = 50;
public static final String ORDER_STATUS_PAID = "PAID";

// 反例
public static final int MAX_COUNT = 50;
```

### 1.2 特殊命名

- 【强制】抽象类命名使用Abstract或Base开头
- 【强制】异常类命名使用Exception结尾
- 【强制】测试类命名以它要测试的类的名称开始，以Test结尾
- 【推荐】Service/DAO层方法命名规约：
  - 获取单个对象：`get`前缀
  - 获取多个对象：`list`前缀
  - 获取统计值：`count`前缀
  - 插入：`save`或`insert`前缀
  - 删除：`remove`或`delete`前缀
  - 修改：`update`前缀

```java
// 正例
public OrderVO getOrderById(Long orderId);
public List<OrderVO> listOrdersByStatus(String status);
public int countOrdersByDate(Date date);
```

### 1.3 包命名

- 【强制】包名统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词
- 【强制】包名统一使用单数形式，但是类名如果有复数含义，类名可以使用复数形式

```
com.haier.oms.order.service
com.haier.oms.product.controller
```

## 二、常量定义

- 【强制】不允许任何魔法值（即未经预先定义的常量）直接出现在代码中
- 【强制】在long或Long赋值时，数值后使用大写L，不能是小写l
- 【推荐】不要使用一个常量类维护所有常量，要按常量功能进行归类，分开维护

```java
// 正例
public class OrderConstants {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
}

// 反例
if (status.equals("PAID")) { // 魔法值
    // ...
}
```

## 三、代码格式

### 3.1 缩进与空格

- 【强制】采用4个空格缩进，禁止使用tab字符
- 【强制】单行字符数限制不超过120个，超出需要换行
- 【强制】方法参数在定义和传入时，多个参数逗号后边必须加空格

```java
// 正例
method(args1, args2, args3);

// 反例
method(args1,args2,args3);
```

### 3.2 大括号

- 【强制】左大括号前不换行，左大括号后换行；右大括号前换行，右大括号后还有else等代码则不换行
- 【强制】if/for/while/switch/do等保留字与括号之间都必须加空格

```java
// 正例
if (condition) {
    // ...
} else {
    // ...
}

// 反例
if(condition){
    // ...
}else{
    // ...
}
```

## 四、OOP规约

### 4.1 访问控制

- 【强制】避免通过一个类的对象引用访问此类的静态变量或静态方法，直接用类名来访问即可
- 【强制】所有的覆写方法，必须加@Override注解
- 【强制】相同参数类型，相同业务含义，才可以使用Java的可变参数，避免使用Object
- 【强制】外部正在调用或者二方库依赖的接口，不允许修改方法签名，避免对接口调用方产生影响

### 4.2 类设计

- 【强制】构造方法里面禁止加入任何业务逻辑，如果有初始化逻辑，请放在init方法中
- 【推荐】类内方法定义的顺序依次是：公有方法或保护方法 > 私有方法 > getter/setter方法
- 【推荐】setter方法中，参数名称与类成员变量名称一致，this.成员名 = 参数名

```java
public class OrderService {
    // 公有方法
    public void createOrder(OrderParam param) {
        validateParam(param);
        // ...
    }
    
    // 私有方法
    private void validateParam(OrderParam param) {
        // ...
    }
    
    // getter/setter
    public String getOrderNo() {
        return orderNo;
    }
}
```

## 五、集合处理

### 5.1 使用规范

- 【强制】关于hashCode和equals的处理，遵循如下规则：
  - 只要覆写equals，就必须覆写hashCode
  - Set存储的对象、Map的key必须覆写这两个方法
- 【强制】ArrayList的subList结果不可强转成ArrayList，否则会抛出ClassCastException异常
- 【强制】使用Map的方法keySet()/values()/entrySet()返回集合对象时，不可以对其进行添加元素操作
- 【强制】Collections类返回的对象，如：emptyList()/singletonList()等都是immutable list，不可对其进行添加或删除元素的操作

```java
// 正例
List<String> list = new ArrayList<>();
list.add("item");

// 反例
List<String> list = Collections.emptyList();
list.add("item"); // 抛出UnsupportedOperationException
```

### 5.2 判空处理

- 【强制】判断所有集合内部的元素是否为空，使用isEmpty()方法，而不是size()==0的方式
- 【强制】在使用java.util.stream.Collectors类的toMap()方法转为Map集合时，一定要使用含有参数类型为BinaryOperator，参数名为mergeFunction的方法，否则当出现相同key值时会抛出IllegalStateException异常

```java
// 正例
if (CollectionUtils.isEmpty(list)) {
    // ...
}

// 反例
if (list.size() == 0) {
    // ...
}
```

## 六、并发处理

- 【强制】获取单例对象需要保证线程安全，其中的方法也要保证线程安全
- 【强制】创建线程或线程池时请指定有意义的线程名称，方便出错时回溯
- 【强制】线程资源必须通过线程池提供，不允许在应用中自行显式创建线程
- 【强制】线程池不允许使用Executors去创建，而是通过ThreadPoolExecutor的方式，这样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险

```java
// 正例
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5, 10, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100),
    new ThreadFactoryBuilder().setNameFormat("order-pool-%d").build(),
    new ThreadPoolExecutor.CallerRunsPolicy()
);

// 反例
ExecutorService executor = Executors.newFixedThreadPool(10);
```

## 七、控制语句

### 7.1 条件判断

- 【强制】在一个switch块内，每个case要么通过continue/break/return等来终止，要么注释说明程序将继续执行到哪一个case为止
- 【强制】当switch括号内的变量类型为String并且此变量为外部参数时，必须先进行null判断
- 【强制】在if/else/for/while/do语句中必须使用大括号
- 【推荐】表达异常的分支时，少用if-else方式，这种方式可以改写成：

```java
// 正例
if (condition) {
    // ...
    return obj;
}
// 接着写else的业务逻辑代码

// 反例
if (condition) {
    // ...
    return obj;
} else {
    // ...
}
```

### 7.2 循环处理

- 【推荐】避免采用取反逻辑运算符
- 【推荐】公开接口需要进行入参保护，尤其是批量操作的接口

```java
// 正例
if (StringUtils.isNotBlank(orderNo)) {
    // ...
}

// 反例
if (!(StringUtils.isBlank(orderNo))) {
    // ...
}
```

## 八、注释规约

### 8.1 类注释

- 【强制】类、类属性、类方法的注释必须使用Javadoc规范，使用/**内容*/格式，不得使用//xxx方式
- 【强制】所有的类都必须添加创建者和创建日期

```java
/**
 * 订单服务实现类
 *
 * @author gaomingxi@haier.com
 * @version 1.0
 * @date 2026-01-13 09:50:00
 */
public class OrderServiceImpl implements OrderService {
}
```

### 8.2 方法注释

- 【强制】所有的抽象方法（包括接口中的方法）必须要用Javadoc注释
- 【强制】所有的类都必须添加创建者和创建日期
- 【推荐】方法内部单行注释，在被注释语句上方另起一行，使用//注释

```java
/**
 * 创建订单
 *
 * @param param 订单参数
 * @return 订单ID
 */
public Long createOrder(OrderParam param) {
    // 校验参数
    validateParam(param);
    
    // 保存订单
    return saveOrder(param);
}
```

### 8.3 特殊注释

- 【参考】特殊注释标记，请注明标记人与标记时间
  - 待办事宜（TODO）：需要实现，但目前还未实现的功能
  - 错误，不能工作（FIXME）：代码有问题，需要修复

```java
// TODO gaomingxi 2026-01-13 需要实现订单超时自动取消功能
// FIXME gaomingxi 2026-01-13 此处并发情况下可能出现库存超卖
```

## 九、异常处理

- 【强制】Java类库中定义的可以通过预检查方式规避的RuntimeException异常不应该通过catch的方式来处理，如：NullPointerException，IndexOutOfBoundsException等
- 【强制】异常不要用来做流程控制，条件控制
- 【强制】catch时请分清稳定代码和非稳定代码，稳定代码指的是无论如何不会出错的代码。对于非稳定代码的catch尽可能进行区分异常类型，再做对应的异常处理
- 【强制】捕获异常是为了处理它，不要捕获了却什么都不处理而抛弃之，如果不想处理它，请将该异常抛给它的调用者
- 【强制】事务场景中，抛出异常被catch后，如果需要回滚，一定要注意手动回滚事务

```java
// 正例
try {
    // 业务代码
} catch (BusinessException e) {
    log.error("业务异常", e);
    throw e;
} catch (Exception e) {
    log.error("系统异常", e);
    throw new SystemException("系统异常", e);
}

// 反例
try {
    // 业务代码
} catch (Exception e) {
    // 什么都不做
}
```

## 十、日志规约

- 【强制】应用中不可直接使用日志系统（Log4j、Logback）中的API，而应依赖使用日志框架（SLF4J）中的API
- 【强制】所有日志文件至少保存15天，因为有些异常具备以"周"为频次发生的特点
- 【强制】日志打印时禁止直接用JSON工具将对象转换成String
- 【强制】对于trace/debug/info级别的日志输出，必须使用条件输出形式或者使用占位符的方式

```java
// 正例
log.info("订单创建成功, orderId: {}, orderNo: {}", orderId, orderNo);

// 反例
log.info("订单创建成功, orderId: " + orderId + ", orderNo: " + orderNo);
```

## 十一、MySQL数据库

### 11.1 建表规约

- 【强制】表达是与否概念的字段，必须使用is_xxx的方式命名，数据类型是unsigned tinyint（1表示是，0表示否）
- 【强制】表名、字段名必须使用小写字母或数字，禁止出现数字开头，禁止两个下划线中间只出现数字
- 【强制】表名不使用复数名词
- 【强制】主键索引名为pk_字段名；唯一索引名为uk_字段名；普通索引名则为idx_字段名
- 【强制】小数类型为decimal，禁止使用float和double
- 【强制】表必备三字段：id, create_time, update_time

### 11.2 索引规约

- 【强制】业务上具有唯一特性的字段，即使是组合字段，也必须建成唯一索引
- 【强制】超过三个表禁止join。需要join的字段，数据类型保持绝对一致；多表关联查询时，保证被关联的字段需要有索引
- 【强制】在varchar字段上建立索引时，必须指定索引长度，没必要对全字段建立索引
- 【推荐】SQL性能优化的目标：至少要达到range级别，要求是ref级别，最好是consts级别

### 11.3 SQL语句

- 【强制】不要使用count(列名)或count(常量)来替代count(*)
- 【强制】count(distinct col)计算该列除NULL之外的不重复行数
- 【强制】当某一列的值全是NULL时，count(col)的返回结果为0，但sum(col)的返回结果为NULL
- 【强制】使用ISNULL()来判断是否为NULL值
- 【强制】不得使用外键与级联，一切外键概念必须在应用层解决
- 【强制】禁止使用存储过程，存储过程难以调试和扩展，更没有移植性

```sql
-- 正例
SELECT COUNT(*) FROM t_order WHERE status = 'PAID';

-- 反例
SELECT COUNT(order_no) FROM t_order WHERE status = 'PAID';
```

## 十二、工程结构

### 12.1 应用分层

- 【推荐】图中默认上层依赖于下层，箭头关系表示可直接依赖
  - 开放API层：对外提供的接口
  - Web层：主要是对访问控制进行转发，各类基本参数校验，或者不复用的业务简单处理等
  - Service层：业务逻辑层
  - Manager层：通用业务处理层
  - DAO层：数据访问层，与底层MySQL、Oracle、Hbase等进行数据交互
  - 外部接口或第三方平台：包括其它部门RPC开放接口，基础平台，其它公司的HTTP接口

### 12.2 二方库依赖

- 【强制】定义GAV遵从以下规则：
  - GroupID格式：com.{公司/BU}.业务线.[子业务线]，最多4级
  - ArtifactID格式：产品线名-模块名
  - Version：详细规定参考下方

### 12.3 服务器规约

- 【推荐】高并发服务器建议调小TCP协议的time_wait超时时间
- 【推荐】调大服务器所支持的最大文件句柄数
- 【推荐】给JVM环境参数设置-XX:+HeapDumpOnOutOfMemoryError参数，让JVM碰到OOM场景时输出dump信息

## 十三、安全规约

- 【强制】隶属于用户个人的页面或者功能必须进行权限控制校验
- 【强制】用户敏感数据禁止直接展示，必须对展示数据进行脱敏
- 【强制】用户输入的SQL参数严格使用参数绑定或者METADATA字段值限定，防止SQL注入
- 【强制】用户请求传入的任何参数必须做有效性验证
- 【强制】禁止向HTML页面输出未经安全过滤或未正确转义的用户数据

```java
// 正例：使用预编译
String sql = "SELECT * FROM t_order WHERE order_no = ?";
PreparedStatement pstmt = connection.prepareStatement(sql);
pstmt.setString(1, orderNo);

// 反例：SQL拼接
String sql = "SELECT * FROM t_order WHERE order_no = '" + orderNo + "'";
```

---

## 参考资料

- [阿里巴巴Java开发手册（黄山版）](https://github.com/alibaba/p3c)
