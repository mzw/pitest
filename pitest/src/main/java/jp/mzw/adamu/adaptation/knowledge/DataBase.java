package jp.mzw.adamu.adaptation.knowledge;

import java.sql.Connection;
import java.sql.SQLException;

interface DataBase {
    
    Connection getConnection() throws SQLException;
    void init() throws SQLException;
    void close() throws SQLException;
    
}
