# Hashtable

对数据操作的时候都会上锁，所以效率比较低下。

> **快速失败（fail—fast）**是java集合中的一种机制， 在用迭代器遍历一个集合对象时，如果遍历过程中对集合对象的内容进行了修改（增加、删除、修改），则会抛出Concurrent Modification Exception。

Hashtable使用的是**安全失败机制（fail-safe）**，这种机制会使此次读到的数据不一定是最新的数据。

如果使用null值，就会使得其无法判断对应的key是不存在还是为空，因为无法再调用一次contain(key来对key是否存在进行判断，ConcurrentHashMap同理。

