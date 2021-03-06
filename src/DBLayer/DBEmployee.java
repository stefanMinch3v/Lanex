package DBLayer;

import ModelLayer.Employee;
import ModelLayer.Person;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by USER on 12.4.2017 г..
 */
public class DBEmployee implements IDBEmployee {
    /**
     *
    // * @param name: employee name
   //  * @param address: employee address
    // * @param email: employee email
    // * @param phone: employee phone
    // * @param city: employee city
     * @return employee object
     * @throws SQLException thrown if insertion fails
     */

    //check the numbers of tuples in the DB - needed fot transaction part!
    private int getNumberRows(){
        int counter = 0;
        String sql = String.format("SELECT * FROM person WHERE category = 1");
        try{
            Connection conn = DBConnection.getInstance().getDBcon();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()){
                counter++;
            }

        } catch (Exception e){
            e.getMessage();
        } finally {
            DBConnection.closeConnection();
        }
        return counter;
    }



    @Override
    public Employee create(String name, String address, String email, String phone, String city,int work_id)throws SQLException{
        int currentRows = getNumberRows();
        Employee employee = new Employee(name,address,email,phone,city,work_id);
        String sql = "BEGIN TRANSACTION " +
                "INSERT INTO person (name, address, email, phone, city, category) " +
                "VALUES ('"+name+"', '"+address+"', '"+email+"', '"+phone+"', '"+city+"', 1) " +
                "declare @sql as int " +
                "SELECT @sql = id " +
                "FROM Person ORDER BY Person.id ASC " +
                "INSERT INTO employee (work_id, person_id) VALUES ('"+work_id+"', @sql) " +
                "IF @@ROWCOUNT <> 1 " +
                "BEGIN " +
                "ROLLBACK " +
                "RAISERROR('Work ID already exists!', 16, 1) " +
                "END " +
                "ELSE " +
                "BEGIN " +
                "COMMIT " +
                "END";
        try{
            Connection conn = DBConnection.getInstance().getDBcon();
            conn = DBConnection.getInstance().getDBcon();
            conn.createStatement().executeUpdate(sql);

        } catch (SQLException e){
            e.getMessage();
        } finally {
            DBConnection.closeConnection();
        }

        int lastChangesOfRows = getNumberRows();
        if(currentRows == lastChangesOfRows) {
            throw new IllegalArgumentException("The Work ID already exists!!!");
        }

        return employee;

       /* Employee employee = new Employee();
        String sql = String.format("INSERT INTO person (name, address, email, phone, city, category) VALUES ('%s', '%s', '%s', '%s', '%s', 1)", name, address, email, phone, city);
        try{
            Connection conn = DBConnection.getInstance().getDBcon();
            conn.createStatement().executeUpdate(sql);

            String sql2 = "SELECT TOP 1 id FROM Person ORDER BY id DESC";
            ResultSet rs = conn.createStatement().executeQuery(sql2);

            if(rs.next()) {
                String sql3 = "INSERT INTO employee (work_id,person_id) VALUES ("+work_id+","+rs.getInt("id")+")";
                conn.createStatement().executeUpdate(sql3);
            }else{
                throw new SQLException();
            }

        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection();
        }

        int lastChangesOfRows = getNumberRows();
        if(currentRows == lastChangesOfRows) {
            throw new IllegalArgumentException("The WORK ID already exists!!!");
        }
        return employee;
        */
    }


   @Override
    public Employee read(int work_id) throws SQLException{
        Employee employee = new Employee();
        try{
            java.sql.Connection conn = DBConnection.getInstance().getDBcon();
            String sql = String.format("SELECT p.id, p.name , p.address, p.email, p.city, p.phone, " +
                    "e.work_id FROM person p \n" +
                    "INNER JOIN employee e \n" +
                    "ON p.id = e.person_id \n" +
                    "WHERE e.work_id =" +work_id+ " \n" +
                    "ORDER BY p.id ASC");
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()){
                employee = buildObject(rs);
            }
        }catch (SQLException e) {
            throw e;
        }finally{
            DBConnection.closeConnection();
        }
        return employee;
    }



    @Override
    public boolean update(Employee employee , int work_id) throws SQLException{
        try {
            Connection conn = DBConnection.getInstance().getDBcon();
            String name = employee.getName();
            String address = employee.getAddress();
            String email = employee.getEmail();
            String phone = employee.getPhone();
            String city = employee.getCity();
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE Person \n" +
                    "SET name = ?, address = ?, email = ?, city = ?, phone = ?\n" +
                    "FROM Person p\n" +
                    "INNER JOIN Employee e\n" +
                    "ON p.id = e.person_id\n" +
                    "WHERE e.work_id = work_id");
            preparedStatement.setNString(1,name);
            preparedStatement.setNString(2,address);
            preparedStatement.setNString(3,email);
            preparedStatement.setNString(4,city);
            preparedStatement.setNString(5,phone);
            preparedStatement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            DBConnection.closeConnection();
        }

        return true;
    }

    @Override
    public boolean delete(int work_id)throws SQLException{
        try {
            Connection conn = DBConnection.getInstance().getDBcon();
            String sql0 = String.format("Select person_id from employee where work_id ='%d' ",work_id);
            ResultSet rs = conn.createStatement().executeQuery(sql0);
            int person_id;
            while(rs.next()){
                person_id =rs.getInt(1);
                String sql = String.format("Delete from person where id= '%d'",person_id);
                conn.createStatement().executeUpdate(sql);
            }

        } catch(SQLException e) {
            e.printStackTrace();
            throw e;
        }finally {
            DBConnection.closeConnection();
        }
        return true;
    }

    @Override
    public ArrayList<Person> readAll() throws SQLException {
        ArrayList<Person> person = new ArrayList<>();
        String sql = "SELECT * FROM person WHERE category = 1";
        try(Statement st = DBConnection.getInstance().getDBcon().createStatement()) {
            ResultSet rs= st.executeQuery(sql);
            while (rs.next()) {
                person = buildObjects(rs);
            }

        } catch (SQLException e){
            e.printStackTrace();
            throw e;
        }
        finally {
            DBConnection.closeConnection();
        }
        return person;
    }


    private Employee buildObject(ResultSet rs) throws SQLException{
        Employee employee = new Employee();
        try {
            employee.setName(rs.getString("name"));
            employee.setAddress(rs.getString("address"));
            employee.setEmail(rs.getString("email"));
            employee.setPhone(rs.getString("phone"));
            employee.setCity(rs.getString("city"));
            employee.setWorkId(rs.getInt(1));
        } catch(SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return employee;
    }


    private ArrayList<Person> buildObjects(ResultSet rs) throws SQLException{
        ArrayList<Person> person = new ArrayList<>();
        while(rs.next()) {
            person.add(buildObject(rs));
        }
        return person;
    }

}
