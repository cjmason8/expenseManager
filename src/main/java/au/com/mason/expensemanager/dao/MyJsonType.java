package au.com.mason.expensemanager.dao;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;
import org.springframework.util.ObjectUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MyJsonType implements UserType {
	 
    private final Gson gson = new GsonBuilder().serializeNulls().create();
 
    @Override
    public Object deepCopy(Object originalValue) throws HibernateException {
        if (originalValue == null) {
            return null;
        }
 
        if (!(originalValue instanceof Map)) {
            return null;
        }
 
        Map<String, Object> resultMap = new HashMap<>();
 
        Map<?, ?> tempMap = (Map<?, ?>) originalValue;
        tempMap.forEach((key, value) -> {
        	if (value instanceof ArrayList) {
        		resultMap.put((String) key, (List) value);
        	}
        	else {
        		resultMap.put((String) key, (String) value);
        	}
        });
 
        return resultMap;
    }
 
    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        Object copy = deepCopy(value);
 
        if (copy instanceof Serializable) {
            return (Serializable) copy;
        }
 
        throw new SerializationException(String.format("Cannot serialize '%s', %s is not Serializable.", 
        		value, value.getClass()), null);
    }
 
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }
 
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
 
    @Override
    public boolean isMutable() {
        return true;
    }
 
    @Override
    public int hashCode(Object x) throws HibernateException {
        if (x == null) {
            return 0;
        }
 
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, int i, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws SQLException {
        return null;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return ObjectUtils.nullSafeEquals(x, y);
    }

    @Override
    public int getSqlType() {
        return Types.JAVA_OBJECT;
    }

    @Override
    public Class<?> returnedClass() {
        return Map.class;
    }
 
	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor arg3)
			throws HibernateException, SQLException {
		if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, gson.toJson(value, Map.class), Types.OTHER);
        }
	}
	
}