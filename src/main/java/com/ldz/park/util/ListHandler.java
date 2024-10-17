package com.ldz.park.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ListHandler extends BaseTypeHandler<List<String>>  {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<String> attachments, JdbcType jdbcType) throws SQLException {
        Gson gson = new Gson();
        if(attachments != null) {
            preparedStatement.setString(i, gson.toJson(attachments,new TypeToken<List<String>>() {}.getType()));
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        Gson gson = new Gson();
        String v = resultSet.getString(s);
        if(StringUtils.isNoneBlank(v)) {
            return gson.fromJson(v, new TypeToken<List<String>>() {}.getType());
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        Gson gson = new Gson();
        String v = resultSet.getString(i);
        if(StringUtils.isNoneBlank(v)) {
            return gson.fromJson(v, new TypeToken<List<String>>() {}.getType());
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        Gson gson = new Gson();
        String v = callableStatement.getString(i);
        if(StringUtils.isNoneBlank(v)) {
            return gson.fromJson(v, new TypeToken<List<String>>() {}.getType());
        }
        return new ArrayList<>();
    }

}
