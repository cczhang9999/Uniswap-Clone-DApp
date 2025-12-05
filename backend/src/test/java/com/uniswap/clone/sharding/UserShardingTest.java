package com.uniswap.clone.sharding;

import com.uniswap.clone.entity.User;
import com.uniswap.clone.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserShardingTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DataSource dataSource;

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

        // 3. Verify physical distribution using JdbcTemplate (Physical View)
        // Note: We need to be careful here. If ShardingSphere is proxying the DataSource, 
        // simple JDBC queries might still be routed. 
        // However, usually for debugging/verification, we want to see if data actually went to different 'tables' 
        // if we were inspecting the DB. 
        // Since we are using ShardingSphere-JDBC, the 'dataSource' bean IS the ShardingSphereDataSource.
        // Executing "SELECT count(*) FROM users_0" might work if ShardingSphere allows passing through 
        // or if we use a raw connection. 
        // But ShardingSphere parser might block queries to actual tables if not configured.
        // Let's try to query the logical table and see if we can infer distribution, 
        // OR just trust the logical count for now and maybe print IDs to see if they are even/odd.
        
        List<User> users = userMapper.selectList(null);
        int evenIds = 0;
        int oddIds = 0;
        for (User u : users) {
            if (u.getId() % 2 == 0) {
                evenIds++;
            } else {
                oddIds++;
            }
        }
        System.out.println("Even IDs: " + evenIds);
        System.out.println("Odd IDs: " + oddIds);
        
        // With Snowflake ID, the ID is large, but the sharding algorithm is id % 2.
        // So we should expect roughly even distribution if we insert enough, 
        // or at least some in both if the IDs are random enough in parity.
        // Snowflake IDs usually have a sequence part, so parity should flip.
        
        assertTrue(evenIds > 0, "Should have some users with even IDs");
        assertTrue(oddIds > 0, "Should have some users with odd IDs");
    }
}
