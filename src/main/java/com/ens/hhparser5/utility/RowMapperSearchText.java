package com.ens.hhparser5.utility;

import com.ens.hhparser5.model.SearchText;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RowMapperSearchText implements RowMapper<SearchText> {
    @Override
    public SearchText mapRow(ResultSet rs, int i) throws SQLException {
        SearchText searchText = new SearchText();
        searchText.setText(rs.getString("text"));
        searchText.setProjectId(rs.getLong("project_id"));
        searchText.setId(rs.getLong("id"));

        return searchText;
    }
}
