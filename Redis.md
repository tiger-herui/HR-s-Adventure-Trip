​	Redis是一个基于BSD开源的项目，是一个把结构化的数据放在内存中的一个存储系统，你可以把它作为数据库，缓存和消息中间件来使用。

​	同时支持strings，lists，hashes，sets，sorted sets，bitmaps，hyperloglogs和geospatial indexes等数据类型。

​	它还内建了复制，lua脚本、LRU、事务等功能，通过redis sentinel实现高可用，通过redis cluster实现了自动分片，以及事发布、订阅、自动故障转移等等。

## NPC

N：Network Delay，网络延迟
P：Process Pause，进程暂停（GC）
C：Clock Drift，时钟漂移

# 分布式锁

对应概念：单机锁——多线程

：解决多进程同时操作一个共享资源的问题



## SETNX:SET if Not eXists-互斥

实现分布式锁，Redis首先要有互斥能力

```shell
SETNX lock 1
DEL lock
```

## EXPIRE

### 问题1-死锁

1. 程序处理业务逻辑异常，没及时释放锁
2. 进程挂了，没机会释放锁

```she
SETNX lock 1
EXPIRE lock 10  //10s后自动过期
```

### 问题2-原子性

1. SETNX 执行成功，执行 EXPIRE 时由于网络问题，执行失败
2. SETNX 执行成功，Redis 异常宕机，EXPIRE 没有机会执行		
3. SETNX 执行成功，客户端异常崩溃，EXPIRE 也没有机会执行

```shell
//保证原子性
SET lock A EX 10 NX
```

### 问题3-锁对应

1. 客户端 1 加锁成功，开始操作共享资源
2. 客户端 1 操作共享资源的时间，「超过」了锁的过期时间，锁被「自动释放」
3. 客户端 2 加锁成功，开始操作共享资源
4. 客户端 1 操作共享资源完成，释放锁（但释放的是客户端 2 的锁）

```shell
//锁是自己的才释放(需要原子执行)
if redis.get("lock") == $uuid:
	redis.del("lock")
```

### 问题4-Lua脚本

（上文代码中...）

1. 客户端 1 执行 GET，判断锁是自己的
2. 客户端 2 执行了 SET 命令，强制获取到锁
3. 客户端 1 执行 DEL，却释放了客户端 2 的锁

<img src="https://i.loli.net/2021/10/12/jeIzxVmlryScaiC.jpg" alt="jeIzxVmlryScaiC" style="zoom:67%;" />





## Redisson-过期时间设置

：加锁时，先设置一定过期时间，然后开启线程守护，定时检测这个锁的失效时间，如果锁快要过期，但操作资源还没有完成就自动续期。

<img src="https://i.loli.net/2021/10/12/YK5odSMZNC1ijxR.jpg" alt="YK5odSMZNC1ijxR" style="zoom:50%;" />



## Redlock-主从切换的安全机制

1. 客户端 1 在主库上执行 SET 命令，加锁成功；
2. 此时，主库异常宕机，SET 命令还未同步到从库上（主从复制是异步的）；
3. 从库被哨兵提升为新主库，这个锁在新的主库上，丢失了。

<img src="https://i.loli.net/2021/10/12/qduy35wSJmBA1G8.jpg" alt="qduy35wSJmBA1G8" style="zoom: 40%;" />

Redlock 的方案基于 2 个前提:

1. 不再需要部署**从库**和**哨兵**实例，**只部署**主库
2. 但主库要部署多个，官方推荐至少 5 个实例（都是一个个孤立的实例）

<img src="https://i.loli.net/2021/10/12/c5KL7oB6jeQgOuN.jpg" alt="c5KL7oB6jeQgOuN" style="zoom:75%;" />



1. 客户端先获取「当前时间戳T1」
2. 客户端依次向这 5 个 Redis 实例发起加锁请求（ SET 命令），且每个请求会设置超时时间（毫秒级，要远小于锁的有效时间），如果某一个实例加锁失败（包括网络超时、锁被其它人持有等各种异常情况），就立即向下一个 Redis 实例申请加锁
3. 如果客户端从 >=3 个（大多数）以上 Redis 实例加锁成功，则再次获取「当前时间戳T2」，如果 T2 - T1 < 锁过期时间，此时，认为客户端加锁成功，否则认为加锁失败
4. 加锁成功，去操作共享资源（例如修改 MySQL 某一行或发起一个 API 请求）
5. 加锁失败，向「全部节点」发起释放锁请求（Lua 脚本释放锁）



# 缓存

：API服务器缓存

<img src="https://i.loli.net/2021/10/17/K5pa8wDnMHoTEcN.png" alt="K5pa8wDnMHoTEcN" style="zoom:67%;" />

### Persistence-持久化

：redis会把内存的中的数据写入到硬盘中，在redis重新启动的时候加载这些数据，从而最大限度的降低缓存丢失带来的影响。

：单独创建fork()一个子进程，将当前父进程的数据库数据复制到子进程的内存中，然后由子进程写入到临时文件中，持久化的过程结束了，再用这个临时文件替换上次的快照文件，然后子进程退出，内存释放。

#### RDB

redis默认的持久化方式，按照一定的时间周期策略把内存的数据以快照的形式保存到硬盘的二进制文件。即Snapshot快照存储，对应产生的数据文件为dump.rdb，通过配置文件中的save参数来定义快照的周期

#### AOF

Redis会将每一个收到的写命令都通过Write函数追加到文件最后，类似于MySQL的binlog。当Redis重启是会通过重新执行文件中保存的写命令来在内存中重建整个数据库的内容

### Sentinel-哨兵

：Sentinel可以管理多个Redis服务器，它提供了监控，提醒以及自动的故障转移的功能；Replication则是负责让一个Redis服务器可以配备多个备份的服务器。Redis也是利用这两个功能来保证Redis的高可用的。此外，Sentinel功能则是对Redis的发布和订阅功能的一个利用。



### 定期删除+惰性删除+内存淘汰策略

1）noeviction：当内存不足以容纳新写入数据时，新写入操作会报错。应该没人用吧。

2）allkeys-lru：当内存不足以容纳新写入数据时，在键空间中，移除最近最少使用的key。推荐使用，目前项目在用这种。

3）allkeys-random：当内存不足以容纳新写入数据时，在键空间中，随机移除某个key。应该也没人用吧，你不删最少使用Key,去随机删。

4）volatile-lru：当内存不足以容纳新写入数据时，在设置了过期时间的键空间中，移除最近最少使用的key。这种情况一般是把redis既当缓存，又做持久化存储的时候才用。不推荐

5）volatile-random：当内存不足以容纳新写入数据时，在设置了过期时间的键空间中，随机移除某个key。依然不推荐

6）volatile-ttl：当内存不足以容纳新写入数据时，在设置了过期时间的键空间中，有更早过期时间的key优先移除。不推荐

### 缓存雪崩（多条）

：大量redis key在同一时间失效，导致大量请求访问数据库，数据库服务器宕机，线上服务大面积报错。

（1）redis高可用 

（2）加锁排队，限流降级 

（3）缓存失效时间均匀分布

### 缓存穿透

：指缓存和数据库中都没有的数据，导致所有的请求都落到数据库上，造成数据库短时间内承受大量请求而崩掉。

（1）接口层增加校验 

（2）采用布隆过滤器：将所有可能存在的数据哈希到一个足够大的bitmap中，一个一定不存在的数据会被这个bitmap拦截掉，从而避免了对底层存储系统的查询压力。如果一个查询返回的数据为空（不管是数据不存在，还是系统故障），我们仍然把这个空结果进行缓存，但它的过期时间会很短，最长不超过五分钟。通过这个直接设置的默认值存放到缓存，这样第二次到缓冲中获取就有值了，而不会继续访问数据库，这种办法最简单粗暴

### 缓存击穿（同一条的并发）

1. 加锁
2. 数据库限流

## Redis 三种集群

### 主从复制

1. 从服务器连接主服务器，发送SYNC命令；
2. 主服务器接收到SYNC命名后，开始执行BGSAVE命令生成RDB文件并使用缓冲区记录此后执行的所有写命令；
3. 主服务器BGSAVE执行完后，向所有从服务器发送快照文件，并在发送期间继续记录被执行的写命令；
4. 从服务器收到快照文件后丢弃所有旧数据，载入收到的快照； 主服务器快照发送完毕后开始向从服务器发送缓冲区中的写命令；
5. 从服务器完成对快照的载入，开始接收命令请求，并执行来自主服务器缓冲区的写命令；（从服务器初始化完成）
6. 主服务器每执行一个写命令就会向从服务器发送相同的写命令，从服务器接收并执行收到的写命令（从服务器初始化完成后的操作）

#### 优点

1. 主机会自动将数据同步到从机，可以进行读写分离；
2. 为了分载Master的读操作压力，Slave服务器可以为客户端提供只读操作的服务，写服务仍然必须由Master来完成；
3. Slave同样可以接受其它Slaves的连接和同步请求，这样可以有效的分载Master的同步压力；
4. Master Server是以非阻塞的方式为Slaves提供服务，所以在Master-Slave同步期间，客户端仍然可以提交查询或修改请求；
5. Slave Server同样是以非阻塞的方式完成数据同步，在同步期间，如果有客户端提交查询请求，Redis则返回同步之前的数据；

#### 缺点

1. Redis不具备自动容错和恢复功能，主机从机的宕机都会导致前端部分读写请求失败，需要等待机器重启或者手动切换前端的IP才能恢复；
2. 主机宕机，宕机前有部分数据未能及时同步到从机，切换IP后还会引入数据不一致的问题，降低了系统的可用性；
3. Redis较难支持在线扩容，在集群容量达到上限时在线扩容会变得很复杂。

### 哨兵模式

：当主服务器中断服务后，可以将一个从服务器升级为主服务器，以便继续提供服务，Redis 2.8中提供了哨兵工具来实现自动化的系统监控和故障恢复功能。


1. 每个Sentinel（哨兵）进程以每秒钟一次的频率向整个集群中的Master主服务器，Slave从服务器以及其他Sentinel（哨兵）进程发送一个 PING 命令。
2. 如果一个实例（instance）距离最后一次有效回复 PING 命令的时间超过 down-after-milliseconds 选项所指定的值， 则这个实例会被 Sentinel（哨兵）进程标记为主观下线（SDOWN）
3. 如果一个Master主服务器被标记为主观下线（SDOWN），则正在监视这个Master主服务器的所有 Sentinel（哨兵）进程要以每秒一次的频率确认Master主服务器的确进入了主观下线状态
4. 当有足够数量的 Sentinel（哨兵）进程（大于等于配置文件指定的值）在指定的时间范围内确认Master主服务器进入了主观下线状态（SDOWN）， 则Master主服务器会被标记为客观下线（ODOWN）
5. 在一般情况下， 每个 Sentinel（哨兵）进程会以每 10 秒一次的频率向集群中的所有Master主服务器、Slave从服务器发送 INFO 命令。
6. 当Master主服务器被 Sentinel（哨兵）进程标记为客观下线（ODOWN）时，Sentinel（哨兵）进程向下线的 Master主服务器的所有 Slave从服务器发送 INFO 命令的频率会从 10 秒一次改为每秒一次。
7. 若没有足够数量的 Sentinel（哨兵）进程同意 Master主服务器下线， Master主服务器的客观下线状态就会被移除。若 Master主服务器重新向 Sentinel（哨兵）进程发送 PING 命令返回有效回复，Master主服务器的主观下线状态就会被移除。
   

#### 优点

哨兵模式是基于主从模式的，所有主从的优点，哨兵模式都具有。

#### 缺点

Redis较难支持在线扩容，在集群容量达到上限时在线扩容会变得很复杂。

### Redis-Cluster集群

：redis的哨兵模式基本已经可以实现高可用，读写分离 ，但是在这种模式下每台redis服务器都存储相同的数据，很浪费内存，所以在redis3.0上加入了cluster模式，实现的redis的分布式存储，也就是说每台redis节点上存储不同的内容。


1. 所有的redis节点彼此互联(PING-PONG机制)，内部使用二进制协议优化传输速度和带宽；
2. 节点的fail是通过集群中超过半数的节点检测失效时才生效；
3. 客户端与redis节点直连，不需要中间代理层。客户端不需要连接集群所有节点,连接集群中任何一个可用节点即可。

​	在redis的每一个节点上，都有这么两个东西：

1. 插槽（slot），它的的取值范围是：0-16383；
2. cluster，可以理解为是一个集群管理的插件。

当我们的存取的key到达的时候，redis会根据crc16的算法得出一个结果，然后把结果对 16384 求余数，这样每个 key 都会对应一个编号在 0-16383 之间的哈希槽，通过这个值，去找到对应的插槽所对应的节点，然后直接自动跳转到这个对应的节点上进行存取操作。
为了保证高可用，redis-cluster集群引入了主从模式，一个主节点对应一个或者多个从节点，当主节点宕机的时候，就会启用从节点。当其它主节点ping一个主节点A时，如果半数以上的主节点与A通信超时，那么认为主节点A宕机了。如果主节点A和它的从节点A1都宕机了，那么该集群就无法再提供服务了。