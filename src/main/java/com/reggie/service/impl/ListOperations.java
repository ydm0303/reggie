package com.reggie.service.impl;

import java.util.*;

public class ListOperations {
    public static void main(String[] args) {
        List<String> myList = new ArrayList<>();
        myList.add("A");
        myList.add("B");
        myList.add("A");
        myList.add("C");
        myList.add("A");
        myList.add("B");

        // 使用 Set 来去重
        Set<String> uniqueElements = new HashSet<>(myList);

        // 将去重后的元素存回一个新的列表
        List<String> deduplicatedList = new ArrayList<>(uniqueElements);

        // 打印去重后的列表
        System.out.println("去重后的列表: " + deduplicatedList);


        // 使用一个 Map 来统计每个元素出现的次数
        Map<String, Integer> elementCount = new HashMap<>();
        for (String element : myList) {
            elementCount.put(element, elementCount.getOrDefault(element, 0) + 1);
        }

        // 遍历列表并根据元素出现的次数执行不同的操作
        for (String element : myList) {
            if (elementCount.get(element) >= 2) {
                // 如果元素出现 2 次或以上，执行操作 A
                System.out.println("Performing operation A for element: " + element);
                // 在这里执行操作 A 的代码
            } else {
                // 否则，执行操作 B
                System.out.println("Performing operation B for element: " + element);
                // 在这里执行操作 B 的代码
            }
        }
    }
}
