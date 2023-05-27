package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Vacancy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class VacancyRepoImpl implements VacancyRepo{

    @Autowired
    private Connection connection;

    /**
     * Ищет вакансию в базе
     * Если находит - обновляет
     * Если не находит - создает новую, устанавливает поле id значением из базы данных
     * Возвращает этот же объект из параметра vacancy.
     * Возвращать надо обязательно, т.к. даже если обновляем существующую вакансию,
     * то в переданном объекте не заполнен наш id. Он заполняется в этом методе.
     * @param vacancy
     * @return
     */
    @Override
    public Vacancy saveOrUpdate(Vacancy vacancy) {
        // поиск ведем по hhid, т.к. в переданном объекте не заполнен наш id.
        // Он заполняется в этом методе.
        Vacancy existingVacancy = this.findByHhid(vacancy.getHhid());

        try {
            PreparedStatement stmt;
            String action = "";
            if (existingVacancy.getId() == -1L) {
                stmt = insertStatementDriverManager();//getInsertStatement();
                action = "insert";
            } else {
                stmt = updateStatementDriverManager();//getUpdateStatement();
                action = "update";
            }
            stmt.setString(1, vacancy.getHhid());
            stmt.setString(2, vacancy.getName());
            stmt.setInt(3, vacancy.getSalary_from());
            stmt.setInt(4, vacancy.getSalary_to());
            stmt.setInt(5, vacancy.getGross());
            stmt.setString(6, vacancy.getUrl());
            stmt.setString(7, vacancy.getAlternate_url());
            stmt.setLong(8, vacancy.getEmployer());
            stmt.setInt(9, vacancy.getArchived() ? 1 : 0);
            stmt.setInt(10, vacancy.getRegion());
            // сюда передали не до конца заполненный объект - не хватает нашего id
            // поэтому если вакансия уже есть в базе, то добавим ее id в этот dto
            if (action.equals("update")) {
                stmt.setLong(11, existingVacancy.getId());
            }
            stmt.executeUpdate();
            // если создавали новую вакансию, нужно получить созданный id из базы данных
            // и добавить его к dto
            if (action.equals("insert")) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    vacancy.setId(keys.getLong(1));
                }
                keys.close();
            } else {
                vacancy.setId(existingVacancy.getId());
            }
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return vacancy;
    }

    private void fillVacancyFromRS(ResultSet rs, Vacancy vac) throws SQLException {
        vac.setId(rs.getLong("id"));
        vac.setHhid(rs.getString("hhid"));
        vac.setName(rs.getString("name"));
        vac.setArchived(rs.getInt("archived") == 1);
        vac.setAlternate_url(rs.getString("alternate_url"));
        vac.setGross(rs.getInt("gross"));
        vac.setUrl(rs.getString("url"));
        vac.setSalary_from(rs.getInt("salary_from"));
        vac.setSalary_to(rs.getInt("salary_to"));
        vac.setEmployer(rs.getLong("employer_id"));
        vac.setRegion(rs.getInt("region"));
    }

    @Override
    public Vacancy findByHhid(String hhid) {
        try(PreparedStatement stmt = connection.prepareStatement( "SELECT * FROM vacancy WHERE hhid = ?" )) {
            stmt.setString(1, hhid);
            ResultSet rs = stmt.executeQuery();
            Vacancy vac = new Vacancy(-1L);
            if (rs.next()) {
                fillVacancyFromRS(rs, vac);
            }
            rs.close();
            return vac;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Vacancy findById(long vacancyId) {
        try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM vacancy WHERE id = ?")) {
            stmt.setLong(1, vacancyId);
            ResultSet rs = stmt.executeQuery();
            Vacancy vac = new Vacancy(-1L);
            if (rs.next()){
                fillVacancyFromRS(rs, vac);
            }
            rs.close();
            return vac;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //--------------------  INSERT DriverManager
    private PreparedStatement insertStatementDriverManager() throws Exception {
        return connection.prepareStatement("""
                INSERT INTO vacancy (hhid, name, salary_from, salary_to, gross, url, alternate_url, employer_id, archived, region) VALUES (?,?,?,?,?,?,?,?,?,?)
                   """, Statement.RETURN_GENERATED_KEYS);
    }

    //--------------------  UPDATE DriverManager
    private PreparedStatement updateStatementDriverManager() throws Exception {
        return connection.prepareStatement("""
                UPDATE vacancy SET hhid=?,name=?, salary_from=?, salary_to=?, gross=?, url=?, alternate_url=?, employer_id=?, archived=?, region=? WHERE id=?
                   """);
    }
}

