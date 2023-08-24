import com.reggie.ReggieApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = ReggieApplication.class)
public class MyTest {

    @Test
    public void test1(){
        List<String> list = Arrays.asList("java", "scala", "python", "shell", "ruby");
        long count = list.parallelStream().filter(x -> x.length() < 5).count();
        System.out.println(count);
    }

    @Test
    public void test2(){
        List<String> strings = Arrays.asList(new String[]{"a", "b", "c"});
        List<String> collect = strings.stream().map(String::toUpperCase).collect(Collectors.toList());
        collect.forEach(x-> System.out.println(x + ""));
    }
}
