package com.gmc.trading.infra.strategy;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

  private static final String PREFIX_TABLE_NAME = "tt_";
  private static final String PREFIX_SEQUENCE_NAME = "seq_";

  @Override
  public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return super.toPhysicalSequenceName(Identifier.toIdentifier(PREFIX_SEQUENCE_NAME + PREFIX_TABLE_NAME + name.getText()), jdbcEnvironment);
  }

  @Override
  public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return super.toPhysicalTableName(Identifier.toIdentifier(PREFIX_TABLE_NAME + name.getText()), jdbcEnvironment);
  }
}