package ru.doccloud.common.jooq;
import static org.jooq.tools.Convert.convert;

import java.io.IOException;
import java.sql.*;
import org.jooq.*;
import org.jooq.impl.DSL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

public class PostgresJSONJacksonJsonNodeBinding implements Binding<Object, JsonNode> { 

    @Override
    public Converter<Object, JsonNode> converter() {
        return new Converter<Object, JsonNode>() {
            @Override
            public JsonNode from(Object t) {
                try {
                    return t == null 
                      ? NullNode.instance 
                      : new ObjectMapper().readTree(t + "");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object to(JsonNode u) {
                try {
                    return u == null || u.equals(NullNode.instance) 
                      ? null 
                      : new ObjectMapper().writeValueAsString(u);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Class<Object> fromType() {
                return Object.class;
            }

            @Override
            public Class<JsonNode> toType() {
                return JsonNode.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<JsonNode> ctx) throws SQLException {

        // This ::json cast is explicitly needed by PostgreSQL:
        ctx.render().visit(DSL.val(ctx.convert(converter()).value())).sql("::jsonb");
    }

    @Override
    public void register(BindingRegisterContext<JsonNode> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<JsonNode> ctx) throws SQLException {
        ctx.statement().setString(
            ctx.index(), 
            convert(ctx.convert(converter()).value(), String.class));
    }

    @Override
    public void get(BindingGetResultSetContext<JsonNode> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<JsonNode> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
    }

    // The below methods aren't needed in PostgreSQL:

    @Override
    public void set(BindingSetSQLOutputContext<JsonNode> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<JsonNode> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}