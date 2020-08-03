package de.braincooler.gwhelper;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class GwHelperApplicationTest {

    @Test
    void test() {
        List<String> list = Collections.EMPTY_LIST;

        List<String> collect = list.stream().collect(Collectors.toList());

        assertTrue(collect.isEmpty());
    }
}