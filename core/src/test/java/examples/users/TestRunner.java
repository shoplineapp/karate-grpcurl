package examples.users;

import com.intuit.karate.junit5.Karate;

class TestRunner {
    
    @Karate.Test
    Karate testTest() {
        return Karate.run("test").relativeTo(getClass());
    }    

}
