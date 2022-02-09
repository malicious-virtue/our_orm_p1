package orm;

import annotations.Column;
import annotations.PrimaryKey;
import util.JDBCConnection;
import util.ResourceNotFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OurORM<T>{

    private Connection conn = JDBCConnection.getConnection();

    /*
    ***************************************************
    *   CRUD OPERATIONS
    * These methods takes in a Generic Model Object and retrieves the Primary Key and field Name
    * and Values using Refection API. Column and Primary key Annotations are used for object mapping
    * Then a prepared statement is constructed using the fields
    ****************************************************
    */

    public T addObj(T t) throws SQLException, IllegalAccessException {

        if(t == null){
            System.out.println("Object is Null");
        }

        //getting the class we are going to add i.e. Movie
        Class<? extends Object> clazz = t.getClass();
        //getting all the declared fields from the class
        Field[] fields = clazz.getDeclaredFields();
        Field pkey = null;
        ArrayList<Field> columns = new ArrayList<Field>();
        StringJoiner sj = new StringJoiner(",");

        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                pkey = field; //id
                field.setAccessible(true);
            } else if (field.isAnnotationPresent(Column.class)) {
                sj.add(field.getName()); //field.getName() = title
                columns.add(field);
                field.setAccessible(true);
            }
        }
        //getting the size of the column
        int count = columns.size();
        //adding the length of the sj to a int of "?" then separating them by ",";
        String marks = IntStream.range(0, count).mapToObj(e -> "?").collect(Collectors.joining(","));
        String sql = "INSERT into "+ clazz.getSimpleName()+ " VALUES (default, "+marks+") RETURNING *";
        //System.out.println(sql);

        try{
            PreparedStatement ps = conn.prepareStatement(sql);
            ConstructPSQuery(t, columns, ps);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return buildObj(t,pkey, columns, rs);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public T GetObj(Class<T> clazz, int id) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        //getting all the declared fields from the class
        Field[] fields = clazz.getDeclaredFields(); //getting the class we are going to add i.e. Movie
        Field pkey = null;
        T t = clazz.getConstructor().newInstance();
        ArrayList<Field> columns = new ArrayList<Field>();
        StringJoiner sj = new StringJoiner(",");

        for (Field field : fields) { //int num: numbers

            if (field.isAnnotationPresent(PrimaryKey.class)) {
                pkey = field; //id
                field.setAccessible(true);
                //System.out.println("Primary Key/Field Name: " + field.getName() + " || Value: " + field.get(t));
            }else if(field.isAnnotationPresent(Column.class)){
                sj.add(field.getName()); //field.getName() = title
                columns.add(field);
                field.setAccessible(true);
                //System.out.println("Field Name: " + field.getName() + " || Value: "  + field.get(t));
            }

        }
        String sql = "SELECT * FROM "+ clazz.getSimpleName()+ " WHERE " + pkey.getName() + "=?";
        System.out.println(sql);
        try {
            //Set up PreparedStatement
            PreparedStatement ps = conn.prepareStatement(sql);
            //Set values for any Placeholders
            ps.setInt(1, id);
            //Execute the query, store the results -> ResultSet
            ResultSet rs = ps.executeQuery();
            //Extract results out of ResultSet
            if(rs.next()) {
                return buildObj(t, pkey, columns, rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<T> GetAllObj(Class<T> clazz) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        //getting all the declared fields from the class
        Field[] fields = clazz.getDeclaredFields(); //getting the class we are going to add i.e. Movie
        Field pkey = null;
        ArrayList<Field> columns = new ArrayList<Field>();
        StringJoiner sj = new StringJoiner(",");

        String sql = "SELECT * FROM "+ clazz.getSimpleName();
        System.out.println(sql);
        try {
            //Set up PreparedStatement
            PreparedStatement ps = conn.prepareStatement(sql);
            //Execute the query, store the results -> ResultSet
            ResultSet rs = ps.executeQuery();
            //Extract results out of ResultSet
            List<T> objlist = new ArrayList<T>();
            while (rs.next()) {
                T t = clazz.getConstructor().newInstance();
                for (Field field : fields) { //int num: numbers

                    if (field.isAnnotationPresent(PrimaryKey.class)) {
                        pkey = field; //id
                        field.setAccessible(true);
                        //System.out.println("Primary Key/Field Name: " + field.getName() + " || Value: " + field.get(t));
                    }else if(field.isAnnotationPresent(Column.class)){
                        sj.add(field.getName()); //field.getName() = title
                        columns.add(field);
                        field.setAccessible(true);
                        //System.out.println("Field Name: " + field.getName() + " || Value: "  + field.get(t));
                    }

                }

                objlist.add(buildObj(t, pkey, columns, rs));
            }
            return objlist;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public T updateObj(T t, int id) throws IllegalAccessException {
        //getting the class we are going to add i.e. Movie
        Class<? extends Object> clazz = t.getClass(); //Movie
        //getting all the declared fields from the class
        Field[] fields = clazz.getDeclaredFields();
        Field pkey = null;
        ArrayList<Field> columns = new ArrayList<Field>();
        StringJoiner sj = new StringJoiner(",");

        for (Field field : fields) {

            if (field.isAnnotationPresent(PrimaryKey.class)) {
                pkey = field; //id
                field.setAccessible(true);
                //System.out.println("Primary Key/Field Name: " + field.getName() + " || Value: " + field.get(t));
            } else if (field.isAnnotationPresent(Column.class)) {
                sj.add(field.getName()); //field.getName() = title
                columns.add(field);
                field.setAccessible(true);
                //System.out.println("Primary Key/Field Name: " + field.getName() + " || Value: " + field.get(t));
            }
        }

        String sql;
        //add one to the length so as not to include the primary key
        Iterator value = columns.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE " + clazz.getSimpleName() + " SET ");

        while(value.hasNext()){
            Field field = (Field)value.next();
            field.setAccessible(true);

            if(!value.hasNext()){
                sb.append(field.getName() + "=? ");
            }else{
                sb.append(field.getName() + "=?,");
            }

        }
        sb.append("WHERE " + pkey.getName() +"=? RETURNING *");
        sql = sb.toString();
        //System.out.println(sql);

        try{

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(ConstructPSQuery(t, columns, ps), id);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return buildObj(t, pkey, columns, rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public T deleteObj(Class<T> clazz, int id) throws IllegalAccessException {

        //getting all the declared fields from the class
        Field[] fields = clazz.getDeclaredFields();
        Field pkey = null;
        ArrayList<Field> columns = new ArrayList<Field>();
        StringJoiner sj = new StringJoiner(",");

        for (Field field : fields) {

            if (field.isAnnotationPresent(PrimaryKey.class)) {
                pkey = field; //id
                field.setAccessible(true);
                //System.out.println("Primary Key/Field Name: " + field.getName() + " || Value: " + field.get(t));
            } else if (field.isAnnotationPresent(Column.class)) {
                sj.add(field.getName()); //field.getName() = title
                columns.add(field);
                field.setAccessible(true);
                //System.out.println("Primary Key/Field Name: " + field.getName() + " || Value: " + field.get(t));
            }
        }

        String sql;
        //add one to the length so as not to include the primary key
        Iterator value = columns.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM " + clazz.getSimpleName());
        sb.append(" WHERE " + pkey.getName() +"=? RETURNING *");
        sql = sb.toString();
        //System.out.println(sql);

        try{
            T t = clazz.getConstructor().newInstance();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return buildObj(t, pkey, columns, rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }


    /*
     ***************************************************
     *   Helper Functions for CRUD Operations
     ****************************************************
     */

    private int ConstructPSQuery(T t, ArrayList<Field> columns, PreparedStatement ps) throws IllegalAccessException, SQLException {
        int index = 1;
        Iterator value = columns.iterator();

        while(value.hasNext()){
            Field field = (Field)value.next();
            field.setAccessible(true);
            //System.out.println("value: " + field.get(t));
            if(field.getType() == int.class ){
                ps.setInt(index, (Integer) field.get(t));
            } else if(field.getType() == double.class ){
                ps.setDouble(index, (Double) field.get(t));
            }else if (field.getType() == String.class){
                ps.setString(index, (String)field.get(t));
            }else if (field.getType() == boolean.class){
                ps.setBoolean(index, (boolean)field.get(t));
            }else if (field.getType() == long.class){
                ps.setLong(index, (long)field.get(t));
            }
            index++;
        }

        return index;
    }

    private T buildObj(T t, Field pkey, ArrayList<Field> columns, ResultSet rs) throws IllegalAccessException, SQLException {
        Iterator value;
        T claz = t;
        value = columns.iterator();

        while(value.hasNext()){
            Field field = (Field)value.next();
            field.setAccessible(true);
            //System.out.println("nameOfField: " + field.getName() + "|| value: " + field.get(t));

            //Set the primary key value
            pkey.setInt(claz, rs.getInt(pkey.getName()));

            if(field.getType() == int.class ){
                field.set(claz, rs.getInt(field.getName()));
            }else if(field.getType() == double.class ){
                field.set(claz, rs.getDouble(field.getName()));
            }else if (field.getType() == String.class){
                field.set(claz, rs.getString(field.getName()));
            }else if (field.getType() == boolean.class){
                field.set(claz, rs.getBoolean(field.getName()));
            }else if (field.getType() == long.class){
                field.set(claz, rs.getLong(field.getName()));
            }
        }

        return claz;
    }


}

