package junittesting;
import models.Movie;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import orm.OurORM;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ORMTest {

    static Movie m;
    static Movie m1;
    static List<Movie> movies;
    static List<Movie> movies1;
    static OurORM<Movie> testMovie;
    static int m_id;
    static int m1_id;
    static String s;

    @BeforeAll
    public static void setUp(){
        System.out.println("really helpful for any initialization processes necessary to occur before tests run.");
        m = new Movie();
        testMovie = new OurORM<Movie>();
    }

    @BeforeEach
    public void before() {
        System.out.println("About to start another test. Could be helpful for common initial test setups.");
    }

    public  static void setUpAddTest() throws SQLException, IllegalAccessException {
        m = new Movie("Captain-America", 5, true, 0);
        m1 = testMovie.addObj(m);
    }

    @Test
    public void testAdd() throws SQLException, IllegalAccessException {
        assertEquals(m, m1);
    }

    public static void setUpIdTest(){
        m_id = m.getId();
        m1_id = m1.getId();
    }

    @Test
    public void testIdMatch() {
        assertEquals(m_id, m1_id);
    }

    public static void setUpGetIDTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        m = new Movie(1,"Avengers", 10, true, 0);
        m1 = testMovie.GetObj(Movie.class, 1);
    }

    @Test
    public void testGetById() {
        assertEquals(m, m1);
    }

    public static void setUpGetAllTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        movies.add(new Movie("Avengers", 10, true, 0));
        movies.add(new Movie("Captain America", 12, false, 0));
        movies.add(new Movie("Spider-Man", 13, true, 0));
        movies1 = testMovie.GetAllObj(Movie.class);
    }

    @Test
    public void testGetAll() {
        assertEquals(movies, movies1);
    }

    public void setUpUpdate() throws IllegalAccessException {
        m = new Movie(1,"Avengers", 10, true, 0);
        m.setTitle("Black-Widow"); //12
        m1 = testMovie.updateObj(m, 1);

    }

    @Test
    public void testUpdate(){
        assertEquals(m, m1);
    }

    public void setUpDelete() throws IllegalAccessException {
        m = new Movie(2,"Spider-Man", 10, true, 0);
        m1 = testMovie.deleteObj(Movie.class, 2);
    }

    @Test
    public void testDelete(){
        assertEquals(m, m1);
    }


}
