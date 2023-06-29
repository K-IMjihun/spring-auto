package com.sparta.springauth;

import com.sparta.springauth.food.Food;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BeanTest {

   @Autowired
   @Qualifier("pizza")
   Food food;  // 같은 타입의 bean 객체가 하나 이상이라 등록 불가.

    @Autowired
    Food pizza;

    @Autowired
    Food chicken;

    @Test
    @DisplayName("테스트")
    void test1(){
        pizza.eat();
        chicken.eat();
        food.eat();
    }
}
