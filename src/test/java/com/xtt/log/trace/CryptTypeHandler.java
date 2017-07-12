package com.xtt.log.trace;

//import com.systoon.commons.pojo.annotation.EncryptModeCollection;
//import com.systoon.commons.pojo.annotation.EncryptType;
//import com.systoon.commons.pojo.annotation.handler.EncryptFactory;
//import com.systoon.commons.pojo.annotation.handler.IEncrypt;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CryptTypeHandler extends BaseTypeHandler<CryptType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CryptType parameter, JdbcType jdbcType) throws SQLException {
        String value="";
        if(parameter!=null && parameter.toString()!=null){
            value=encrypt(parameter.toString());
        }
        ps.setString(i, value);
    }

    @Override
    public CryptType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public CryptType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public CryptType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }
    private String encrypt(String value) {
//        value=getEncryptUtil().encrypt(value);
        return value;
    }
    private CryptType decrypt(String value){
        CryptType v=new CryptType(value);
        /*if(value!=null){
            value=getEncryptUtil().decrypt(value.toString());
            v.setValue(value);
        }*/
        return v;
    }

    /*private IEncrypt getEncryptUtil(){
        return EncryptFactory.getEncryptTypeFactory(EncryptType.SysmmertricEncrypt).getEncryptByMode(EncryptModeCollection.SymmertricMode.AES);
    }*/



   /*
   private Object  encodeProperty(Object bean){
        return transBean(bean,true);
    }
    private Object  decodeProperty(Object bean){
        return transBean(bean,false);
    }
    private  Object  transBean(Object bean,Boolean isEncrypt){
        if(bean instanceof ArrayList<?>){
            List<?> list = (ArrayList<?>)bean;
            for(Object val:list){
                transBeanProperty(val,isEncrypt);
            }
        }else{
            transBeanProperty(bean,isEncrypt);
        }
        return bean;
    }
    private  Object  transBeanProperty(Object bean,Boolean isEncrypt){
        try {
            Field[] fields = bean.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Allow.class)) {
                    field.setAccessible(true); //设置些属性是可以访问的
                    String val = ObjectUtils.toString(field.get(bean));//得到此属性的值
                    field.set(bean, isEncrypt ? getEncryptUtil().encrypt(val) : getEncryptUtil().decrypt(val));
                }
            }
            return bean;
        }catch (Exception e){
            return bean;
        }
    }

    private IEncrypt getEncryptUtil(){
        return EncryptFactory.getEncryptTypeFactory(EncryptType.SysmmertricEncrypt).getEncryptByMode(EncryptModeCollection.SymmertricMode.AES);
    }*/
}
