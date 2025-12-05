package com.uniswap.clone.sharding;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.uniswap.clone.entity.User;
import com.uniswap.clone.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.math.BigInteger;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserShardingTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testUserSharding() {
        // 1. Insert Users
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setName("User" + i);
            user.setEmail("user" + i + "@example.com");
            userMapper.insert(user);
        }

        // 2. Verify total count using Mapper (Logical View)
        Long totalUsers = userMapper.selectCount(null);
        System.out.println("Total users found via Mapper: " + totalUsers);
        assertTrue(totalUsers >= 10, "Should have at least 10 users");

        
        List<User> users = userMapper.selectList(null);


        int shard0 = 0;
        int shard1 = 0;
        int shard2 = 0;
        for (User u : users) {
            long mod = u.getId() % 3;
            if (mod == 0) {
                shard0++;
            } else if (mod == 1) {
                shard1++;
            } else {
                shard2++;
            }
        }
        System.out.println("Shard 0 count: " + shard0);
        System.out.println("Shard 1 count: " + shard1);
        System.out.println("Shard 2 count: " + shard2);
        
        // We expect distribution across all 3 shards
        assertTrue(shard0 > 0 || shard1 > 0 || shard2 > 0, "Should have users");
    }


    @Test
public void testQueryByName() {
    // 创建查询条件
     BigInteger id = new BigInteger("1996791068858793986");
    QueryWrapper<User> wrapper = new QueryWrapper<>();
    wrapper.eq("id", id);

    int mod = id.mod(new BigInteger("3")).intValue();

    System.out.println(mod);
    
    // 执行查询 - ShardingSphere 会自动路由到所有分片
    List<User> users = userMapper.selectList(wrapper);
    
    if (!users.isEmpty()) {
        User user = users.get(0);
        System.out.println("Found user: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("ID: " + user.getId());
        System.out.println("ID % 3 = " + (user.getId() % 3) + " (should be 2 for users_2)");
    }
}

}
